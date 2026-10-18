#!/usr/bin/env python3
"""Katalog uchun majburiy avtomatik tekshiruv.

Tekshiradi:
  1. Bazada kamida 1000 ta noyob mahsulot borligini
  2. ID lar takrorlanmasligini
  3. Mahsulot nomlari bo'sh emasligini va takrorlanmasligini
  4. Kategoriya / kichik kategoriya ID lari to'g'riligini
  5. Narx, chegirma, ombor, reyting maydonlarining mantiqiy to'g'riligini
  6. Har bir kategoriya uchun belgilangan minimal mahsulot soniga yetganini
  7. (ixtiyoriy) mahsulot rasmlari mavjudligini  --with-images

Ishga tushirish:
    python3 data/scripts/validate_catalog.py
    python3 data/scripts/validate_catalog.py --with-images
"""

from __future__ import annotations

import json
import sys
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"

MIN_TOTAL = 1000

# Kategoriya -> minimal mahsulot soni (texnik topshiriq 5-bo'lim)
MIN_PER_CATEGORY = {
    "fruits-vegetables": 100,
    "meat-poultry-fish": 70,
    "dairy-eggs": 70,
    "bakery-sweets": 80,
    "beverages-water": 70,
    "grocery-staples": 100,
    "household-cleaning": 100,
    "personal-care": 80,
    "baby-products": 50,
    "clothing-shoes": 80,
    "phones-electronics": 60,
    "kitchen-homeware": 50,
    "sport-outdoor": 40,
    "pet-supplies": 30,
    "auto-other": 20,
}

ALLOWED_UNITS = {"kg", "g", "l", "ml", "dona", "qadoq", "juft", "bog"}


class Checker:
    def __init__(self) -> None:
        self.failures: list[str] = []
        self.checks = 0

    def check(self, condition: bool, message: str) -> None:
        self.checks += 1
        if not condition:
            self.failures.append(message)


def normalize(text: str) -> str:
    """O'zbek lotin matnini qidirish uchun normallashtirish (kichik harf)."""
    text = unicodedata.normalize("NFKC", text).strip().lower()
    table = str.maketrans({"‘": "'", "’": "'", "ʼ": "'", "`": "'"})
    return text.translate(table)


