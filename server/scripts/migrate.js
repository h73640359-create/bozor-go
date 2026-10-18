#!/usr/bin/env node
/**
 * Sxemani qo'llash:  node scripts/migrate.js [--drop]
 * --drop  → barcha jadvallarni o'chirib, sxemani qayta yaratadi (faqat dev).
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import 'dotenv/config';
import { pool, query } from '../src/db.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const schemaPath = path.resolve(__dirname, '..', 'db', 'schema.sql');

const DROP_SQL = `
DROP TABLE IF EXISTS order_items, orders, addresses, otp_codes,
  admin_users, users, banners, products, subcategories, categories CASCADE;
DROP FUNCTION IF EXISTS set_updated_at() CASCADE;
`;

try {
  if (process.argv.includes('--drop')) {
    console.log('Jadvallar o\'chirilmoqda…');
    await query(DROP_SQL);
  }
  const sql = fs.readFileSync(schemaPath, 'utf8');
  await pool.query(sql);
  const tables = await query(
    `SELECT table_name FROM information_schema.tables
      WHERE table_schema = 'public' ORDER BY table_name`,
  );
  console.log('Sxema tayyor. Jadvallar:', tables.rows.map((r) => r.table_name).join(', '));
} catch (error) {
  console.error('Migratsiya xatolik:', error.message);
  process.exitCode = 1;
} finally {
  await pool.end();
}
