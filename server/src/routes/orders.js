import { Router } from 'express';
import { one, many, query, transaction } from '../db.js';
import { asyncHandler, badRequest, conflict, forbidden, notFound } from '../http.js';
import { optionalAuth, requireAuth } from '../auth.js';
import { calculateTotals, effectivePrice, round2 } from '../pricing.js';
import { orderItemToDto, orderToDto } from '../serialize.js';
import { canTransition, validateOrderInput } from '../validate.js';

const router = Router();

function orderNumber() {
  return `BG-${String(Math.floor(100000 + Math.random() * 900000))}`;
}

async function loadOrder(id, userId = null) {
  const row = userId
    ? await one('SELECT * FROM orders WHERE id = $1 AND user_id = $2', [id, userId])
    : await one('SELECT * FROM orders WHERE id = $1', [id]);
  if (!row) return null;
  const items = await many('SELECT * FROM order_items WHERE order_id = $1 ORDER BY id', [id]);
  return orderToDto(row, items.map(orderItemToDto));
}

/**
 * POST /orders
 *
 * Server mustaqil ravishda:
 *  - mahsulot narxi, chegirmasi va ombor qoldig'ini bazadan qayta o'qiydi;
 *  - summani o'zi hisoblaydi (mijoz yuborgan narxga ishonilmaydi);
 *  - idempotencyKey bo'yicha takroriy yuborishni bloklaydi.
 */
router.post(
  '/orders',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const errors = validateOrderInput(req.body);
    if (errors.length) throw badRequest('Buyurtma ma\'lumotlari noto\'g\'ri', errors);

    const {
      items,
      deliveryMethod,
      paymentMethod,
      recipientName,
      phone,
      addressTitle = '',
      addressText,
      comment = '',
      idempotencyKey,
    } = req.body;

    // Takroriy yuborish: shu kalit bilan buyurtma bo'lsa — mavjudini qaytaramiz
    if (idempotencyKey) {
      const existing = await one('SELECT id FROM orders WHERE idempotency_key = $1', [idempotencyKey]);
      if (existing) {
        const order = await loadOrder(existing.id);
        return res.status(200).json({ order, repeated: true });
      }
    }

    const order = await transaction(async (client) => {
      // 1) Mahsulotlarni bazadan qayta o'qiymiz (narx/chegirma/ombor)
      const ids = items.map((item) => item.productId);
      const { rows } = await client.query(
        `SELECT * FROM products WHERE id = ANY($1) AND is_active = TRUE FOR UPDATE`,
        [ids],
      );
      const byId = new Map(rows.map((row) => [row.id, row]));

      const problems = [];
      const lines = [];
      for (const item of items) {
        const row = byId.get(item.productId);
        if (!row) {
          problems.push(`${item.productId}: mahsulot topilmadi`);
          continue;
        }
        if (!row.in_stock || row.stock <= 0) {
          problems.push(`${row.name}: omborda mavjud emas`);
          continue;
        }
        if (item.quantity > row.stock) {
          problems.push(`${row.name}: omborda faqat ${row.stock} dona qoldi`);
          continue;
        }
        lines.push({ row, quantity: item.quantity, selectedOption: item.selectedOption ?? null });
      }
      if (problems.length) throw badRequest('Buyurtmani rasmiylashtirib bo\'lmadi', problems);

      // 2) Summani server hisoblaydi
      const totals = calculateTotals(lines, deliveryMethod);

      // 3) Buyurtma raqami noyobligacha urinish
      let number = orderNumber();
      for (let attempt = 0; attempt < 5; attempt += 1) {
        const dup = await client.query('SELECT 1 FROM orders WHERE order_number = $1', [number]);
        if (dup.rowCount === 0) break;
        number = orderNumber();
      }

      const id = `ord-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
      const userId = req.auth?.role === 'user' ? req.auth.sub : null;

      const created = await client.query(
        `INSERT INTO orders (
            id, user_id, order_number, status, delivery_method, payment_method,
            recipient_name, phone, address_title, address_text, comment,
            items_total, discount, delivery_fee, total,
            is_paid, idempotency_key, is_demo
         ) VALUES ($1,$2,$3,'created',$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,FALSE,$15,$16)
         RETURNING *`,
        [
          id,
          userId,
          number,
          deliveryMethod,
          paymentMethod,
          String(recipientName).trim(),
          String(phone).trim(),
          String(addressTitle ?? '').trim(),
          String(addressText).trim(),
          String(comment ?? '').trim().slice(0, 1000),
          totals.itemsTotal,
          totals.discount,
          totals.deliveryFee,
          totals.total,
          idempotencyKey ? String(idempotencyKey) : null,
          true,
        ],
      );

      for (const line of lines) {
        await client.query(
          `INSERT INTO order_items (
              order_id, product_id, product_name, unit_price, discount_price,
              unit, unit_label, image_url, quantity, selected_option, line_total
           ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
          [
            id,
            line.row.id,
            line.row.name,
            Number(line.row.price),
            line.row.discount_price === null ? null : Number(line.row.discount_price),
            line.row.unit,
            line.row.unit_label,
            line.row.image_url || line.row.image_asset_path || '',
            line.quantity,
            line.selectedOption,
            round2(effectivePrice(line.row) * line.quantity),
          ],
        );
        // 4) Ombor qoldig'ini kamaytiramiz
        await client.query(
          `UPDATE products
              SET stock = stock - $2,
                  in_stock = (stock - $2) > 0
            WHERE id = $1`,
          [line.row.id, line.quantity],
        );
      }

      return created.rows[0];
    });

    const itemsDto = await many('SELECT * FROM order_items WHERE order_id = $1 ORDER BY id', [order.id]);
    return res.status(201).json({ order: orderToDto(order, itemsDto.map(orderItemToDto)) });
  }),
);

