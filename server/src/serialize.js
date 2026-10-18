/** DB qatorlarini Android ilovasi kutgan JSON shakliga o'tkazish. */

const num = (value) => (value === null || value === undefined ? null : Number(value));

export function productToDto(row) {
  return {
    id: row.id,
    name: row.name,
    categoryId: row.category_id,
    categoryName: row.category_name || '',
    subcategoryId: row.subcategory_id,
    subcategoryName: row.subcategory_name || '',
    brand: row.brand,
    image: row.image_asset_path,
    imageUrl: row.image_url,
    description: row.description,
    price: num(row.price),
    discountPrice: num(row.discount_price),
    discountPercent: Number(row.discount_percent || 0),
    unit: row.unit,
    unitValue: num(row.unit_value),
    unitLabel: row.unit_label,
    stock: Number(row.stock),
    inStock: Boolean(row.in_stock),
    rating: num(row.rating),
    reviewCount: Number(row.review_count || 0),
    isPopular: Boolean(row.is_popular),
    isNew: Boolean(row.is_new),
    isDeal: Boolean(row.is_deal),
    options: row.options || [],
    tags: row.tags || [],
    createdAt: row.created_at ? new Date(row.created_at).toISOString() : '',
    isDemo: Boolean(row.is_demo),
  };
}

export function categoryToDto(row, subcategories = []) {
  return {
    id: row.id,
    name: row.name,
    icon: row.icon,
    color: row.color,
    productCount: Number(row.product_count || 0),
    subcategories: subcategories.map((sub) => ({
      id: sub.id,
      name: sub.name,
      icon: sub.icon,
      productCount: Number(sub.product_count || 0),
    })),
  };
}

export function bannerToDto(row) {
  return {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    image: row.image_url || row.image_asset_path || '',
    actionType: row.action_type,
    actionValue: row.action_value,
    startColor: row.start_color,
    endColor: row.end_color,
  };
}

export function orderToDto(row, items = []) {
  return {
    id: row.id,
    orderNumber: row.order_number,
    status: row.status,
    createdAt: row.created_at ? new Date(row.created_at).toISOString() : '',
    items,
    itemsTotal: num(row.items_total),
    discount: num(row.discount),
    deliveryFee: num(row.delivery_fee),
    total: num(row.total),
    deliveryMethod: row.delivery_method,
    paymentMethod: row.payment_method,
    recipientName: row.recipient_name,
    phone: row.phone,
    addressTitle: row.address_title,
    addressText: row.address_text,
    comment: row.comment,
    isPaid: Boolean(row.is_paid),
  };
}

export function orderItemToDto(row) {
  return {
    productId: row.product_id,
    productName: row.product_name,
    quantity: Number(row.quantity),
    unitPrice: num(row.unit_price),
    discountPrice: num(row.discount_price),
    unit: row.unit,
    imageUrl: row.image_url,
    selectedOption: row.selected_option,
  };
}
