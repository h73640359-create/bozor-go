import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { config } from './config.js';
import { many, one, query } from './db.js';
import { ApiError, forbidden, notFound, unauthorized } from './http.js';
import { normalizePhone } from './validate.js';

const OTP_TTL_MINUTES = 10;
const OTP_MAX_ATTEMPTS = 5;

function randomDigits(length) {
  let out = '';
  for (let i = 0; i < length; i += 1) {
    out += Math.floor(Math.random() * 10).toString();
  }
  return out;
}

export function signToken(payload, ttlDays = config.jwtTtlDays) {
  return jwt.sign(payload, config.jwtSecret, { expiresIn: `${ttlDays}d` });
}

export function verifyToken(token) {
  try {
    return jwt.verify(token, config.jwtSecret);
  } catch {
    return null;
  }
}

/** Mijoz (telefon) yoki admin tokeni bo'lishi shart. */
export function requireAuth(req, _res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return next(unauthorized('Token talab qilinadi'));
  const payload = verifyToken(token);
  if (!payload) return next(unauthorized('Token yaroqsiz yoki muddati o\'tgan'));
  req.auth = payload;
  return next();
}

/**
 * Admin huquqi SERVER TOMONIDA tekshiriladi:
 * token ichidagi role maydoni va bazadagi admin_users yozuvi.
 */
export async function requireAdmin(req, _res, next) {
  try {
    if (!req.auth) return next(unauthorized());
    if (req.auth.role !== 'admin' && req.auth.role !== 'manager') {
      return next(forbidden('Bu amal faqat admin uchun'));
    }
    const admin = await one(
      'SELECT id, email, role, is_active FROM admin_users WHERE id = $1',
      [req.auth.sub],
    );
    if (!admin || !admin.is_active) return next(forbidden('Admin topilmadi yoki faol emas'));
    req.admin = admin;
    return next();
  } catch (error) {
    return next(error);
  }
}

/** Ixtiyoriy: token bo'lsa — foydalanuvchini aniqlaydi. */
export function optionalAuth(req, _res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (token) {
    const payload = verifyToken(token);
    if (payload) req.auth = payload;
  }
  next();
}

/** Telefon orqali kirish: SMS kodini yaratish. */
export async function createOtp(phone) {
  const normalized = normalizePhone(phone);
  const code = randomDigits(6);
  await query(
    `INSERT INTO otp_codes (phone, code, expires_at)
     VALUES ($1, $2, now() + ($3 || ' minutes')::interval)`,
    [normalized, code, String(OTP_TTL_MINUTES)],
  );

  // Haqiqiy SMS provayderi ulanmagani uchun kod faqat logga yoziladi.
  if (config.smsProvider === 'none') {
    console.log(`[otp] ${normalized} uchun kod: ${code} (SMS_PROVIDER=none, demo rejim)`);
  } else {
    console.warn(`[otp] SMS_PROVIDER=${config.smsProvider} hali ulanmagan — kod yuborilmadi`);
    throw new ApiError(501, 'SMS provayderi ulanmagan. Kod yuborilmadi.');
  }

  return { phone: normalized, code, demo: config.demo };
}

/** Kodni tekshirish va foydalanuvchini yaratish/yangilash. */
export async function verifyOtp(phone, code, name) {
  const normalized = normalizePhone(phone);
  const record = await one(
    `SELECT * FROM otp_codes
      WHERE phone = $1 AND consumed = FALSE AND expires_at > now()
      ORDER BY created_at DESC LIMIT 1`,
    [normalized],
  );

  if (!record) throw new ApiError(400, 'Kod topilmadi yoki muddati o\'tgan');
  if (record.attempts >= OTP_MAX_ATTEMPTS) throw new ApiError(429, 'Juda ko\'p urinish. Yangi kod so\'rang');
  if (record.code !== String(code)) {
    await query('UPDATE otp_codes SET attempts = attempts + 1 WHERE id = $1', [record.id]);
    throw new ApiError(400, 'Kod noto\'g\'ri');
  }

  await query('UPDATE otp_codes SET consumed = TRUE WHERE id = $1', [record.id]);

  let user = await one('SELECT * FROM users WHERE phone = $1', [normalized]);
  if (!user) {
    const id = `usr-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
    user = await one(
      `INSERT INTO users (id, phone, name) VALUES ($1, $2, $3) RETURNING *`,
      [id, normalized, String(name ?? '').trim().slice(0, 120)],
    );
  } else if (name && !user.name) {
    user = await one('UPDATE users SET name = $2 WHERE id = $1 RETURNING *', [user.id, String(name).trim()]);
  }

  return user;
}

/** Birinchi ishga tushirishda admin yaratish. */
export async function ensureAdminUser() {
  const existing = await one('SELECT id FROM admin_users WHERE email = $1', [config.admin.email]);
  if (existing) return existing;
  const id = `adm-${Date.now().toString(36)}`;
  const hash = await bcrypt.hash(config.admin.password, 10);
  return one(
    `INSERT INTO admin_users (id, email, name, password_hash, role)
     VALUES ($1, $2, $3, $4, 'admin') RETURNING id, email, name, role`,
    [id, config.admin.email, config.admin.name, hash],
  );
}

export async function loginAdmin(email, password) {
  const admin = await one('SELECT * FROM admin_users WHERE email = $1', [String(email).toLowerCase()]);
  if (!admin || !admin.is_active) throw notFound('Admin topilmadi');
  const ok = await bcrypt.compare(String(password), admin.password_hash);
  if (!ok) throw new ApiError(401, 'Email yoki parol noto\'g\'ri');
  return admin;
}

export async function listAdminUsers() {
  return many('SELECT id, email, name, role, is_active, created_at FROM admin_users ORDER BY created_at');
}
