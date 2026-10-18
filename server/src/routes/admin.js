import { Router } from 'express';
import multer from 'multer';
import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import { config } from '../config.js';
import { many, one, query, transaction } from '../db.js';
import { requireAdmin } from '../auth.js';
import { asyncHandler, badRequest, conflict, notFound } from '../http.js';
import { bannerToDto, categoryToDto, orderItemToDto, orderToDto, productToDto } from '../serialize.js';
import { canTransition, ORDER_STATUSES } from '../validate.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const UPLOAD_DIR = path.resolve(__dirname, '..', 'public', 'uploads');
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, UPLOAD_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase().slice(0, 5) || '.img';
    const safe = /^[a-zA-Z0-9._-]+$/.test(ext) ? ext : '.img';
    cb(null, `img-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}${safe}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024, files: 1 },
  fileFilter: (_req, file, cb) => {
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowed.includes(file.mimetype)) {
      return cb(new Error('Faqat JPEG, PNG yoki WEBP rasm yuklash mumkin'));
    }
    return cb(null, true);
  },
});

const router = Router();

// Barcha admin yo'nalishlari SERVER TOMONIDA tekshiriladi
router.use(requireAdmin);

const PRODUCT_SELECT = `
  SELECT p.*, c.name AS category_name, s.name AS subcategory_name
    FROM products p
    JOIN categories c ON c.id = p.category_id
    JOIN subcategories s ON s.id = p.subcategory_id
`;

// ------------------------------- Statistika --------------------------------

router.get(
  '/stats',
  asyncHandler(async (_req, res) => {
    const [products, orders, revenue, customers, lowStock] = await Promise.all([
      one('SELECT COUNT(*)::int AS total, COUNT(*) FILTER (WHERE in_stock)::int AS in_stock FROM products'),
      one(`SELECT COUNT(*)::int AS total,
                  COUNT(*) FILTER (WHERE status = 'created')::int AS created,
                  COUNT(*) FILTER (WHERE status = 'delivered')::int AS delivered
             FROM orders`),
      one('SELECT COALESCE(SUM(total), 0)::float AS sum FROM orders WHERE status <> \'cancelled\''),
      one('SELECT COUNT(*)::int AS total FROM users'),
      many('SELECT id, name, stock FROM products WHERE stock <= 5 ORDER BY stock ASC LIMIT 10'),
    ]);
    res.json({
      products: products.total,
      inStock: products.in_stock,
      orders: orders.total,
      pendingOrders: orders.created,
      deliveredOrders: orders.delivered,
      revenue: revenue.sum,
      customers: customers.total,
      lowStock,
      demo: config.demo,
    });
  }),
);

// ------------------------------- Mahsulotlar --------------------------------

router.get(
  '/products',
  asyncHandler(async (req, res) => {
    const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 50, 1), 200);
    const offset = Math.max(parseInt(req.query.offset, 10) || 0, 0);
    const q = req.query.q ? String(req.query.q).toLowerCase() : '';
    const params = [];
    let where = '';
    if (q) {
      params.push(`%${q}%`);
      where = `WHERE lower(p.name) LIKE $1 OR lower(p.brand) LIKE $1 OR p.id LIKE $1`;
    }
    const rows = await many(
      `${PRODUCT_SELECT} ${where} ORDER BY p.id ASC LIMIT ${limit} OFFSET ${offset}`,
      params,
    );
    const total = await one(
      `SELECT COUNT(*)::int AS total FROM products p ${where}`,
      params,
    );
    res.json({ data: rows.map(productToDto), total: total.total, limit, offset });
  }),
);

router.get(
  '/products/:id',
  asyncHandler(async (req, res) => {
    const row = await one(`${PRODUCT_SELECT} WHERE p.id = $1`, [req.params.id]);
    if (!row) throw notFound('Mahsulot topilmadi');
    res.json({ product: productToDto(row) });
  }),
);

function readProductBody(body, { partial = false } = {}) {
  const fields = {};
  const set = (key, value) => {
    if (value !== undefined) fields[key] = value;
  };

  if (!partial || body.name !== undefined) {
    const name = String(body.name ?? '').trim();
    if (!name) throw badRequest('Mahsulot nomi kerak');
    set('name', name);
  }
  if (!partial || body.categoryId !== undefined) {
    const categoryId = String(body.categoryId ?? '').trim();
    if (!categoryId) throw badRequest('Kategoriya kerak');
    set('category_id', categoryId);
  }
  if (!partial || body.subcategoryId !== undefined) {
    const subcategoryId = String(body.subcategoryId ?? '').trim();
    if (!subcategoryId) throw badRequest('Kichik kategoriya kerak');
    set('subcategory_id', subcategoryId);
  }
  if (!partial || body.price !== undefined) {
    const price = Number(body.price);
    if (!Number.isFinite(price) || price < 0) throw badRequest('Narx noto\'g\'ri');
    set('price', price);
  }
  if (body.brand !== undefined) set('brand', String(body.brand).trim());
  if (body.description !== undefined) set('description', String(body.description).trim());
  if (body.unit !== undefined) set('unit', String(body.unit).trim() || 'dona');
  if (body.unitValue !== undefined) set('unit_value', Number(body.unitValue) || 1);
  if (body.unitLabel !== undefined) set('unit_label', String(body.unitLabel).trim());
  if (body.stock !== undefined) {
    const stock = Number(body.stock);
    if (!Number.isInteger(stock) || stock < 0) throw badRequest('Ombor qoldig\'i noto\'g\'ri');
    set('stock', stock);
    set('in_stock', stock > 0);
  }
  if (body.rating !== undefined) set('rating', Math.min(Math.max(Number(body.rating) || 0, 0), 5));
  if (body.reviewCount !== undefined) set('review_count', Math.max(Number(body.reviewCount) || 0, 0));
  if (body.imageUrl !== undefined) set('image_url', String(body.imageUrl).trim());
  if (body.imageAssetPath !== undefined) set('image_asset_path', String(body.imageAssetPath).trim());
  if (body.options !== undefined) set('options', JSON.stringify(body.options ?? []));
  if (body.tags !== undefined) set('tags', JSON.stringify(body.tags ?? []));
  if (body.isActive !== undefined) set('is_active', Boolean(body.isActive));

  if (body.discountPrice !== undefined) {
    const discount = body.discountPrice === null || body.discountPrice === '' ? null : Number(body.discountPrice);
    if (discount !== null && (!Number.isFinite(discount) || discount < 0)) {
      throw badRequest('Chegirma narxi noto\'g\'ri');
    }
    fields.discount_price = discount;
  }

  return fields;
}

/** Chegirma foizini narxlardan hisoblab qo'yish. */
function withDiscountPercent(fields, currentPrice) {
  if (fields.discount_price === undefined) return fields;
  const price = fields.price !== undefined ? fields.price : currentPrice;
  const discount = fields.discount_price;
  if (discount === null || !price) {
    fields.discount_percent = 0;
    fields.is_deal = false;
    return fields;
  }
  if (discount >= price) throw badRequest('Chegirma narxi asl narxdan kichik bo\'lishi kerak');
  fields.discount_percent = Math.round((1 - discount / price) * 100);
  fields.is_deal = fields.discount_percent > 0;
  return fields;
}

router.post(
  '/products',
  asyncHandler(async (req, res) => {
    const fields = withDiscountPercent(readProductBody(req.body ?? {}), null);
    const id = String(req.body?.id ?? '').trim() || `prd-${Date.now().toString(36)}`;
    const exists = await one('SELECT 1 FROM products WHERE id = $1', [id]);
    if (exists) throw conflict('Bu ID bilan mahsulot mavjud');

    const columns = ['id', ...Object.keys(fields)];
    const values = [id, ...Object.values(fields)];
    const placeholders = values.map((_, index) => `$${index + 1}`).join(', ');
    const row = await one(
      `INSERT INTO products (${columns.join(', ')}) VALUES (${placeholders}) RETURNING *`,
      values,
    );
    res.status(201).json({ product: productToDto(row) });
  }),
);

router.put(
  '/products/:id',
  asyncHandler(async (req, res) => {
    const current = await one('SELECT * FROM products WHERE id = $1', [req.params.id]);
    if (!current) throw notFound('Mahsulot topilmadi');
    const fields = withDiscountPercent(readProductBody(req.body ?? {}, { partial: true }), Number(current.price));
    if (Object.keys(fields).length === 0) throw badRequest('Yangilash uchun maydon yo\'q');

    const assignments = Object.keys(fields).map((key, index) => `${key} = $${index + 1}`).join(', ');
    const values = [...Object.values(fields), req.params.id];
    const row = await one(
      `UPDATE products SET ${assignments} WHERE id = $${values.length} RETURNING *`,
      values,
    );
    res.json({ product: productToDto(row) });
  }),
);

router.delete(
  '/products/:id',
  asyncHandler(async (req, res) => {
    const result = await query('DELETE FROM products WHERE id = $1', [req.params.id]);
    if (result.rowCount === 0) throw notFound('Mahsulot topilmadi');
    res.json({ success: true });
  }),
);

/** POST /admin/products/:id/stock  { stock } */
router.post(
  '/products/:id/stock',
  asyncHandler(async (req, res) => {
    const stock = Number(req.body?.stock);
    if (!Number.isInteger(stock) || stock < 0) throw badRequest('Ombor qoldig\'i noto\'g\'ri');
    const row = await one(
      'UPDATE products SET stock = $2, in_stock = ($2 > 0) WHERE id = $1 RETURNING *',
      [req.params.id, stock],
    );
    if (!row) throw notFound('Mahsulot topilmadi');
    res.json({ product: productToDto(row) });
  }),
);

/** POST /admin/products/:id/discount  { discountPrice | null } */
router.post(
  '/products/:id/discount',
  asyncHandler(async (req, res) => {
    const current = await one('SELECT * FROM products WHERE id = $1', [req.params.id]);
    if (!current) throw notFound('Mahsulot topilmadi');
    const raw = req.body?.discountPrice;
    const discount = raw === null || raw === '' ? null : Number(raw);
    if (discount !== null && (!Number.isFinite(discount) || discount < 0)) {
      throw badRequest('Chegirma narxi noto\'g\'ri');
    }
    const price = Number(current.price);
    if (discount !== null && discount >= price) {
      throw badRequest('Chegirma narxi asl narxdan kichik bo\'lishi kerak');
    }
    const percent = discount === null ? 0 : Math.round((1 - discount / price) * 100);
    const row = await one(
      `UPDATE products
          SET discount_price = $2, discount_percent = $3, is_deal = $4
        WHERE id = $1 RETURNING *`,
      [req.params.id, discount, percent, percent > 0],
    );
    res.json({ product: productToDto(row) });
  }),
);

/** POST /admin/upload — rasm yuklash (JPEG/PNG/WEBP, ≤ 5 MB). */
router.post('/upload', (req, res, next) => {
  upload.single('image')(req, res, (error) => {
    if (error) return next(badRequest(error.message));
    if (!req.file) return next(badRequest('Rasm tanlanmagan'));
    return res.status(201).json({
      url: `/uploads/${req.file.filename}`,
      size: req.file.size,
      contentType: req.file.mimetype,
    });
  });
});

// ------------------------------- Kategoriyalar ------------------------------

router.get(
  '/categories',
  asyncHandler(async (_req, res) => {
    const categories = await many('SELECT * FROM categories ORDER BY sort_order, name');
    const subs = await many('SELECT * FROM subcategories ORDER BY sort_order, name');
    res.json({
      categories: categories.map((category) =>
        categoryToDto(category, subs.filter((sub) => sub.category_id === category.id)),
      ),
    });
  }),
);

router.post(
  '/categories',
  asyncHandler(async (req, res) => {
    const id = String(req.body?.id ?? '').trim();
    const name = String(req.body?.name ?? '').trim();
    if (!id || !name) throw badRequest('ID va nom kerak');
    const exists = await one('SELECT 1 FROM categories WHERE id = $1', [id]);
    if (exists) throw conflict('Bu kategoriya mavjud');
    const row = await one(
      `INSERT INTO categories (id, name, icon, color, sort_order)
       VALUES ($1, $2, $3, $4, $5) RETURNING *`,
      [id, name, String(req.body?.icon ?? 'box'), String(req.body?.color ?? '#4CAF50'), Number(req.body?.sortOrder ?? 0)],
    );
    res.status(201).json({ category: categoryToDto(row, []) });
  }),
);

router.put(
  '/categories/:id',
  asyncHandler(async (req, res) => {
    const current = await one('SELECT * FROM categories WHERE id = $1', [req.params.id]);
    if (!current) throw notFound('Kategoriya topilmadi');
    const row = await one(
      `UPDATE categories
          SET name = $2, icon = $3, color = $4, sort_order = $5
        WHERE id = $1 RETURNING *`,
      [
        req.params.id,
        String(req.body?.name ?? current.name),
        String(req.body?.icon ?? current.icon),
        String(req.body?.color ?? current.color),
        Number(req.body?.sortOrder ?? current.sort_order),
      ],
    );
    const subs = await many('SELECT * FROM subcategories WHERE category_id = $1 ORDER BY sort_order', [row.id]);
    res.json({ category: categoryToDto(row, subs) });
  }),
);

router.delete(
  '/categories/:id',
  asyncHandler(async (req, res) => {
    const used = await one('SELECT COUNT(*)::int AS total FROM products WHERE category_id = $1', [req.params.id]);
    if (used && used.total > 0) {
      throw conflict(`Bu kategoriyada ${used.total} ta mahsulot bor — avval ularni o'chiring`);
    }
    const result = await query('DELETE FROM categories WHERE id = $1', [req.params.id]);
    if (result.rowCount === 0) throw notFound('Kategoriya topilmadi');
    res.json({ success: true });
  }),
);

