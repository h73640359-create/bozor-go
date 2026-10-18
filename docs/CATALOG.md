# Katalog: generatsiya, tekshiruv, rasmlar

## Manba

Katalog `data/scripts/` ichidagi generatorlar bilan **deterministik** tarzda yig'iladi
(`SEED = 20260118`): bir xil kiruvchi ma'lumotlardan har doim bir xil JSON hosil bo'ladi.

```
data/scripts/catalog_data/f01…f15_*.py   # har bir kategoriya uchun xom qatorlar
data/scripts/catalog_registry.py         # kichik bo'limlar, ikonkalar, ranglar, birliklar
data/scripts/build_catalog.py            # → data/catalog.json, categories.json, banners.json
data/scripts/generate_images.py          # → data/images/products/*.webp, banners/*.webp
data/scripts/generate_category_images.py # → data/images/categories/*.webp, fallback/*.webp
data/scripts/validate_catalog.py         # 71 ta tekshiruv (--with-images bilan)
data/scripts/sync_assets.py              # data/ → app/src/main/assets/
data/scripts/generate_brand.py, icons.py # logotip va ikonka chizish
```

Qator formati (`RAW` ichida):

```
nom|kichik_kategoriya|brend|o'lchov_birligi|miqdor|narx(UZS)
Olma Golden 1 kg|fresh-fruits|Bog'bon|kg|1|16900
```

## Qayta yig'ish

```bash
python3 data/scripts/build_catalog.py          # 1000 ta mahsulot
python3 data/scripts/generate_images.py        # 1000 ta rasm (≈13 s)
python3 data/scripts/validate_catalog.py --with-images
python3 data/scripts/sync_assets.py            # Android assets papkasiga ko'chirish
```

## Tekshiruvlar (63 ta)

`validate_catalog.py` quyidagilarni tekshiradi (qisqacha):
mahsulotlar soni (≥ 1000), ID noyobligi, nom noyobligi va uzunligi, kategoriya/kichik bo'lim
mavjudligi, narx > 0, chegirma < narx, ombor ≥ 0, `inStock` muvofiqligi, reyting 0..5,
birlik ruxsat etilgan ro'yxatda, tavsif uzunligi, rasm fayli mavjudligi va noyobligi,
rasmlarning bir xil emasligi (xesh bo'yicha), kategoriyalar taqsimoti, `isDemoData` belgisi,
bannerlar va kategoriyalar strukturasi.

Xuddi shunday tekshiruvlar Android unit testlarida ham bor (`app/src/test/.../CatalogSeedTest.kt`).

## Rasmlar

- Har bir mahsulot uchun **o'ziga xos** rasm: kichik bo'lim ikonkasi + kategoriya rangidan
  hosil qilingan gradient fon + o'lchov yorlig'i (`320×320`, WebP, ~3 KB).
- Hammasi loyiha ichida Pillow bilan chizilgan **original illustratsiyalar** — real fotosurat emas,
  uchinchi shaxs mualliflik huquqiga tegishli emas, litsenziya talab qilmaydi.
- Bir xil rasm takrorlanmaydi (xesh bo'yicha tekshiriladi): 1000 ta mahsulot → 1000 ta noyob fayl.
  Har bir rasm mahsulot ID si va nomidan olingan xesh asosida o'z gradienti, ikonka siljishi va
  bezak nuqtalariga ega.
- Jami hajm: ~3.0 MB (1000 ta rasm) — APK ichida, internetsiz ham ko'rinadi.
- Bannerlar: `800×400` WebP (5 ta) — faqat fon + illustratsiya, matn Compose'da chiziladi.
- Kategoriya rasmlari: `480×320` WebP (15 ta) — katalog kartalari va bosh sahifa chiplari uchun.
- Zaxira rasmlar: `320×320` WebP (15 ta) — mahsulot rasmi topilmaganda kategoriyaga mos rasm.

### Generatsiya

```bash
python3 data/scripts/generate_images.py          # 1000 mahsulot + 5 banner
python3 data/scripts/generate_category_images.py # 15 kategoriya + 15 zaxira rasm
python3 data/scripts/sync_assets.py              # data/ -> app/src/main/assets/
python3 data/scripts/validate_catalog.py --with-images
```

Nomi o'zgargan mahsulotning rasmi ham o'zgaradi, lekin fayl nomi har doim
`products/<productId>.webp` — ya'ni rasm mahsulot identifikatori bo'yicha bog'langan,
nom bo'yicha taxmin qilinmaydi.

### Ilovada qanday ko'rsatiladi

`ProductImage` (ui/components/ProductCard.kt) ketma-ket 3 ta manbani sinaydi:
`imageUrl` → asset → kategoriyaning zaxira rasmi. Internet orqali yuklashda progress
ko'rsatkichi chiqadi va 1 marta qayta uriniladi; xatolik sababi `BozorGoImage` log tegiga
yoziladi. Rasmlar `lazy` ro'yxatlarda (LazyVerticalGrid/LazyRow) sahifalab yuklanadi,
Coil xotira + disk keshidan foydalanadi.

## Nima uchun 1000 ta aniq?

Talab: kamida 1000 ta noyob mahsulot, kategoriyalar bo'yicha aniq taqsimot.
`catalog_data/` fayllaridagi qatorlar soni aynan shu taqsimotga mos keltirilgan
(`build_catalog.py` chiqishida har bir kategoriya satrida son ko'rinadi).
Agar qator qo'shilsa/o'chirilsa, `validate_catalog.py` va Android testlari darhol xatolik beradi.
