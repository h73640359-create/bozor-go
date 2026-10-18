// BozorGo admin paneli — oddiy, qaramligi yo'q (vanilla JS) SPA.
// Barcha yozuv/o'chirish so'rovlari /api/admin/* ga JWT bilan yuboriladi,
// huquqni esa SERVER tekshiradi (requireAdmin).

const API = '/api';
const TOKEN_KEY = 'bozorgo_admin_token';

const state = {
  token: localStorage.getItem(TOKEN_KEY) || '',
  products: { offset: 0, limit: 50, q: '' },
  categories: [],
  orderStatus: '',
};

const el = (id) => document.getElementById(id);
const money = (value) => `${Number(value).toLocaleString('uz-UZ')} so'm`;

function toast(message, isError = false) {
  const node = el('toast');
  node.textContent = message;
  node.style.background = isError ? '#e5484d' : '#16181d';
  node.hidden = false;
  setTimeout(() => { node.hidden = true; }, 3500);
}

async function api(path, { method = 'GET', body } = {}) {
  const response = await fetch(`${API}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${state.token}`,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : {};
  if (!response.ok) {
    if (response.status === 401 || response.status === 403) logout();
    throw new Error(data?.error?.message || `HTTP ${response.status}`);
  }
  return data;
}

async function upload(file) {
  const form = new FormData();
  form.append('image', file);
  const response = await fetch(`${API}/admin/upload`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${state.token}` },
    body: form,
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data?.error?.message || 'Yuklab bo\'lmadi');
  return data.url;
}

// --------------------------------- Kirish ----------------------------------

async function login(event) {
  event.preventDefault();
  const email = el('login-email').value.trim();
  const password = el('login-password').value;
  el('login-error').textContent = '';
  try {
    const data = await fetch(`${API}/auth/admin/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }).then((r) => r.json().then((json) => ({ ok: r.ok, json })));
    if (!data.ok) throw new Error(data.json?.error?.message || 'Kirib bo\'lmadi');
    state.token = data.json.token;
    localStorage.setItem(TOKEN_KEY, state.token);
    await bootstrap();
  } catch (error) {
    el('login-error').textContent = error.message;
  }
}

function logout() {
  state.token = '';
  localStorage.removeItem(TOKEN_KEY);
  el('app').hidden = true;
  el('login').hidden = false;
}

// ------------------------------- Boshqaruv ---------------------------------

async function loadStats() {
  const stats = await api('/admin/stats');
  const cards = [
    ['Mahsulotlar', stats.products],
    ['Omborda mavjud', stats.inStock],
    ['Buyurtmalar', stats.orders],
    ['Kutilayotgan', stats.pendingOrders],
    ['Yetkazilgan', stats.deliveredOrders],
    ['Tushum (demo)', `${Math.round(stats.revenue).toLocaleString('uz-UZ')} so'm`],
    ['Mijozlar', stats.customers],
  ];
  el('stats').innerHTML = cards
    .map(([label, value]) => `<div class="card"><div class="label">${label}</div><div class="value">${value}</div></div>`)
    .join('');
  el('low-stock').innerHTML = `<tr><th>Mahsulot</th><th>Ombor</th></tr>${stats.lowStock
    .map((row) => `<tr><td>${row.name}</td><td>${row.stock}</td></tr>`)
    .join('')}`;
}

// ------------------------------ Mahsulotlar --------------------------------

