import { Router } from 'express';
import { many, one } from '../db.js';
import { notFound } from '../http.js';
import { bannerToDto, categoryToDto, productToDto } from '../serialize.js';

const PRODUCT_SELECT = `
  SELECT p.*, c.name AS category_name, s.name AS subcategory_name
    FROM products p
    JOIN categories c ON c.id = p.category_id
    JOIN subcategories s ON s.id = p.subcategory_id
`;

const router = Router();

/** GET /products — sahifalab olish (qidiruv/filtr ham qo'llab-quvvatlanadi). */
router.get('/products', async (req, res, next) => {
  try {
    const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 100, 1), 500);
    const offset = Math.max(parseInt(req.query.offset, 10) || 0, 0);
    const {
      categoryId,
      subcategoryId,
      q,
      discount,
      sort,
      updatedAfter,
    } = req.query;

    const where = ['p.is_active = TRUE'];
    const params = [];

    if (categoryId) {
      params.push(categoryId);
      where.push(`p.category_id = $${params.length}`);
    }
    if (subcategoryId) {
      params.push(subcategoryId);
      where.push(`p.subcategory_id = $${params.length}`);
    }
    if (discount === '1' || discount === 'true') {
      where.push('p.discount_price IS NOT NULL');
    }
    if (q) {
      params.push(`%${String(q).toLowerCase()}%`);
      const idx = params.length;
      where.push(`(lower(p.name) LIKE $${idx} OR lower(p.brand) LIKE $${idx} OR lower(p.description) LIKE $${idx})`);
    }
    if (updatedAfter) {
      params.push(updatedAfter);
      where.push(`p.updated_at > $${params.length}`);
    }

    const orderBy = {
      price_asc: 'p.price ASC',
      price_desc: 'p.price DESC',
      popular: 'p.rating DESC, p.review_count DESC',
      newest: 'p.created_at DESC',
      discount: 'p.discount_percent DESC',
    }[String(sort)] || 'p.id ASC';

    const clause = where.length ? `WHERE ${where.join(' AND ')}` : '';
    const rows = await many(
      `${PRODUCT_SELECT} ${clause} ORDER BY ${orderBy} LIMIT ${limit} OFFSET ${offset}`,
      params,
    );
    const totalRow = await one(
      `SELECT COUNT(*)::int AS total FROM products p ${clause}`,
      params,
    );

    res.json({
      data: rows.map(productToDto),
      total: totalRow?.total ?? rows.length,
      limit,
      offset,
    });
  } catch (error) {
    next(error);
  }
});

/** GET /products/:id */
router.get('/products/:id', async (req, res, next) => {
  try {
    const row = await one(`${PRODUCT_SELECT} WHERE p.id = $1`, [req.params.id]);
    if (!row) return next(notFound('Mahsulot topilmadi'));
    return res.json(productToDto(row));
  } catch (error) {
    return next(error);
  }
});

/** GET /categories */
router.get('/categories', async (_req, res, next) => {
  try {
    const categories = await many(`
      SELECT c.*,
             (SELECT COUNT(*) FROM products p WHERE p.category_id = c.id AND p.is_active)::int AS product_count
        FROM categories c ORDER BY c.sort_order, c.name
    `);
    const subs = await many(`
      SELECT s.*,
             (SELECT COUNT(*) FROM products p WHERE p.subcategory_id = s.id AND p.is_active)::int AS product_count
        FROM subcategories s ORDER BY s.sort_order, s.name
    `);
    res.json({
      categories: categories.map((category) =>
        categoryToDto(category, subs.filter((sub) => sub.category_id === category.id)),
      ),
    });
  } catch (error) {
    next(error);
  }
});

/** GET /banners */
router.get('/banners', async (_req, res, next) => {
  try {
    const rows = await many(
      'SELECT * FROM banners WHERE is_active = TRUE ORDER BY sort_order, id',
    );
    res.json({ banners: rows.map(bannerToDto) });
  } catch (error) {
    next(error);
  }
});

export default router;
