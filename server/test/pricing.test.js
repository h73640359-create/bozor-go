import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  DELIVERY_FEE,
  EXPRESS_DELIVERY_FEE,
  FREE_DELIVERY_THRESHOLD,
  calculateTotals,
  deliveryFeeFor,
  effectivePrice,
} from '../src/pricing.js';

const row = (price, discount = null) => ({ price, discount_price: discount });

test('yetkazib berish narxi qoidalari', () => {
  assert.equal(deliveryFeeFor(0, 'standard'), 0);
  assert.equal(deliveryFeeFor(50_000, 'standard'), DELIVERY_FEE);
  assert.equal(deliveryFeeFor(FREE_DELIVERY_THRESHOLD, 'standard'), 0);
  assert.equal(deliveryFeeFor(50_000, 'express'), EXPRESS_DELIVERY_FEE);
  assert.equal(deliveryFeeFor(50_000, 'pickup'), 0);
  assert.throws(() => deliveryFeeFor(50_000, 'drone'));
});

test('chegirma hisobga olingan narx', () => {
  assert.equal(effectivePrice(row(100, 70)), 70);
  assert.equal(effectivePrice(row(100, null)), 100);
  assert.equal(effectivePrice(row(100, 120)), 100, 'chegirma asl narxdan katta bo\'lsa e\'tiborga olinmaydi');
});

test('savat summasi server tomonidan qayta hisoblanadi', () => {
  const totals = calculateTotals(
    [
      { row: row(20_000, 15_000), quantity: 2 },
      { row: row(10_000), quantity: 1 },
    ],
    'pickup',
  );
  assert.equal(totals.itemsCount, 3);
  assert.equal(totals.listTotal, 50_000);
  assert.equal(totals.itemsTotal, 40_000);
  assert.equal(totals.discount, 10_000);
  assert.equal(totals.deliveryFee, 0);
  assert.equal(totals.total, 40_000);
});

test('yetkazib berish summaga qo\'shiladi', () => {
  const totals = calculateTotals([{ row: row(60_000), quantity: 1 }], 'express');
  assert.equal(totals.total, 60_000 + EXPRESS_DELIVERY_FEE);
});

test('bo\'sh savat uchun yetkazib berish bepul', () => {
  const totals = calculateTotals([], 'standard');
  assert.equal(totals.total, 0);
  assert.equal(totals.deliveryFee, 0);
});
