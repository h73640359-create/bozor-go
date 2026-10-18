import { Router } from 'express';
import { config } from '../config.js';
import { createOtp, loginAdmin, signToken, verifyOtp } from '../auth.js';
import { asyncHandler, badRequest } from '../http.js';
import { isValidPhone, normalizePhone } from '../validate.js';

const router = Router();

/** POST /auth/otp/request — SMS kodini so'rash (demo: kod javobda qaytadi). */
router.post(
  '/otp/request',
  asyncHandler(async (req, res) => {
    const phone = String(req.body?.phone ?? '');
    if (!isValidPhone(phone)) {
      throw badRequest('Telefon raqami noto\'g\'ri (+998XXXXXXXXX)');
    }
    const { code } = await createOtp(phone);
    res.json({
      success: true,
      demo: config.demo,
      message: config.demo
        ? 'Demo rejim: SMS yuborilmadi, kod javobda qaytarildi'
        : 'Kod yuborildi',
      debugCode: config.demo ? code : undefined,
    });
  }),
);

/** POST /auth/otp/verify — kodni tekshirish va JWT olish. */
router.post(
  '/otp/verify',
  asyncHandler(async (req, res) => {
    const phone = String(req.body?.phone ?? '');
    const code = String(req.body?.code ?? '');
    const name = req.body?.name ? String(req.body.name) : null;
    if (!isValidPhone(phone)) throw badRequest('Telefon raqami noto\'g\'ri');
    if (!/^\d{6}$/.test(code)) throw badRequest('Kod 6 xonali bo\'lishi kerak');

    const user = await verifyOtp(phone, code, name);
    const token = signToken({ sub: user.id, phone: user.phone, role: 'user' });
    res.json({
      token,
      user: {
        id: user.id,
        name: user.name,
        phone: normalizePhone(user.phone),
        isAdmin: Boolean(user.is_admin),
      },
    });
  }),
);

/** POST /auth/admin/login — admin panel uchun kirish. */
router.post(
  '/admin/login',
  asyncHandler(async (req, res) => {
    const email = String(req.body?.email ?? '').trim().toLowerCase();
    const password = String(req.body?.password ?? '');
    if (!email || !password) throw badRequest('Email va parol kerak');
    const admin = await loginAdmin(email, password);
    const token = signToken({ sub: admin.id, email: admin.email, role: admin.role });
    res.json({
      token,
      user: { id: admin.id, name: admin.name, email: admin.email, isAdmin: true },
    });
  }),
);

export default router;
