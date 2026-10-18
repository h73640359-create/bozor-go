import express from 'express';
import cors from 'cors';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { config } from './config.js';
import { ensureAdminUser } from './auth.js';
import { healthcheck } from './db.js';
import { errorHandler, notFound } from './http.js';
import catalogRoutes from './routes/catalog.js';
import authRoutes from './routes/auth.js';
import orderRoutes from './routes/orders.js';
import paymentRoutes from './routes/payments.js';
import adminRoutes from './routes/admin.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();

app.use(cors({ origin: config.corsOrigin === '*' ? true : config.corsOrigin.split(',') }));
app.use(express.json({ limit: '2mb' }));
app.use(express.urlencoded({ extended: true }));

// Yuklangan rasmlar
app.use('/uploads', express.static(path.join(__dirname, 'public', 'uploads')));

// Admin panel (statik fayllar) — http://localhost:3000/admin
app.use('/admin', express.static(path.join(__dirname, 'public', 'admin')));

// REST API
app.use(config.apiPrefix, catalogRoutes);
app.use(config.apiPrefix, authRoutes);
app.use(config.apiPrefix, orderRoutes);
app.use(config.apiPrefix, paymentRoutes);
app.use(`${config.apiPrefix}/admin`, adminRoutes);

app.get('/health', async (_req, res) => {
  const ok = await healthcheck().catch(() => false);
  res.status(ok ? 200 : 503).json({ status: ok ? 'ok' : 'db-unavailable', demo: config.demo });
});

app.use((_req, _res, next) => next(notFound('Bunday yo\'l mavjud emas')));
app.use(errorHandler);

const port = config.port;

async function start() {
  const dbOk = await healthcheck().catch((error) => {
    console.error('[db] ulanib bo\'lmadi:', error.message);
    return false;
  });
  if (!dbOk) {
    console.error('[db] PostgreSQL ishlamayapti. DATABASE_URL ni tekshiring yoki `docker compose up -d` bajaring.');
    process.exit(1);
  }

  const admin = await ensureAdminUser();
  app.listen(port, () => {
    console.log(`BozorGo API:  http://localhost:${port}${config.apiPrefix}`);
    console.log(`Admin panel:  http://localhost:${port}/admin  (${admin.email})`);
    console.log(`Demo rejim:   ${config.demo ? 'yoqilgan' : "o'chirilgan"}`);
    if (config.smsProvider === 'none') {
      console.log('OGOHLANTIRISH: SMS_PROVIDER=none — haqiqiy SMS yuborilmaydi (demo kod).');
    }
    if (config.paymentProvider === 'none') {
      console.log('OGOHLANTIRISH: PAYMENT_PROVIDER=none — karta to\'lovi amalga oshirilmaydi.');
    }
  });
}

start().catch((error) => {
  console.error(error);
  process.exit(1);
});
