#!/usr/bin/env node
/**
 * data/catalog.json, categories.json va banners.json ni bazaga yozish.
 *
 * Ishga tushirish:  npm run seed
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import 'dotenv/config';
import { pool, transaction } from '../src/db.js';
import { ensureAdminUser } from '../src/auth.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = path.resolve(__dirname, '..', '..', 'data');

function readJson(name) {
  const file = path.join(DATA_DIR, name);
  if (!fs.existsSync(file)) {
    throw new Error(`${file} topilmadi. Avval: python3 data/scripts/build_catalog.py`);
  }
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

async function main() {
  const catalog = readJson('catalog.json');
  const categoriesFile = readJson('categories.json');
  const bannersFile = readJson('banners.json');

  console.log(`Seed: ${catalog.products.length} ta mahsulot, ${categoriesFile.categories.length} ta kategoriya`);

  await transaction(async (client) => {
    await client.query('TRUNCATE order_items, orders, addresses, otp_codes, banners, products, subcategories, categories CASCADE');

    for (const [index, category] of categoriesFile.categories.entries()) {
      await client.query(
        `INSERT INTO categories (id, name, icon, color, sort_order)
         VALUES ($1,$2,$3,$4,$5) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name`,
        [category.id, category.name, category.icon, category.color, index],
      );
      for (const [subIndex, sub] of category.subcategories.entries()) {
        await client.query(
          `INSERT INTO subcategories (id, category_id, name, icon, sort_order)
           VALUES ($1,$2,$3,$4,$5) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name`,
          [sub.id, category.id, sub.name, sub.icon, subIndex],
        );
      }
    }

    for (const product of catalog.products) {
      await client.query(
        `INSERT INTO products (
            id, name, category_id, subcategory_id, brand, description,
            price, discount_price, discount_percent, unit, unit_value, unit_label,
            stock, in_stock, rating, review_count, is_popular, is_new, is_deal,
            image_asset_path, image_url, options, tags, is_demo, created_at
         ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25)
         ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            price = EXCLUDED.price,
            discount_price = EXCLUDED.discount_price,
            discount_percent = EXCLUDED.discount_percent,
            stock = EXCLUDED.stock,
            in_stock = EXCLUDED.in_stock,
            updated_at = now()`,
        [
          product.id,
          product.name,
          product.categoryId,
          product.subcategoryId,
          product.brand,
          product.description,
          product.price,
          product.discountPrice ?? null,
          product.discountPercent ?? 0,
          product.unit,
          product.unitValue ?? 1,
          product.unitLabel,
          product.stock,
          product.inStock,
          product.rating,
          product.reviewCount,
          Boolean(product.isPopular),
          Boolean(product.isNew),
          Boolean(product.isDeal),
          product.image,
          product.imageUrl ?? '',
          JSON.stringify(product.options ?? []),
          JSON.stringify(product.tags ?? []),
          true,
          product.createdAt || new Date().toISOString(),
        ],
      );
    }

    for (const [index, banner] of bannersFile.banners.entries()) {
      await client.query(
        `INSERT INTO banners (id, title, subtitle, image_url, action_type, action_value, start_color, end_color, is_active, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,TRUE,$9)
         ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title`,
        [banner.id, banner.title, banner.subtitle, banner.image, banner.actionType, banner.actionValue, banner.startColor, banner.endColor, index],
      );
    }
  });

  const count = await pool.query('SELECT COUNT(*)::int AS total FROM products');
  console.log(`Bazadagi mahsulotlar: ${count.rows[0].total}`);
  const admin = await ensureAdminUser();
  console.log(`Admin: ${admin.email} (parol: .env dagi ADMIN_PASSWORD)`);
}

main()
  .then(() => pool.end())
  .catch(async (error) => {
    console.error('Seed xatolik:', error.message);
    await pool.end();
    process.exit(1);
  });
