# BozorGo REST API

Baza: `API_BASE_URL` (`local.properties`) — masalan `http://192.168.1.50:3000/api/`.
Barcha javoblar JSON. Xatoliklar: `{ "error": { "message": "...", "status": 400, "details": [...] } }`.

Avtorizatsiya: `Authorization: Bearer <JWT>`.
- Mijoz tokeni: `POST /auth/otp/verify`;
- Admin tokeni: `POST /auth/admin/login` (faqat `admin_users` jadvalidagi foydalanuvchilar).

---

## Mahsulotlar

### `GET /products`
Parametrlar: `limit` (1..500, default 100), `offset`, `categoryId`, `subcategoryId`,
`q` (nom/brend/tavsif bo'yicha), `discount=1`, `sort` (`price_asc|price_desc|popular|newest|discount`),
`updatedAfter` (ISO-8601).

```json
{ "data": [ { "id": "prd-0001", "name": "Olma Golden 1 kg", "price": 16900, "discountPrice": null,
              "stock": 41, "inStock": true, "unit": "kg", "unitLabel": "1 kg", "image": "products/prd-0001.webp",
              "imageUrl": "", "brand": "Bog'bon", "categoryId": "fruits-vegetables", "isDemo": true } ],
  "total": 1000, "limit": 100, "offset": 0 }
```

### `GET /products/:id`
Bitta mahsulot (yuqoridagi shaklda).

## Katalog metadatasi

### `GET /categories`
```json
{ "categories": [ { "id": "fruits-vegetables", "name": "Meva va sabzavotlar", "icon": "produce",
                    "color": "#4CAF50", "productCount": 100,
                    "subcategories": [ { "id": "fresh-fruits", "name": "Yangi mevalar", "icon": "apple", "productCount": 44 } ] } ] }
```

### `GET /banners`
```json
{ "banners": [ { "id": "bnr-001", "title": "Birinchi buyurtmangizga", "subtitle": "15% chegirma",
                 "image": "banners/bnr-001.webp", "actionType": "category", "actionValue": "fruits-vegetables",
                 "startColor": "#2E7D32", "endColor": "#66BB6A" } ] }
```

## Autentifikatsiya (telefon + SMS kod)

### `POST /auth/otp/request`
So'rov: `{ "phone": "+998 90 123 45 67" }`.

`SMS_PROVIDER=none` va `DEMO_MODE=true` bo'lsa:
```json
{ "success": true, "demo": true, "message": "Demo rejim: SMS yuborilmadi, kod javobda qaytarildi", "debugCode": "123456" }
```
Provayder ulanmagan (lekin demo o'chirilgan) bo'lsa — `501` qaytariladi; hech qachon
"yuborildi" degan yolg'on javob bo'lmaydi.

### `POST /auth/otp/verify`
So'rov: `{ "phone": "...", "code": "123456", "name": "Ali" }`.
Javob: `{ "token": "<JWT>", "user": { "id": "usr-...", "name": "Ali", "phone": "998901234567", "isAdmin": false } }`.

Kod 6 xonali, 10 daqiqa amal qiladi, 5 marta urinishdan keyin bloklanadi.

### `POST /auth/admin/login`
So'rov: `{ "email": "admin@bozorgo.local", "password": "..." }`. Javob: `{ "token", "user" }`.

## Buyurtmalar

### `POST /orders` (token ixtiyoriy)
So'rov:
```json
{
  "idempotencyKey": "3f1c...-uuid",
  "items": [ { "productId": "prd-0001", "quantity": 2, "selectedOption": "M" } ],
  "deliveryMethod": "standard",       // standard | express | pickup
  "paymentMethod": "cash",            // cash | card
  "recipientName": "Ali Valiyev",
  "phone": "+998 90 123 45 67",
  "addressTitle": "Uy",
  "addressText": "Toshkent, Yunusobod 5-uy, 12-xonadon",
  "comment": "Kechqurun yetkazib bering"
}
```

Server quyidagilarni **o'zi** bajaradi:
1. mahsulotlarni bazadan qayta o'qiydi (`SELECT ... FOR UPDATE`) — narx, chegirma, ombor;
2. omborda yetarli bo'lmagan yoki sotuvdan olingan mahsulot bo'lsa — `400` + aniq ro'yxat;
3. summani o'zi hisoblaydi (mijoz yuborgan narx/chegirma ishlatilmaydi);
4. `idempotencyKey` takrorlansa — yangi buyurtma ochilmaydi, mavjud buyurtma `200` bilan qaytariladi;
5. ombor qoldig'ini kamaytiradi, tranzaksiya ichida.

Javob (`201`): `{ "order": { "id": "ord-...", "orderNumber": "BG-482913", "status": "created", "total": 49000, ... } }`

### `GET /orders`
Foydalanuvchi faqat o'z buyurtmalarini, admin — barchasini ko'radi.

### `GET /orders/:id`

### `POST /orders/:id/cancel`
Faqat `created | confirmed | preparing` holatlarida. Ombor qoldig'i qaytariladi.

## To'lov

### `POST /payments/create`
So'rov: `{ "orderId": "ord-...", "method": "card" }` (token kerak).

- `method=cash` → `{ "status": "pending", "message": "Naqd to'lov: kuryerga to'lanadi" }`
- `method=card`, `PAYMENT_PROVIDER=none` →
  `{ "status": "pending_demo", "demo": true, "message": "To'lov provayderi ulanmagan … haqiqiy pul o'tkazilmadi." }`
- Provayder ko'rsatilgan, lekin integratsiya yozilmagan → `{ "status": "not_implemented", ... }`

Hech qachon `status: "paid"` to'lov amalga oshirilmasdan qaytarilmaydi.

## Admin (`/api/admin/*`, faqat admin tokeni)

| Yo'l | Vazifa |
|---|---|
| `GET /admin/stats` | statistika (mahsulot, buyurtma, tushum, mijozlar, kam qolganlar) |
| `GET/POST/PUT/DELETE /admin/products[/:id]` | mahsulotlar CRUD |
| `POST /admin/products/:id/stock` `{ stock }` | ombor qoldig'i |
| `POST /admin/products/:id/discount` `{ discountPrice \| null }` | chegirma (foiz avtomatik hisoblanadi) |
| `POST /admin/upload` (`multipart/form-data`, maydon `image`) | rasm yuklash (JPEG/PNG/WEBP, ≤ 5 MB) → `{ url: "/uploads/..." }` |
| `GET/POST/PUT/DELETE /admin/categories[/:id]` | kategoriyalar |
| `GET/POST/PUT/DELETE /admin/banners[/:id]` | bannerlar |
| `GET /admin/orders`, `PATCH /admin/orders/:id/status` `{ status }` | buyurtmalar va holat |
| `GET /admin/customers` | mijozlar ro'yxati |

Ruxsat **serverda** tekshiriladi: JWT ichidagi `role` va `admin_users` jadvalidagi faol yozuv
(`src/auth.js` → `requireAdmin`). Faqat ilovadagi "admin" belgisi hech qanday huquq bermaydi.

## Holatlar (7 ta)

`created` → `confirmed` → `preparing` → `courier` → `delivering` → `delivered`
(`created|confirmed|preparing` dan `cancelled` ga o'tish mumkin; `delivered`/`cancelled` dan keyin o'zgartirib bo'lmaydi).

## Sog'liqni tekshirish

`GET /health` → `{ "status": "ok" | "db-unavailable", "demo": true }`
