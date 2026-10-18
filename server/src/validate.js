/**
 * Kiruvchi ma'lumotlarni tekshirish (sof funksiyalar — DB talab qilmaydi,
 * `npm test` bilan sinovdan o'tkaziladi).
 */

export const PHONE_DIGITS_MIN = 9;
export const PHONE_DIGITS_MAX = 12;
export const MAX_QUANTITY = 99;
export const DELIVERY_METHODS = ['standard', 'express', 'pickup'];
export const PAYMENT_METHODS = ['cash', 'card'];
export const ORDER_STATUSES = [
  'created',
  'confirmed',
  'preparing',
  'courier',
  'delivering',
  'delivered',
  'cancelled',
];

export function normalizePhone(value) {
  return String(value ?? '').replace(/\D/g, '');
}

export function isValidPhone(value) {
  const digits = normalizePhone(value);
  return digits.length >= PHONE_DIGITS_MIN && digits.length <= PHONE_DIGITS_MAX;
}

export function isValidQuantity(value) {
  return Number.isInteger(value) && value > 0 && value <= MAX_QUANTITY;
}

/** Mijoz kiritgan ma'lumotlarni tekshiradi, xatoliklar ro'yxatini qaytaradi. */
export function validateOrderInput(body) {
  const errors = [];

  if (!body || typeof body !== 'object') {
    return ['So\'rov tanasi bo\'sh'];
  }

  const recipientName = String(body.recipientName ?? '').trim();
  if (recipientName.length < 2) errors.push('Ism kamida 2 belgi bo\'lishi kerak');
  if (recipientName.length > 120) errors.push('Ism juda uzun');

  if (!isValidPhone(body.phone)) errors.push('Telefon raqami noto\'g\'ri');

  const addressText = String(body.addressText ?? '').trim();
  if (addressText.length < 5) errors.push('Manzil to\'liq kiritilmagan');

  if (!DELIVERY_METHODS.includes(body.deliveryMethod)) {
    errors.push('Yetkazib berish usuli noto\'g\'ri');
  }
  if (!PAYMENT_METHODS.includes(body.paymentMethod)) {
    errors.push('To\'lov usuli noto\'g\'ri');
  }

  const items = Array.isArray(body.items) ? body.items : [];
  if (items.length === 0) errors.push('Savat bo\'sh');
  if (items.length > 200) errors.push('Savatda juda ko\'p mahsulot');

  const seen = new Map();
  items.forEach((item, index) => {
    const productId = String(item?.productId ?? '').trim();
    if (!productId) errors.push(`#${index + 1} mahsulot: id ko'rsatilmagan`);
    if (!isValidQuantity(item?.quantity)) {
      errors.push(`#${index + 1} mahsulot: miqdor 1..${MAX_QUANTITY} oralig'ida bo'lishi kerak`);
    }
    const key = `${productId}|${item?.selectedOption ?? ''}`;
    if (seen.has(key)) errors.push(`#${index + 1} mahsulot: takrorlangan qator`);
    seen.set(key, true);
  });

  if (typeof body.comment === 'string' && body.comment.length > 1000) {
    errors.push('Izoh juda uzun');
  }

  return errors;
}

/**
 * Server hisoblagan summa bilan mijoz yuborgan summani solishtirish.
 * Farq 1 so'mdan oshsa — buyurtma rad etiladi.
 */
export function totalsMatch(clientTotal, serverTotal, tolerance = 1) {
  const a = Number(clientTotal);
  const b = Number(serverTotal);
  if (!Number.isFinite(a) || !Number.isFinite(b)) return false;
  return Math.abs(a - b) <= tolerance;
}

/** Buyurtma holatini o'zgartirish mumkinmi (faqat admin). */
export function canTransition(from, to) {
  if (!ORDER_STATUSES.includes(from) || !ORDER_STATUSES.includes(to)) return false;
  if (from === to) return false;
  if (from === 'delivered' || from === 'cancelled') return false;
  if (to === 'cancelled') return ['created', 'confirmed', 'preparing'].includes(from);
  const flow = ORDER_STATUSES.filter((s) => s !== 'cancelled');
  return flow.indexOf(to) >= flow.indexOf(from);
}