// ---------------------------------- Bannerlar --------------------------------

router.get(
  '/banners',
  asyncHandler(async (_req, res) => {
    const rows = await many('SELECT * FROM banners ORDER BY sort_order, id');
    res.json({ banners: rows.map(bannerToDto) });
  }),
);

router.post(
  '/banners',
  asyncHandler(async (req, res) => {
    const title = String(req.body?.title ?? '').trim();
    if (!title) throw badRequest('Banner sarlavhasi kerak');
    const id = String(req.body?.id ?? '').trim() || `bnr-${Date.now().toString(36)}`;
    const row = await one(
      `INSERT INTO banners (id, title, subtitle, image_url, action_type, action_value, start_color, end_color, is_active, sort_order)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) RETURNING *`,
      [
        id,
        title,
        String(req.body?.subtitle ?? ''),
        String(req.body?.imageUrl ?? ''),
        String(req.body?.actionType ?? 'category'),
        String(req.body?.actionValue ?? ''),
        String(req.body?.startColor ?? '#2E7D32'),
        String(req.body?.endColor ?? '#66BB6A'),
        req.body?.isActive !== false,
        Number(req.body?.sortOrder ?? 0),
      ],
    );
    res.status(201).json({ banner: bannerToDto(row) });
  }),
);

router.put(
  '/banners/:id',
  asyncHandler(async (req, res) => {
    const current = await one('SELECT * FROM banners WHERE id = $1', [req.params.id]);
    if (!current) throw notFound('Banner topilmadi');
    const row = await one(
      `UPDATE banners
          SET title = $2, subtitle = $3, image_url = $4, action_type = $5,
              action_value = $6, start_color = $7, end_color = $8, is_active = $9, sort_order = $10
        WHERE id = $1 RETURNING *`,
      [
        current.id,
        String(req.body?.title ?? current.title),
        String(req.body?.subtitle ?? current.subtitle),
        String(req.body?.imageUrl ?? current.image_url),
        String(req.body?.actionType ?? current.action_type),
        String(req.body?.actionValue ?? current.action_value),
        String(req.body?.startColor ?? current.start_color),
        String(req.body?.endColor ?? current.end_color),
        req.body?.isActive === undefined ? current.is_active : Boolean(req.body.isActive),
        Number(req.body?.sortOrder ?? current.sort_order),
      ],
    );
    res.json({ banner: bannerToDto(row) });
  }),
);

