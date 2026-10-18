# BozorGo server — REST API, PostgreSQL va admin panel

Node.js 18+ / Express 4 / PostgreSQL 16.

> Demo loyiha: SMS va to'lov provayderlari ulanmagan. Ular sozlanmaganda server
> hech qachon "yuborildi" yoki "to'landi" deb javob qaytarmaydi.

## 1. Ishga tushirish

```bash
cd server
cp .env.example .env          # JWT_SECRET va ADMIN_PASSWORD ni o'zgartiring!
docker compose up -d          # PostgreSQL (postgres://bozorgo:bozorgo@localhost:5432/bozorgo)
npm install
npm run migrate               # db/schema.sql ni qo'llash
npm run seed                  # 1000 ta mahsulot, 15 ta kategoriya, 5 ta banner
npm start                     # http://localhost:3000/api
```

- API: `http://localhost:3000/api`
- Admin panel: `http://localhost:3000/admin`
- Sog'liq: `http://localhost:3000/health`

Qayta boshlash (barcha ma'lumotlarni o'chirib):

```bash
npm run reset                 # migrate --drop + seed
```

## 2. Muhit o'zgaruvchilari (`.env`)

| O'zgaruvchi | Default | Izoh |
|---|---|---|
| `PORT` | 3000 | HTTP port |
| `API_PREFIX` | `/api` | API prefiksi |
| `CORS_ORIGIN` | `*` | vergul bilan ajratilgan domenlar |
| `DATABASE_URL` | `postgres://bozorgo:bozorgo@localhost:5432/bozorgo` | PostgreSQL |
| `JWT_SECRET` | (demo) | **Production'da albatta o'zgartiring** |
| `JWT_TTL_DAYS` | 30 | token amal qilish muddati |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NAME` | — | birinchi admin |
| `SMS_PROVIDER` | `none` | `none` bo'lsa SMS yuborilmaydi, kod demo sifatida qaytariladi |
| `PAYMENT_PROVIDER` | `none` | `none` bo'lsa karta to'lovi amalga oshirilmaydi |
| `DEMO_MODE` | `true` | OTP kodi javobda (`debugCode`) qaytadi |

## 3. Jadvallar (`db/schema.sql`)

`categories`, `subcategories`, `products`, `banners`, `users`, `admin_users`, `otp_codes`,
`addresses`, `orders`, `order_items`.

Muhim cheklovlar:
- `orders.idempotency_key UNIQUE` — takroriy buyurtma ochilmaydi;
- `orders.order_number UNIQUE`;
- `products.discount_price < price` (CHECK);
- `orders.status` va `delivery_method`/`payment_method` CHECK ro'yxatlari;
- `updated_at` triggerlari.

## 4. Xavfsizlik va halollik

- Admin huquqi faqat serverda: JWT `role` + `admin_users.is_active` (`src/auth.js`).
- Buyurtma yaratishda server narx/chegirma/ombor/summani qayta hisoblaydi
  (`src/routes/orders.js`, `SELECT … FOR UPDATE` + tranzaksiya).
- Omborda yetarli bo'lmasa yoki mahsulot sotuvdan olingan bo'lsa — `400` + aniq sabablar ro'yxati.
- To'lov: provayder ulanmagan bo'lsa `pending_demo` (hech qachon `paid` emas).
- SMS: provayder `none` bo'lsa kod faqat konsolga yoziladi va demo javobda qaytariladi.

## 5. Testlar

```bash
npm test
```

14 ta test: telefon/miqdor validatsiyasi, buyurtma tarkibi, takroriy qatorlar,
summa solishtirish, holat o'tishlari, yetkazib berish narxi va savat hisobi.
(testlar DB talab qilmaydi — sof mantiq.)

## 6. Endpointlar

To'liq ro'yxat: [../docs/API.md](../docs/API.md). Qisqacha:

| | |
|---|---|
| Ochiq | `GET /products`, `GET /products/:id`, `GET /categories`, `GET /banners`, `POST /auth/otp/*`, `POST /auth/admin/login`, `POST /orders` |
| Token | `GET /orders`, `GET /orders/:id`, `POST /orders/:id/cancel`, `POST /payments/create` |
| Admin | `/admin/stats`, `/admin/products*`, `/admin/categories*`, `/admin/banners*`, `/admin/orders*`, `/admin/customers`, `/admin/upload` |

## 7. Ilovani ulash

`local.properties` ichida (ilova ildizida):

```
API_BASE_URL=http://10.0.2.2:3000/api/     # emulyator
API_BASE_URL=http://192.168.1.50:3000/api/ # telefon (kompyuter LAN IP)
```

`API_BASE_URL` bo'sh bo'lsa ilova to'liq oflayn ishlaydi (lokal Room bazasi, 1000 ta mahsulot).
