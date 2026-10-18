import { Router } from 'express';
import { config } from '../config.js';
import { one } from '../db.js';
import { asyncHandler, badRequest, notFound } from '../http.js';
import { requireAuth } from '../auth.js';

const router = Router();

/**
 * POST /payments/create
 *
 * Haqiqiy provayder (PAYMENT_PROVIDER) ulanmaganda hech qachon
 * "muvaffaqiyatli to'landi" deb javob qaytarilmaydi — demo ekani
 * aniq ko'rsatiladi.
 */
router.post(
  '/payments/create',
  requireAuth,
  asyncHandler(async (req, res) => {
    const orderId = String(req.body?.orderId ?? '');
    const method = String(req.body?.method ?? 'card');
    if (!orderId) throw badRequest('orderId kerak');
    if (!['cash', 'card'].includes(method)) throw badRequest('To\'lov usuli noto\'g\'ri');

    const order = await one('SELECT * FROM orders WHERE id = $1', [orderId]);
    if (!order) throw notFound('Buyurtma topilmadi');

    const isAdmin = req.auth.role === 'admin' || req.auth.role === 'manager';
    if (!isAdmin && order.user_id && order.user_id !== req.auth.sub) {
      throw badRequest('Bu buyurtma sizga tegishli emas');
    }

    if (method === 'cash') {
      return res.json({
        status: 'pending',
        demo: config.demo,
        message: 'Naqd to\'lov: kuryerga yetkazib berilganda to\'lanadi',
      });
    }

    if (config.paymentProvider === 'none') {
      return res.json({
        status: 'pending_demo',
        paymentUrl: null,
        demo: true,
        message:
          'To\'lov provayderi ulanmagan (PAYMENT_PROVIDER=none). Karta orqali to\'lov demo rejimda, ' +
          'haqiqiy pul o\'tkazilmadi.',
      });
    }

    // Provayder ulanganda shu yerda integratsiya qilinadi.
    return res.json({
      status: 'not_implemented',
      demo: config.demo,
      message: `To'lov provayderi (${config.paymentProvider}) uchun integratsiya hali yozilmagan`,
    });
  }),
);

export default router;
