import 'dotenv/config';

function bool(value, fallback = false) {
  if (value === undefined || value === '') return fallback;
  return ['1', 'true', 'yes', 'on'].includes(String(value).toLowerCase());
}

const jwtSecret = process.env.JWT_SECRET || '';

if (jwtSecret.length < 16 && process.env.NODE_ENV === 'production') {
  throw new Error('JWT_SECRET kamida 16 belgi bo\'lishi kerak (production)');
}

export const config = {
  port: Number(process.env.PORT || 3000),
  apiPrefix: process.env.API_PREFIX || '/api',
  corsOrigin: process.env.CORS_ORIGIN || '*',
  databaseUrl: process.env.DATABASE_URL || 'postgres://bozorgo:bozorgo@localhost:5432/bozorgo',
  jwtSecret: jwtSecret || 'demo-secret-change-me-please-32-chars',
  jwtTtlDays: Number(process.env.JWT_TTL_DAYS || 30),
  admin: {
    email: process.env.ADMIN_EMAIL || 'admin@bozorgo.local',
    password: process.env.ADMIN_PASSWORD || 'change-me-admin',
    name: process.env.ADMIN_NAME || 'BozorGo Admin',
  },
  smsProvider: process.env.SMS_PROVIDER || 'none',
  paymentProvider: process.env.PAYMENT_PROVIDER || 'none',
  demo: bool(process.env.DEMO_MODE, true),
};

export const isDemo = () => config.demo;