def main() -> int:
    with_images = "--with-images" in sys.argv

    catalog = json.loads((DATA_DIR / "catalog.json").read_text(encoding="utf-8"))
    categories = json.loads((DATA_DIR / "categories.json").read_text(encoding="utf-8"))["categories"]
    products = catalog["products"]

    c = Checker()

    # 1. Umumiy son
    c.check(
        len(products) >= MIN_TOTAL,
        f"Kamida {MIN_TOTAL} ta mahsulot kerak, topildi: {len(products)}",
    )

    # 2. Noyob ID lar
    ids = [p["id"] for p in products]
    duplicates = sorted({i for i in ids if ids.count(i) > 1})
    c.check(len(duplicates) == 0, f"Takrorlangan ID lar mavjud: {duplicates[:10]}")
    c.check(len(set(ids)) == len(ids), "ID lar noyob emas")
    c.check(all(i and i.strip() for i in ids), "Ba'zi ID lar bo'sh")

    # 3. Nomlar
    names = [p["name"] for p in products]
    c.check(all(n and n.strip() for n in names), "Ba'zi mahsulot nomlari bo'sh")
    norm_names = [normalize(n) for n in names]
    dup_names = sorted({n for n in norm_names if norm_names.count(n) > 1})
    c.check(len(dup_names) == 0, f"Takrorlangan mahsulot nomlari: {dup_names[:10]}")

    # 4. Kategoriya ID lari
    cat_ids = {cat["id"] for cat in categories}
    sub_by_cat = {cat["id"]: {s["id"] for s in cat["subcategories"]} for cat in categories}
    bad_cat = sorted({p["categoryId"] for p in products if p["categoryId"] not in cat_ids})
    c.check(not bad_cat, f"Kategoriyalar.json da yo'q categoryId lar: {bad_cat}")

    bad_sub = []
    for p in products:
        allowed = sub_by_cat.get(p["categoryId"], set())
        if p["subcategoryId"] not in allowed:
            bad_sub.append((p["id"], p["categoryId"], p["subcategoryId"]))
    c.check(not bad_sub, f"Noto'g'ri subcategoryId: {bad_sub[:10]}")

    # 5. Narx va boshqa maydonlar
    bad_price = [p["id"] for p in products if not isinstance(p["price"], (int, float)) or p["price"] <= 0]
    c.check(not bad_price, f"Narxi noto'g'ri mahsulotlar: {bad_price[:10]}")

    bad_discount = [
        p["id"]
        for p in products
        if p.get("discountPrice") is not None and p["discountPrice"] >= p["price"]
    ]
    c.check(not bad_discount, f"Chegirma narxi asosiy narxdan katta: {bad_discount[:10]}")

    bad_unit = [p["id"] for p in products if p["unit"] not in ALLOWED_UNITS]
    c.check(not bad_unit, f"Noto'g'ri o'lchov birligi: {sorted(set(bad_unit))[:10]}")

    bad_stock = [p["id"] for p in products if p["stock"] < 0 or p["inStock"] != (p["stock"] > 0)]
    c.check(not bad_stock, f"Ombor maydoni mantiqsiz: {bad_stock[:10]}")

    bad_rating = [p["id"] for p in products if not (0.0 <= p["rating"] <= 5.0)]
    c.check(not bad_rating, f"Reyting 0..5 oralig'ida emas: {bad_rating[:10]}")

    c.check(
        all(p.get("description", "").strip() for p in products),
        "Ba'zi mahsulotlarda tavsif yo'q",
    )
    c.check(all(p.get("image", "").strip() for p in products), "Ba'zi mahsulotlarda rasm yo'q")
    c.check(all(p.get("brand", "").strip() for p in products), "Ba'zi mahsulotlarda brend yo'q")

    # 6. Kategoriya bo'yicha minimal sonlar
    counts: dict[str, int] = {}
    for p in products:
        counts[p["categoryId"]] = counts.get(p["categoryId"], 0) + 1
    for cat_id, minimum in MIN_PER_CATEGORY.items():
        actual = counts.get(cat_id, 0)
        c.check(
            actual >= minimum,
            f"Kategoriya '{cat_id}': kamida {minimum} kerak, topildi {actual}",
        )
    c.check(
        len(counts) == len(cat_ids),
        f"Kategoriyalar soni mos emas: katalogda {len(counts)}, categories.json da {len(cat_ids)}",
    )

    # categories.json ichidagi hisoblar katalog bilan mosligi
    for cat in categories:
        c.check(
            cat["productCount"] == counts.get(cat["id"], 0),
            f"{cat['id']}: productCount ({cat['productCount']}) katalogdagi son ({counts.get(cat['id'])}) ga mos emas",
        )
        sub_sum = sum(s["productCount"] for s in cat["subcategories"])
        c.check(
            sub_sum == cat["productCount"],
            f"{cat['id']}: kichik kategoriyalar yig'indisi ({sub_sum}) != {cat['productCount']}",
        )

    # 7. Rasmlar
    if with_images:
        missing = [
            p["image"]
            for p in products
            if not (DATA_DIR / "images" / p["image"]).exists()
        ]
        c.check(not missing, f"Rasm fayllari topilmadi ({len(missing)} ta), masalan: {missing[:5]}")

    # Natija
    total = len(products)
    print(f"Tekshiruvlar soni : {c.checks}")
    print(f"Mahsulotlar soni  : {total} (minimum {MIN_TOTAL})")
    print(f"Kategoriyalar     : {len(categories)}")
    print(f"Kichik bo'limlar  : {sum(len(c['subcategories']) for c in categories)}")
    print(f"Chegirmadagilar   : {sum(1 for p in products if p['discountPrice'])}")
    print(f"Omborda yo'qlar   : {sum(1 for p in products if not p['inStock'])}")
    print(f"Brendlar soni     : {len({p['brand'] for p in products})}")

    if c.failures:
        print("\n❌ XATOLAR:", file=sys.stderr)
        for f in c.failures:
            print(f"  - {f}", file=sys.stderr)
        return 1

    print("\n✅ Barcha tekshiruvlar muvaffaqiyatli o'tdi.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
