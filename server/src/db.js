import pg from 'pg';
import { config } from './config.js';

export const pool = new pg.Pool({
  connectionString: config.databaseUrl,
  max: 10,
  idleTimeoutMillis: 30_000,
});

pool.on('error', (error) => {
  console.error('[db] kutilmagan xatolik:', error.message);
});

export async function query(text, params = []) {
  const result = await pool.query(text, params);
  return result;
}

export async function one(text, params = []) {
  const result = await pool.query(text, params);
  return result.rows[0] || null;
}

export async function many(text, params = []) {
  const result = await pool.query(text, params);
  return result.rows;
}

/** Tranzaksiya: callback ichida `client` bilan ishlang. */
export async function transaction(fn) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await fn(client);
    await client.query('COMMIT');
    return result;
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

export async function healthcheck() {
  const row = await one('SELECT 1 AS ok');
  return Boolean(row);
}
