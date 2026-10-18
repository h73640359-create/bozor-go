# BozorGo

O'zbekiston bozori uchun namunaviy (demo) onlayn do'kon — Android ilovasi (Kotlin + Jetpack Compose),
PostgreSQL/Express backend va admin paneli bilan.

> **Muhim:** Bu demo loyiha. Katalogdagi 1000 ta mahsulot, narxlar, brendlar, chegirmalar,
> reytinglar va sharhlar **to'qima (namunaviy) ma'lumot** — real do'kon narxlari emas.
> Haqiqiy SMS va to'lov provayderlari ulanmagan: ular demo ekani ilovaning o'zida ko'rsatiladi.
> Hech qanday ekranda "to'lov muvaffaqiyatli amalga oshirildi" degan yolg'on xabar chiqmaydi.

---

## 0. Holat (tekshirilgan)

| Tekshiruv | Natija |
|---|---|
| GitHub Actions: `./gradlew testDebugUnitTest` | ✅ muvaffaqiyatli (run **39133535192**) |
| GitHub Actions: `./gradlew assembleDebug` | ✅ muvaffaqiyatli |
| APK artefakti (`app-debug`) | ✅ 22 019 146 bayt (21,0 MB) — `app/build/outputs/apk/debug/app-debug.apk` |
| APK ichidagi rasmlar (CI tekshiruvi) | ✅ 1000 mahsulot + 15 kategoriya + 15 zaxira + 5 banner rasmi |
| Katalog: 1000 ta noyob mahsulot | ✅ 71 ta tekshiruv (`--with-images`) + unit testlar |
| Rasmlar unit testlari (`ImageAssetsTest`) | ✅ 9 ta test (fayl mavjudligi, WebP, ID bo'yicha bog'lash, takrorlanish) |
| Backend testlari (`cd server && npm test`) | ✅ 14 ta test |

APK **faqat GitHub Actions'da** yig'iladi (loyihada JDK/Android SDK shart).