/** GET /orders — faqat o'z buyurtmalari (yoki admin: hammasi). */
router.get(
  '/orders',
  requireAuth,
  asyncHandler(async (req, res) => {
    const isAdmin = req.auth.role === 'admin' || req.auth.role === 'manager';
    const rows = isAdmin
      ? await many('SELECT * FROM orders ORDER BY created_at DESC LIMIT 200')
      : await many('SELECT * FROM orders WHERE user_id = $1 ORDER BY created_at DESC LIMIT 100', [req.auth.sub]);
    const orders = [];
    for (const row of rows) {
      const items = await many('SELECT * FROM order_items WHERE order_id = $1 ORDER BY id', [row.id]);
      orders.push(orderToDto(row, items.map(orderItemToDto)));
    }
    res.json({ orders });
  }),
);

/** GET /orders/:id */
router.get(
  '/orders/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const isAdmin = req.auth.role === 'admin' || req.auth.role === 'manager';
    const order = await loadOrder(req.params.id, isAdmin ? null : req.auth.sub);
    if (!order) throw notFound('Buyurtma topilmadi');
    res.json({ order });
  }),
);

/** POST /orders/:id/cancel */
router.post(
  '/orders/:id/cancel',
  requireAuth,
  asyncHandler(async (req, res) => {
    const isAdmin = req.auth.role === 'admin' || req.auth.role === 'manager';
    const current = isAdmin
      ? await one('SELECT * FROM orders WHERE id = $1', [req.params.id])
      : await one('SELECT * FROM orders WHERE id = $1 AND user_id = $2', [req.params.id, req.auth.sub]);
    if (!current) throw notFound('Buyurtma topilmadi');
    if (!canTransition(current.status, 'cancelled')) {
      throw conflict(`Buyurtmani "${current.status}" holatida bekor qilib bo'lmaydi`);
    }

    await transaction(async (client) => {
      await client.query(
        `UPDATE orders SET status = 'cancelled', updated_at = now() WHERE id = $1`,
        [current.id],
      );
      // Omborga qaytarish
      const { rows } = await client.query('SELECT * FROM order_items WHERE order_id = $1', [current.id]);
      for (const item of rows) {
        await client.query(
          `UPDATE products SET stock = stock + $2, in_stock = TRUE WHERE id = $1`,
          [item.product_id, item.quantity],
        );
      }
    });

    const order = await loadOrder(current.id);
    res.json({ order });
  }),
);

export default router;
