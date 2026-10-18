/**
 * Yetkazib berish narxi va buyurtma summasi hisobi.
 *
 * Android ilovadagi `CartCalculator` bilan bir xil qoidalar:
 *   pickup  -> 0
 *   express -> 25 000
 *   standard-> 15 000 (agar 100 000 dan kam bo'lsa), aks holda 0
 * Savat bo'sh bo'lsa yetkazib berish bepul (0).
 */
export const FREE_DELIVERY_THRESHOLD = 100_000;
export const DELIVERY_FEE = 15_000;
export const EXPRESS_DELIVERY_FEE = 25_000;

const METHODS = new Set(['standard', 'express', 'pickup']);

export function deliveryFeeFor(itemsTotal, deliveryMethod = 'standard') {
  if (!METHODS.has(deliveryMethod)) {
    throw new Error(`Noto'g'ri yetkazib berish usuli: ${deliveryMethod}`);
  }
  if (deliveryMethod === 'pickup') return 0;
  if (deliveryMethod === 'express') return EXPRESS_DELIVERY_FEE;
  if (itemsTotal <= 0) return 0;
  return itemsTotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
}

/** Chegirma hisobga olingan narx. */
export function effectivePrice(row) {
  const discount = row.discount_price === null ? null : Number(row.discount_price);
  const price = Number(row.price);
  return discount !== null && discount > 0 && discount < price ? discount : price;
}

/**
 * Savat tarkibini qayta hisoblash (mijoz yuborgan narxlar hisobga olinmaydi).
 * @param {Array<{row: object, quantity: number}>} lines
 */
export function calculateTotals(lines, deliveryMethod) {
  let listTotal = 0;
  let payable = 0;
  let itemsCount = 0;

  for (const line of lines) {
    const price = Number(line.row.price);
    const unit = effectivePrice(line.row);
    const quantity = line.quantity;
    listTotal += price * quantity;
    payable += unit * quantity;
    itemsCount += quantity;
  }

  const discount = Math.max(listTotal - payable, 0);
  const deliveryFee = deliveryFeeFor(payable, deliveryMethod);
  return {
    itemsCount,
    listTotal: round2(listTotal),
    itemsTotal: round2(payable),
    discount: round2(discount),
    deliveryFee: round2(deliveryFee),
    total: round2(payable + deliveryFee),
  };
}

export function round2(value) {
  return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
}