async function loadProducts() {
  const { q, limit, offset } = state.products;
  const data = await api(`/admin/products?limit=${limit}&offset=${offset}&q=${encodeURIComponent(q)}`);
  const rows = data.data.map((product) => `
    <tr>
      <td>${product.id}</td>
      <td>${product.name}</td>
      <td>${product.categoryId}</td>
      <td>${money(product.price)}</td>
      <td>${product.discountPrice ? `${money(product.discountPrice)} (-${product.discountPercent}%)` : '—'}</td>
      <td><span class="badge ${product.stock === 0 ? 'bad' : product.stock <= 5 ? 'warn' : 'ok'}">${product.stock}</span></td>
      <td>
        <button class="ghost" data-edit="${product.id}">Tahrirlash</button>
        <button class="danger" data-delete="${product.id}">O'chirish</button>
      </td>
    </tr>`).join('');
  el('products-table').querySelector('tbody').innerHTML = rows;
  el('products-info').textContent = `${offset + 1}–${offset + data.data.length} / ${data.total}`;
  el('products-prev').disabled = offset === 0;
  el('products-next').disabled = offset + data.data.length >= data.total;

  el('products-table').querySelectorAll('[data-edit]').forEach((button) => {
    button.onclick = () => openProduct(button.dataset.edit);
  });
  el('products-table').querySelectorAll('[data-delete]').forEach((button) => {
    button.onclick = async () => {
      if (!confirm('Mahsulot o\'chirilsinmi?')) return;
      await api(`/admin/products/${button.dataset.delete}`, { method: 'DELETE' });
      toast('Mahsulot o\'chirildi');
      loadProducts();
    };
  });
}

const PRODUCT_FIELDS = [
  ['name', 'Nom', 'text'],
  ['brand', 'Brend', 'text'],
  ['categoryId', 'Kategoriya ID', 'text'],
  ['subcategoryId', 'Kichik kategoriya ID', 'text'],
  ['price', 'Narx (so\'m)', 'number'],
  ['discountPrice', 'Chegirma narxi (bo\'sh = chegirmasiz)', 'number'],
  ['stock', 'Ombor qoldig\'i', 'number'],
  ['unit', 'O\'lchov birligi', 'text'],
  ['unitLabel', 'O\'lchov yorlig\'i', 'text'],
  ['description', 'Tavsif', 'textarea'],
  ['imageUrl', 'Rasm URL (yoki fayl yuklang)', 'text'],
];

function openModal(title, fields, values = {}) {
  el('modal-title').textContent = title;
  el('modal-fields').innerHTML = fields.map(([key, label, type]) => {
    const value = values[key] ?? '';
    if (type === 'textarea') {
      return `<label>${label}<textarea id="field-${key}" rows="3">${value}</textarea></label>`;
    }
    return `<label>${label}<input id="field-${key}" type="${type}" value="${String(value).replace(/"/g, '&quot;')}" /></label>`;
  }).join('') + `
    <label>Rasm fayli (JPEG/PNG/WEBP, ≤5 MB)<input id="field-file" type="file" accept="image/*" /></label>`;
  el('modal').hidden = false;
}

function closeModal() { el('modal').hidden = true; }

function readModal(fields) {
  const body = {};
  for (const [key, , type] of fields) {
    const node = el(`field-${key}`);
    if (!node) continue;
    const raw = node.value.trim();
    if (raw === '') { body[key] = type === 'number' ? null : ''; continue; }
    body[key] = type === 'number' ? Number(raw) : raw;
  }
  return body;
}

async function openProduct(id) {
  let product = {};
  if (id) {
    const data = await api(`/admin/products/${id}`);
    product = data.product;
  }
  openModal(id ? 'Mahsulotni tahrirlash' : 'Yangi mahsulot', PRODUCT_FIELDS, product);

  el('modal-save').onclick = async () => {
    try {
      const body = readModal(PRODUCT_FIELDS);
      const file = el('field-file').files[0];
      if (file) body.imageUrl = await upload(file);
      if (id) {
        await api(`/admin/products/${id}`, { method: 'PUT', body });
        toast('Mahsulot yangilandi');
      } else {
        await api('/admin/products', { method: 'POST', body });
        toast('Mahsulot qo\'shildi');
      }
      closeModal();
      loadProducts();
      loadStats();
    } catch (error) {
      toast(error.message, true);
    }
  };
}

// ----------------------------- Kategoriyalar --------------------------------

async function loadCategories() {
  const data = await api('/admin/categories');
  state.categories = data.categories;
  el('categories-table').querySelector('tbody').innerHTML = data.categories.map((category) => `
    <tr>
      <td>${category.id}</td>
      <td>${category.name}</td>
      <td>${category.subcategories.map((s) => s.name).join(', ')}</td>
      <td><button class="danger" data-del="${category.id}">O'chirish</button></td>
    </tr>`).join('');
  el('categories-table').querySelectorAll('[data-del]').forEach((button) => {
    button.onclick = async () => {
      try {
        await api(`/admin/categories/${button.dataset.del}`, { method: 'DELETE' });
        toast('Kategoriya o\'chirildi');
        loadCategories();
      } catch (error) {
        toast(error.message, true);
      }
    };
  });
}

