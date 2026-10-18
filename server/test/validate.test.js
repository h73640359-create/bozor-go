import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  canTransition,
  isValidPhone,
  isValidQuantity,
  ORDER_STATUSES,
  totalsMatch,
  validateOrderInput,
} from '../src/validate.js';

const validBody = {
  recipientName: 'Ali Valiyev',
  phone: '+998 90 123 45 67',
  addressText: 'Toshkent, Yunusobod, 5-uy',
  deliveryMethod: 'standard',
  paymentMethod: 'cash',
  items: [{ productId: 'prd-0001', quantity: 2 }],
};

test('telefon raqami tekshiruvi', () => {
  assert.equal(isValidPhone('+998 90 123 45 67'), true);
  assert.equal(isValidPhone('998901234567'), true);
  assert.equal(isValidPhone('90 123 45 67'), true);
  assert.equal(isValidPhone('123'), false);
  assert.equal(isValidPhone(''), false);
  assert.equal(isValidPhone('+998901234567890'), false);
});

test('miqdor 1..99 oralig\'ida', () => {
  assert.equal(isValidQuantity(1), true);
  assert.equal(isValidQuantity(99), true);
  assert.equal(isValidQuantity(0), false);
  assert.equal(isValidQuantity(100), false);
  assert.equal(isValidQuantity(1.5), false);
});

test('to\'g\'ri buyurtma xatosiz o\'tadi', () => {
  assert.deepEqual(validateOrderInput(validBody), []);
});

test('bo\'sh savat rad etiladi', () => {
  assert.ok(validateOrderInput({ ...validBody, items: [] }).length > 0);
});

test('noto\'g\'ri miqdor rad etiladi', () => {
  const errors = validateOrderInput({ ...validBody, items: [{ productId: 'p1', quantity: 0 }] });
  assert.ok(errors.some((e) => e.includes('miqdor')));
});

test('takrorlangan mahsulot qatori rad etiladi', () => {
  const errors = validateOrderInput({
    ...validBody,
    items: [
      { productId: 'p1', quantity: 1 },
      { productId: 'p1', quantity: 1 },
    ],
  });
  assert.ok(errors.some((e) => e.includes('takrorlangan')));
});

test('noto\'g\'ri to\'lov/yetkazib berish usuli rad etiladi', () => {
  assert.ok(validateOrderInput({ ...validBody, paymentMethod: 'crypto' }).length > 0);
  assert.ok(validateOrderInput({ ...validBody, deliveryMethod: 'drone' }).length > 0);
});

test('server va mijoz summasi solishtiriladi', () => {
  assert.equal(totalsMatch(1000, 1000), true);
  assert.equal(totalsMatch(1000, 1000.5), true);
  assert.equal(totalsMatch(1000, 1500), false);
  assert.equal(totalsMatch('abc', 1000), false);
});

test('buyurtma holati o\'tishlari', () => {
  assert.equal(canTransition('created', 'confirmed'), true);
  assert.equal(canTransition('confirmed', 'preparing'), true);
  assert.equal(canTransition('preparing', 'courier'), true);
  assert.equal(canTransition('delivering', 'delivered'), true);
  assert.equal(canTransition('created', 'cancelled'), true);
  assert.equal(canTransition('delivered', 'cancelled'), false);
  assert.equal(canTransition('delivered', 'created'), false);
  assert.equal(canTransition('created', 'created'), false);
  assert.equal(canTransition('created', 'unknown'), false);
  assert.equal(ORDER_STATUSES.length, 7);
});