To'g'ridan-to'g'ri yuklab olish (GitHub'ga kirgan holda):

- So'nggi yashil ish (run **39133535192**):
  <https://github.com/h73640359-create/bozor-go/actions/runs/39133535192#artifacts>
- Artefakt havolasi (`app-debug`, 22 019 146 bayt):
  <https://github.com/h73640359-create/bozor-go/actions/runs/39133535192/artifacts/12138522682>
- Yoki: **GitHub → Actions → "Android" → oxirgi yashil ish → Artifacts → `app-debug`**

Artefakt ZIP ichida `app-debug.apk` fayli bor. Debug paket nomi: `uz.bozorgo.debug`,
artefakt 2026-11-17 gacha saqlanadi.

**Rasmlar:** 1000 ta mahsulot rasmi, 15 ta kategoriya rasmi va 15 ta kategoriyaga mos zaxira
rasm APK ichidagi `assets/` dan yuklanadi — internet talab qilmaydi, o'lik havolalar yo'q.
Batafsil: [docs/CATALOG.md](docs/CATALOG.md) bo'limi "Rasmlar".

## 1. Loyiha tarkibi

```
bozor-go/
├── app/                      # Android ilovasi (Kotlin, Compose, Material 3)
│   └── src/main/assets/      # seed JSON (seed/) + rasmlar: products/, categories/,
│                             # fallback/, banners/ (barchasi WebP)
├── data/                     # katalog manbasi (generatorlar, JSON, rasmlar)
│   ├── catalog.json          # 1000 ta noyob mahsulot
│   ├── categories.json       # 15 ta kategoriya / 86 ta kichik bo'lim
│   ├── banners.json          # 5 ta bosh sahifa banneri
│   ├── images/               # generatsiya qilingan original rasmlar (WebP)
│   └── scripts/              # katalog va rasm generatorlari + validator
├── server/                   # REST API (Node.js/Express) + PostgreSQL + admin panel
├── docs/                     # hujjatlar (arxivitektura, API, build, CI, admin)
└── .github/workflows/        # Android CI: test + debug APK artefakti
```

## 2. Asosiy imkoniyatlar

**Ilova (offline-first)**
- 1000 ta noyob mahsulot birinchi ishga tushirishda lokal Room bazasiga yoziladi — internetsiz ham ishlaydi.
- Kategoriya sahifalari, qidiruv (o'zbek apostroflari va registrga chidamli),
  filtrlar (narx oralig'i, chegirma, brend), saralash (narx, mashhurlik, yangilik, chegirma),
  sahifalab yuklash (pagination).
- Mahsulot kartochkasi (gallereya, chegirma, reyting, ombor qoldig'i, variantlar — rang/o'lcham,
  tavsif, yetkazib berish shartlari, o'xshash mahsulotlar).
- Savatcha (miqdor, variant, chegirma, bepul yetkazib berish chegarasi), sevimlilar, manzillar.
- Buyurtma: 10 qadamli oqim (savat → manzil → yetkazib berish → to'lov → tasdiqlash →
  buyurtma raqami → tarix), 7 ta holat, takroriy yuborishdan himoya.
- Profil: telefon orqali kirish (demo SMS kod), ism/telefonni tahrirlash, sevimlilar,
  buyurtmalar, manzillar, bildirishnomalar, til (o'zbek/rus), mavzu, yordam, maxfiylik, shartlar.
- To'lov: naqd (kuryerga) + karta. Provayder ulanmaganda — faqat demo rejim (pul o'tkazilmaydi).

**Backend (server/)**
- PostgreSQL sxemasi (`server/db/schema.sql`), seed skripti (`npm run seed`).
- REST API (`/api/...`): mahsulotlar, kategoriyalar, bannerlar, OTP, buyurtmalar, to'lovlar.
- Server narx, chegirma, ombor qoldig'i va buyurtma summasini **qaytadan hisoblaydi**;
  mijoz yuborgan narxga ishonilmaydi. Takroriy yuborish `idempotencyKey` bilan bloklanadi.
- Admin huquqlari faqat serverda tekshiriladi (JWT + `admin_users` jadvali).

**Admin panel (server/public/admin)**
- Mahsulotlar: qo'shish/tahrirlash/o'chirish, narx, chegirma, ombor, rasm yuklash.
- Kategoriyalar, bannerlar, buyurtmalar (holatni o'zgartirish), mijozlar, statistika.

## 3. Tez ishga tushirish

### Android ilovasi (offline rejimda ham to'liq ishlaydi)

```bash
# 1) Katalogni tekshirish (ixtiyoriy)
python3 data/scripts/validate_catalog.py --with-images

# 2) local.properties
cp local.properties.example local.properties
#   sdk.dir=... ni o'z yo'lingiz bilan almashtiring
#   API_BASE_URL ni bo'sh qoldiring — ilova oflayn ishlaydi

# 3) Yig'ish va o'rnatish
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # ulangan qurilmaga o'rnatish
./gradlew testDebugUnitTest      # unit testlar (1000 ta mahsulot tekshiruvi ham shu yerda)
```

Batafsil: [docs/BUILD.md](docs/BUILD.md).

### Backend (ixtiyoriy — ilova serversiz ham ishlaydi)

```bash
cd server
cp .env.example .env             # JWT_SECRET va ADMIN_PASSWORD ni o'zgartiring
docker compose up -d             # PostgreSQL
npm install
npm run migrate                  # server/db/schema.sql
npm run seed                     # 1000 ta mahsulotni bazaga yozish
npm start                        # http://localhost:3000/api
# Admin panel: http://localhost:3000/admin
```

Ilovani serverga ulash uchun `local.properties` ichida:

```
API_BASE_URL=http://10.0.2.2:3000/api/     # emulyator
API_BASE_URL=http://192.168.1.50:3000/api/ # telefon (kompyuter IP si)
```

Hujjatlar: [server/README.md](server/README.md), [docs/API.md](docs/API.md), [docs/ADMIN.md](docs/ADMIN.md).

## 4. Katalog: 1000 ta mahsulot

| Kategoriya | Soni |
|---|---:|
| Meva va sabzavotlar | 100 |
| Go'sht, tovuq va baliq | 70 |
| Sut mahsulotlari va tuxum | 70 |
| Non, shirinliklar va pishiriqlar | 80 |
| Ichimliklar va suv | 70 |
| Oziq-ovqat, guruch, yog' va yormalar | 100 |
| Uy-ro'zg'or va tozalash vositalari | 100 |
| Gigiyena va kosmetika | 80 |
| Bolalar mahsulotlari | 50 |
| Kiyim-kechak va poyabzal | 80 |
| Telefonlar va elektronika | 60 |
| Oshxona anjomlari va uy jihozlari | 50 |
| Sport va dam olish | 40 |
| Uy hayvonlari uchun mahsulotlar | 30 |
| Avtomobil aksessuarlari va boshqa | 20 |
| **Jami** | **1000** |

Har bir mahsulot: noyob `id` (`prd-0001`…), nom, kategoriya + kichik bo'lim, Brend, rasm,
to'liq tavsif, narx, ixtiyoriy chegirma narxi, o'lchov birligi (dona/kg/g/ml/l/qadoq/juft/bog'),
ombor qoldig'i, reyting, sharhlar soni, mavjudlik belgisi, ixtiyoriy variantlar (rang/o'lcham).

**Rasmlar:** barcha 1000 ta rasm loyiha ichida maxsus generatsiya qilingan original vektor
illustratsiyalar (Pillow bilan chizilgan, WebP, 320×320). Real fotosurat emas, uchinchi shaxs
mualliflik huquqiga tegishli emas, har bir mahsulot o'z kichik bo'limi ikonkasini oladi.
Batafsil: [docs/CATALOG.md](docs/CATALOG.md).

**Rasmlar strategiyasi (muhim):** rasmlar internetdan emas, APK ichidagi `assets/` dan
yuklanadi — o'lik havolalar yo'q, oflayn ham ishlaydi.

| Nima | Qayerda | Soni |
|---|---|---|
| Mahsulot rasmlari | `assets/products/<productId>.webp` | 1000 (har biri noyob) |
| Kategoriya rasmlari | `assets/categories/<categoryId>.webp` | 15 |
| Zaxira (fallback) rasmlar | `assets/fallback/<categoryId>.webp` | 15 |
| Banner illustratsiyalari | `assets/banners/<bannerId>.webp` | 5 |

Yuklash tartibi (`ui/components/ProductCard.kt` → `ProductImage`):
1. `imageUrl` (admin panel orqali yuklangan rasm) → 2. APK ichidagi asset →
3. rasm topilmasa **kategoriyaga mos zaxira rasm** (hech qachon hamma narsada bitta kulrang
kvadrat qolmaydi). Internet orqali yuklanayotganda — yuklanish ko'rsatkichi, 1 marta qayta
urinish, har bir xatolik `BozorGoImage` tegi bilan logga yoziladi. Coil keshidan (xotira +
disk) foydalaniladi, "Sozlamalar → Keshni tozalash" rasmlar keshini ham tozalaydi.

## 5. Testlar

```bash
./gradlew testDebugUnitTest   # Android unit testlari
cd server && npm test         # backend (sof mantiq) testlari
python3 data/scripts/validate_catalog.py --with-images   # katalog + rasmlar: 71 ta tekshiruv
```

Android testlari orasida **majbriy** tekshiruvlar bor:
kamida 1000 ta mahsulot, ID lar noyobligi, kategoriya ID lari mavjudligi, nom/tavsif/narx/rasm
to'ldirilganligi, kategoriyalar bo'yicha aniq taqsimot, `isDemoData` belgisi.

`ImageAssetsTest` rasmlarni alohida tekshiradi: har bir mahsulotning rasm fayli mavjudligi va
WebP ekani, rasm nomi mahsulot ID siga mosligi, bitta rasm ko'p mahsulotda takrorlanmaganligi,
har bir kategoriya uchun karta + zaxira rasm borligi, uzоq URL lar formati va domen modeli
(`Product.imageModel()` / `Product.fallbackImageModel()`) haqiqatan mavjud faylga yo'naltirishi.

## 6. Hujjatlar

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — ilova arxitekturasi (MVVM, ma'lumotlar oqimi)
- [docs/BUILD.md](docs/BUILD.md) — yig'ish, o'rnatish, imzolash
- [docs/CI.md](docs/CI.md) — GitHub Actions, release imzolash sirlari
- [docs/API.md](docs/API.md) — REST API
- [docs/ADMIN.md](docs/ADMIN.md) — admin panel
- [docs/CATALOG.md](docs/CATALOG.md) — katalog generatori va rasmlar
- [server/README.md](server/README.md) — backend

## 7. Holat va cheklovlar (batafsil ro'yxat)

Quyidagilar **ulanmagan/sozlanmagan** va ilova ularni demo sifatida ko'rsatadi:

| Xizmat | Holat |
|---|---|
| SMS (OTP) | `SMS_PROVIDER=none` — kod yuborilmaydi, demo kod konsolda va javobda (`debugCode`) |
| To'lov (karta) | `PAYMENT_PROVIDER=none` — to'lov amalga oshirilmaydi, demo xabar qaytariladi |
| Push-bildirishnomalar | Tayyor kanal va ruxsat so'rovi bor, FCM/server kaliti sozlanmagan |
| Kuryer GPS-kuzatuvi | Yo'q — buyurtma holati qo'lda o'zgartiriladi |
| Server | Ixtiyoriy: sozlanmasa ilova to'liq oflayn ishlaydi |

Boshqa brendlar (Uzum Market, Korzinka Go) logotipi, dizayni va brend elementlari ishlatilmagan —
faqat funksional g'oyalar olingan, barcha vizual elementlar original.

## 8. Litsenziya

Kod — loyiha egasi ixtiyorida (litsenziya qo'shilmagan). Katalog ma'lumotlari, brend nomlari,
narxlar, reytinglar va sharhlar to'qima demo ma'lumotdir; rasmlar loyiha ichida generatsiya qilingan
original illustratsiyalardir.