const CATEGORY_FIELDS = [
  ['id', 'ID (lotincha, masalan: sport-outdoor)', 'text'],
  ['name', 'Nom', 'text'],
  ['icon', 'Ikonka kaliti', 'text'],
  ['color', 'Rang (#RRGGBB)', 'text'],
];

function openCategory() {
  openModal('Yangi kategoriya', CATEGORY_FIELDS);
  el('modal-save').onclick = async () => {
    try {
      await api('/admin/categories', { method: 'POST', body: readModal(CATEGORY_FIELDS) });
      toast('Kategoriya qo\'shildi');
      closeModal();
      loadCategories();
    } catch (error) {
      toast(error.message, true);
    }
  };
}

// -------------------------------- Bannerlar ---------------------------------

async function loadBanners() {
  const data = await api('/admin/banners');
  el('banners-table').querySelector('tbody').innerHTML = data.banners.map((banner) => `
    <tr>
      <td>${banner.id}</td>
      <td>${banner.title}<br /><span class="muted">${banner.subtitle}</span></td>
      <td>${banner.actionType}: ${banner.actionValue}</td>
      <td>${banner.image ? '<span class="badge ok">Faol</span>' : '<span class="badge">Rasm yo\'q</span>'}</td>
      <td><button class="danger" data-del="${banner.id}">O'chirish</button></td>
    </tr>`).join('');
  el('banners-table').querySelectorAll('[data-del]').forEach((button) => {
    button.onclick = async () => {
      await api(`/admin/banners/${button.dataset.del}`, { method: 'DELETE' });
      toast('Banner o\'chirildi');
      loadBanners();
    };
  });
}

const BANNER_FIELDS = [
  ['title', 'Sarlavha', 'text'],
  ['subtitle', 'Qo\'shimcha matn', 'text'],
  ['imageUrl', 'Rasm URL', 'text'],
  ['actionType', 'Amaliyot turi (category/product)', 'text'],
  ['actionValue', 'Amaliyot qiymati (ID)', 'text'],
];

function openBanner() {
  openModal('Yangi banner', BANNER_FIELDS);
  el('modal-save').onclick = async () => {
    try {
      const body = readModal(BANNER_FIELDS);
      const file = el('field-file')?.files[0];
      if (file) body.imageUrl = await upload(file);
      await api('/admin/banners', { method: 'POST', body });
      toast('Banner qo\'shildi');
      closeModal();
      loadBanners();
    } catch (error) {
      toast(error.message, true);
    }
  };
}

// ------------------------------- Buyurtmalar --------------------------------

const STATUS_LABELS = {
  created: 'Yaratilgan',
  confirmed: 'Tasdiqlangan',
  preparing: 'Tayyorlanmoqda',
  courier: 'Kuryerga berildi',
  delivering: 'Yetkazilmoqda',
  delivered: 'Yetkazildi',
  cancelled: 'Bekor qilingan',
};

