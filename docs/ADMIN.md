# Admin panel

Manzil: `http://<server>:3000/admin` (masalan `http://localhost:3000/admin`).

## Kirish

- Email/parol: `.env` dagi `ADMIN_EMAIL` / `ADMIN_PASSWORD` (birinchi ishga tushirishda
  `admin_users` jadvaliga yoziladi, parol `bcrypt` bilan xeshlanadi).
- Token `localStorage` da saqlanadi; har bir so'rov `Authorization: Bearer <token>` bilan yuboriladi.
- Huquqni server tekshiradi (`requireAdmin`): token ichidagi `role=admin|manager` va
  bazadagi faol admin yozuvi. Tokensiz yoki oddiy foydalanuvchi tokeni bilan
  `/api/admin/*` so'rovlari `401`/`403` qaytaradi.

## Bo'limlar

### Boshqaruv (Dashboard)
Mahsulotlar soni, omborda mavjudlar, buyurtmalar (kutilayotgan/yetkazilgan),
demo tushum, mijozlar soni va "kam qolgan mahsulotlar" (omborda ≤ 5 dona) ro'yxati.

### Mahsulotlar
- Qidiruv: nom, brend yoki ID bo'yicha (`?q=` serverda `ILIKE` bilan).
- Sahifalash: 50 tadan.
- **Yangi mahsulot** / **Tahrirlash**: nom, brend, kategoriya va kichik kategoriya ID,
  narx, chegirma narxi (bo'sh qoldirilsa — chegirma o'chadi, foiz avtomatik hisoblanadi),
  ombor qoldig'i, o'lchov birligi va yorlig'i, tavsif, rasm (URL yoki fayl yuklash).
- **O'chirish** — tasdiqlashdan keyin butunlay o'chiriladi (buyurtma tarixidagi nusxalar saqlanadi).

### Kategoriyalar
Yangi kategoriya qo'shish (id, nom, ikonka, rang) va o'chirish.
Ichida mahsulot bo'lgan kategoriya o'chirilmaydi (server `409` qaytaradi) — avval
mahsulotlarni boshqa kategoriyaga o'tkazing.

### Bannerlar
Sarlavha, qo'shimcha matn, rasm (fayl yoki URL), amaliyot turi/qiymati.
Bannerlar `GET /api/banners` orqali ilova bosh sahifasida ko'rinadi.

### Buyurtmalar
- Holat bo'yicha filtr.
- Holatni o'zgartirish (select) — ruxsat etilmagan o'tish serverda rad etiladi (`409`).
- **Tafsilotlar** — mijoz, manzil, mahsulotlar ro'yxati, chegirma, yetkazib berish, jami.
- Bekor qilinganda ombor qoldig'i avtomatik qaytariladi.

### Mijozlar
Telefon raqami bo'yicha ro'yxat: buyurtmalar soni, jami xarid summasi, ro'yxatdan o'tgan sana.

## Rasm yuklash

`POST /api/admin/upload` (`multipart/form-data`, maydon `image`):
JPEG/PNG/WEBP, maksimum 5 MB. Fayl `server/public/uploads/` ga saqlanadi,
javobda `/uploads/<fayl>` URL qaytadi va mahsulotning `imageUrl` maydoniga yoziladi.
Ilova `imageUrl` bo'sh bo'lsa APK ichidagi lokal rasmni (`imageAssetPath`) ishlatadi.

## Xavfsizlik

- Parollar faqat `bcrypt` xesh ko'rinishida saqlanadi.
- Admin yo'nalishlarining barchasi `requireAdmin` orqali himoyalangan.
- Buyurtma summasini o'zgartirish uchun alohida endpoint yo'q — summa faqat server hisoblaydi.
- Production'da `JWT_SECRET`ni albatta o'zgartiring va `DEMO_MODE=false` qiling.