router.delete(
  '/banners/:id',
  asyncHandler(async (req, res) => {
    const result = await query('DELETE FROM banners WHERE id = $1', [req.params.id]);
    if (result.rowCount === 0) throw notFound('Banner topilmadi');
    res.json({ success: true });
  }),
);

// ---------------------------------- Buyurtmalar ------------------------------

router.get(
  '/orders',
  asyncHandler(async (req, res) => {
    const status = req.query.status ? String(req.query.status) : null;
    const rows = status
      ? await many('SELECT * FROM orders WHERE status = $1 ORDER BY created_at DESC LIMIT 200', [status])
      : await many('SELECT * FROM orders ORDER BY created_at DESC LIMIT 200');
    const orders = [];
    for (const row of rows) {
      const items = await many('SELECT * FROM order_items WHERE order_id = $1 ORDER BY id', [row.id]);
      orders.push(orderToDto(row, items.map(orderItemToDto)));
    }
    res.json({ orders, statuses: ORDER_STATUSES });
  }),
);

router.patch(
  '/orders/:id/status',
  asyncHandler(async (req, res) => {
    const status = String(req.body?.status ?? '');
    if (!ORDER_STATUSES.includes(status)) throw badRequest('Holat noto\'g\'ri');
    const current = await one('SELECT * FROM orders WHERE id = $1', [req.params.id]);
    if (!current) throw notFound('Buyurtma topilmadi');
    if (!canTransition(current.status, status)) {
      throw conflict(`"${current.status}" dan "${status}" ga o'tish mumkin emas`);
    }

    await transaction(async (client) => {
      await client.query('UPDATE orders SET status = $2 WHERE id = $1', [current.id, status]);
      // Bekor qilinganda omborga qaytarish
      if (status === 'cancelled') {
        const { rows } = await client.query('SELECT * FROM order_items WHERE order_id = $1', [current.id]);
        for (const item of rows) {
          await client.query(
            'UPDATE products SET stock = stock + $2, in_stock = TRUE WHERE id = $1',
            [item.product_id, item.quantity],
          );
        }
      }
    });

    const items = await many('SELECT * FROM order_items WHERE order_id = $1 ORDER BY id', [current.id]);
    const updated = await one('SELECT * FROM orders WHERE id = $1', [current.id]);
    res.json({ order: orderToDto(updated, items.map(orderItemToDto)) });
  }),
);

// ---------------------------------- Mijozlar ---------------------------------

router.get(
  '/customers',
  asyncHandler(async (_req, res) => {
    const rows = await many(`
      SELECT u.id, u.name, u.phone, u.created_at,
             COUNT(o.id)::int AS orders_count,
             COALESCE(SUM(o.total) FILTER (WHERE o.status <> 'cancelled'), 0)::float AS total_spent
        FROM users u
        LEFT JOIN orders o ON o.user_id = u.id
       GROUP BY u.id
       ORDER BY u.created_at DESC
       LIMIT 500
    `);
    res.json({ customers: rows });
  }),
);

export default router;