async function loadOrders() {
  const query = state.orderStatus ? `?status=${state.orderStatus}` : '';
  const data = await api(`/admin/orders${query}`);
  el('orders-table').querySelector('tbody').innerHTML = data.orders.map((order) => `
    <tr>
      <td>${order.orderNumber}</td>
      <td>${new Date(order.createdAt).toLocaleString('uz-UZ')}</td>
      <td>${order.recipientName}<br /><span class="muted">${order.phone}</span></td>
      <td>${money(order.total)}</td>
      <td>${order.paymentMethod === 'cash' ? 'Naqd' : 'Karta'}${order.isPaid ? '' : ' (to\'lanmagan)'}</td>
      <td><span class="badge ${order.status === 'delivered' ? 'ok' : order.status === 'cancelled' ? 'bad' : 'warn'}">${STATUS_LABELS[order.status] || order.status}</span></td>
      <td>
        <select data-status="${order.id}">
          ${data.statuses.map((status) => `<option value="${status}" ${status === order.status ? 'selected' : ''}>${STATUS_LABELS[status] || status}</option>`).join('')}
        </select>
        <button class="ghost" data-details="${order.id}">Tafsilotlar</button>
      </td>
    </tr>`).join('');

  el('orders-table').querySelectorAll('[data-status]').forEach((select) => {
    select.onchange = async () => {
      try {
        await api(`/admin/orders/${select.dataset.status}/status`, {
          method: 'PATCH',
          body: { status: select.value },
        });
        toast('Holat yangilandi');
        loadOrders();
        loadStats();
      } catch (error) {
        toast(error.message, true);
        loadOrders();
      }
    };
  });

  const ordersById = new Map(data.orders.map((order) => [order.id, order]));
  el('orders-table').querySelectorAll('[data-details]').forEach((button) => {
    button.onclick = () => {
      const order = ordersById.get(button.dataset.details);
      if (!order) return;
      const items = order.items
        .map((item) => `• ${item.productName} × ${item.quantity} = ${money((item.discountPrice ?? item.unitPrice) * item.quantity)}`)
        .join('\n');
      alert(
        `Buyurtma ${order.orderNumber}\n` +
        `Mijoz: ${order.recipientName} (${order.phone})\n` +
        `Manzil: ${order.addressText}\n` +
        `Yetkazib berish: ${order.deliveryMethod} — ${money(order.deliveryFee)}\n` +
        `Chegirma: ${money(order.discount)}\n` +
        `Jami: ${money(order.total)}\n` +
        `Izoh: ${order.comment || '—'}\n\n${items}`,
      );
    };
  });
}

// -------------------------------- Mijozlar ----------------------------------

async function loadCustomers() {
  const data = await api('/admin/customers');
  el('customers-table').querySelector('tbody').innerHTML = data.customers.map((customer) => `
    <tr>
      <td>${customer.name || '—'}</td>
      <td>${customer.phone || '—'}</td>
      <td>${customer.orders_count}</td>
      <td>${money(customer.total_spent)}</td>
      <td>${new Date(customer.created_at).toLocaleDateString('uz-UZ')}</td>
    </tr>`).join('');
}

// --------------------------------- Boshqaruv --------------------------------

function bindTabs() {
  document.querySelectorAll('.tab').forEach((button) => {
    button.onclick = () => {
      document.querySelectorAll('.tab').forEach((b) => b.classList.remove('active'));
      button.classList.add('active');
      document.querySelectorAll('.tab-panel').forEach((panel) => { panel.hidden = true; });
      el(`tab-${button.dataset.tab}`).hidden = false;
      if (button.dataset.tab === 'products') loadProducts();
      if (button.dataset.tab === 'categories') loadCategories();
      if (button.dataset.tab === 'banners') loadBanners();
      if (button.dataset.tab === 'orders') loadOrders();
      if (button.dataset.tab === 'customers') loadCustomers();
      if (button.dataset.tab === 'dashboard') loadStats();
    };
  });
}

async function bootstrap() {
  try {
    await api('/admin/stats');
  } catch {
    return logout();
  }
  el('login').hidden = true;
  el('app').hidden = false;
  bindTabs();
  await loadStats();
  await loadProducts();
}

function bindEvents() {
  el('login-form').onsubmit = login;
  el('logout').onclick = logout;
  el('modal-cancel').onclick = closeModal;
  el('product-new').onclick = () => openProduct(null);
  el('category-new').onclick = openCategory;
  el('banner-new').onclick = openBanner;
  el('products-prev').onclick = () => {
    state.products.offset = Math.max(0, state.products.offset - state.products.limit);
    loadProducts();
  };
  el('products-next').onclick = () => {
    state.products.offset += state.products.limit;
    loadProducts();
  };
  el('product-search').oninput = (event) => {
    state.products.q = event.target.value.trim();
    state.products.offset = 0;
    clearTimeout(window.__searchTimer);
    window.__searchTimer = setTimeout(loadProducts, 300);
  };
  el('order-status-filter').onchange = (event) => {
    state.orderStatus = event.target.value;
    loadOrders();
  };
}

bindEvents();
if (state.token) bootstrap();
