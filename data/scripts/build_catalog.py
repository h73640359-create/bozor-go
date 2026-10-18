#!/usr/bin/env python3
"""BozorGo katalogini yig'ish skripti.

`catalog_data/` ichidagi xom ma'lumotlardan:
  * data/catalog.json      — 1000+ noyob mahsulot
  * data/categories.json   — kategoriya va kichik kategoriyalar
  * data/banners.json      — bosh sahifa bannerlari

Natija deterministik: bir xil SEED bilan bir xil fayl hosil bo'ladi.

Ishga tushirish:
    python3 data/scripts/build_catalog.py
"""

from __future__ import annotations

import json
import random
import sys
from datetime import date, timedelta
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from catalog_registry import CATEGORY_COLORS, SUBCATEGORIES, UNIT_LABELS  # noqa: E402
from catalog_data import MODULES  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
SEED = 20260118
TODAY = date(2026, 1, 18)

# Variantlar (o'lcham / rang) — faqat kerakli kichik kategoriyalar uchun
SIZE_OPTIONS_CLOTHES = ["S", "M", "L", "XL", "XXL"]
SIZE_OPTIONS_SHOES = ["38", "39", "40", "41", "42", "43"]
SIZE_OPTIONS_KIDS = ["2-3 yosh", "4-5 yosh", "6-7 yosh", "8-9 yosh", "10-12 yosh"]
SIZE_OPTIONS_UNDERWEAR = ["S", "M", "L", "XL"]
COLOR_OPTIONS = ["Qora", "Oq", "Ko'k", "Kulrang"]

VARIANTS = {
    "mens-clothing": ("O'lcham", SIZE_OPTIONS_CLOTHES),
    "womens-clothing": ("O'lcham", SIZE_OPTIONS_CLOTHES),
    "kids-clothing": ("Yosh", SIZE_OPTIONS_KIDS),
    "footwear": ("O'lcham", SIZE_OPTIONS_SHOES),
    "underwear-socks": ("O'lcham", SIZE_OPTIONS_UNDERWEAR),
    "phones": ("Rang", COLOR_OPTIONS),
    "phone-accessories": ("Rang", COLOR_OPTIONS),
    "audio": ("Rang", COLOR_OPTIONS),
    "power": ("Rang", COLOR_OPTIONS),
    "toys": ("Rang", ["Ko'p rangli", "Qizil", "Ko'k", "Sariq"]),
    "home-textile": ("O'lcham", ["1 kishilik", "1,5 kishilik", "2 kishilik", "Yevro"]),
}

DESCRIPTION_TEMPLATES = [
    "{name}. {brand} brendining sifatli mahsuloti. {hint} O'lchov birligi: {unit}. "
    "Tavsif namunaviy (demo) ma'lumot bo'lib, haqiqiy do'kon katalogi emas.",
    "{name} — kundalik ehtiyoj uchun {brand} mahsuloti. {hint} Ombordagi qoldiq har kuni "
    "yangilanadi. Bu demo katalog yozuvi.",
    "{name}. {hint} Ishlab chiqaruvchi: {brand}. Yetkazib berish shartlari buyurtma "
    "rasmiylashtirilganda ko'rsatiladi. Namunaviy demo tavsif.",
]


def parse_units(raw_value: str) -> float:
    v = float(raw_value)
    return int(v) if v == int(v) else v


def format_unit_label(unit: str, value: float) -> str:
    """Masalan: (kg, 1) -> '1 kg'; (ml, 500) -> '500 ml'."""
    if isinstance(value, float) and value == int(value):
        value = int(value)
    text = f"{value}".replace(".", ",")
    return f"{text} {UNIT_LABELS.get(unit, unit)}"


def build() -> dict:
    rng = random.Random(SEED)
    products: list[dict] = []
    per_subcategory: dict[str, int] = {}
    per_category: dict[str, int] = {}
    seen_names: set[str] = set()
    errors: list[str] = []

    for module in MODULES:
        category_id = module.CATEGORY_ID
        category_name = module.CATEGORY_NAME
        for line_no, raw_line in enumerate(module.RAW.strip().splitlines(), start=1):
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue
            parts = [p.strip() for p in line.split("|")]
            if len(parts) != 6:
                errors.append(
                    f"{module.__name__}:{line_no}: noto'g'ri format (6 ta maydon kerak): {line}"
                )
                continue
            name, subcategory_id, brand, unit, unit_value_raw, price_raw = parts

            if subcategory_id not in SUBCATEGORIES:
                errors.append(f"{module.__name__}:{line_no}: noma'lum kichik kategoriya: {subcategory_id}")
                continue
            if not name:
                errors.append(f"{module.__name__}:{line_no}: mahsulot nomi bo'sh")
                continue

            key = name.lower()
            if key in seen_names:
                errors.append(f"Takroriy mahsulot nomi: {name}")
            seen_names.add(key)

            product_id = f"prd-{len(products) + 1:04d}"
            price = int(price_raw)
            unit_value = parse_units(unit_value_raw)

            # Chegirma: har ~4-mahsulotga
            has_discount = rng.random() < 0.32
            discount_price = None
            discount_percent = 0
            if has_discount:
                factor = rng.choice([0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9])
                discount_price = int(round(price * factor) / 100) * 100
                if discount_price >= price:
                    discount_price = price - 500
                discount_percent = int(round((1 - discount_price / price) * 100))

            # Ombor: ~4% mahsulot tugagan
            stock = 0 if rng.random() < 0.04 else rng.randint(4, 600)

            rating = round(rng.uniform(3.6, 5.0), 1)
            review_count = rng.randint(3, 480)

            is_new = rng.random() < 0.12
            is_popular = rating >= 4.5 and review_count >= 120
            days_ago = rng.randint(0, 14) if is_new else rng.randint(15, 240)
            created_at = (TODAY - timedelta(days=days_ago)).isoformat()

            hint = SUBCATEGORIES[subcategory_id][2]
            template = DESCRIPTION_TEMPLATES[len(products) % len(DESCRIPTION_TEMPLATES)]
            description = template.format(
                name=name, brand=brand, hint=hint, unit=format_unit_label(unit, unit_value)
            )

            option_name, option_values = VARIANTS.get(subcategory_id, (None, None))
            options = []
            if option_name:
                options = [{"name": option_name, "values": list(option_values)}]

            subcategory_name, subcategory_icon, _ = SUBCATEGORIES[subcategory_id]

            products.append(
                {
                    "id": product_id,
                    "name": name,
                    "categoryId": category_id,
                    "categoryName": category_name,
                    "subcategoryId": subcategory_id,
                    "subcategoryName": subcategory_name,
                    "brand": brand,
                    "image": f"products/{product_id}.webp",
                    "imageUrl": "",  # backend/admin tomonidan to'ldiriladi
                    "description": description,
                    "price": price,
                    "discountPrice": discount_price,
                    "discountPercent": discount_percent,
                    "unit": unit,
                    "unitValue": unit_value,
                    "unitLabel": format_unit_label(unit, unit_value),
                    "stock": stock,
                    "inStock": stock > 0,
                    "rating": rating,
                    "reviewCount": review_count,
                    "isPopular": is_popular,
                    "isNew": is_new,
                    "isDeal": has_discount,
                    "options": options,
                    "tags": [category_name.lower(), subcategory_name.lower(), brand.lower()],
                    "createdAt": created_at,
                    "isDemo": True,
                }
            )
            per_subcategory[subcategory_id] = per_subcategory.get(subcategory_id, 0) + 1
            per_category[category_id] = per_category.get(category_id, 0) + 1

    if errors:
        print("XATOLAR:", file=sys.stderr)
        for e in errors:
            print(f"  - {e}", file=sys.stderr)
        raise SystemExit(1)

    # Kategoriyalar
    categories = []
    for module in MODULES:
        category_id = module.CATEGORY_ID
        subs = []
        for sub_id, (sub_name, sub_icon, _hint) in SUBCATEGORIES.items():
            count = per_subcategory.get(sub_id, 0)
            if count == 0:
                continue
            # kichik kategoriya qaysi asosiy kategoriyaga tegishli?
            if any(p["categoryId"] == category_id and p["subcategoryId"] == sub_id for p in products):
                subs.append(
                    {
                        "id": sub_id,
                        "name": sub_name,
                        "icon": sub_icon,
                        "productCount": count,
                    }
                )
        categories.append(
            {
                "id": category_id,
                "name": module.CATEGORY_NAME,
                "icon": module.ICON,
                "color": CATEGORY_COLORS.get(category_id, "#4CAF50"),
                "productCount": per_category.get(category_id, 0),
                "subcategories": subs,
            }
        )

    banners = [
        {
            "id": "bnr-001",
            "title": "Birinchi buyurtmangizga",
            "subtitle": "15% chegirma",
            "image": "banners/bnr-001.webp",
            "actionType": "category",
            "actionValue": "fruits-vegetables",
            "startColor": "#2E7D32",
            "endColor": "#66BB6A",
        },
        {
            "id": "bnr-002",
            "title": "Kunlik yangi mahsulotlar",
            "subtitle": "Har kuni ertalab yetkazib beramiz",
            "image": "banners/bnr-002.webp",
            "actionType": "category",
            "actionValue": "dairy-eggs",
            "startColor": "#1565C0",
            "endColor": "#42A5F5",
        },
        {
            "id": "bnr-003",
            "title": "Uy-ro'zg'or haftaligi",
            "subtitle": "Tozalash vositalariga -25%",
            "image": "banners/bnr-003.webp",
            "actionType": "category",
            "actionValue": "household-cleaning",
            "startColor": "#00695C",
            "endColor": "#26A69A",
        },
        {
            "id": "bnr-004",
            "title": "Elektronika bo'limi",
            "subtitle": "Aksessuarlar 2+1",
            "image": "banners/bnr-004.webp",
            "actionType": "category",
            "actionValue": "phones-electronics",
            "startColor": "#283593",
            "endColor": "#5C6BC0",
        },
        {
            "id": "bnr-005",
            "title": "Bolalar dunyosi",
            "subtitle": "Taglik va ovqatlar to'plami",
            "image": "banners/bnr-005.webp",
            "actionType": "category",
            "actionValue": "baby-products",
            "startColor": "#AD1457",
            "endColor": "#F06292",
        },
    ]

    catalog = {
        "version": 1,
        "generatedAt": TODAY.isoformat(),
        "currency": "UZS",
        "isDemoData": True,
        "notice": (
            "Ushbu katalog namunaviy (demo) ma'lumotlardan iborat. Narxlar, brendlar, "
            "reytinglar va sharhlar to'qima bo'lib, real do'kon ma'lumotlari emas."
        ),
        "productCount": len(products),
        "products": products,
    }

    return {"catalog": catalog, "categories": categories, "banners": banners}


def main() -> None:
    data = build()
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    (DATA_DIR / "catalog.json").write_text(
        json.dumps(data["catalog"], ensure_ascii=False, indent=1) + "\n", encoding="utf-8"
    )
    (DATA_DIR / "categories.json").write_text(
        json.dumps({"categories": data["categories"]}, ensure_ascii=False, indent=1) + "\n",
        encoding="utf-8",
    )
    (DATA_DIR / "banners.json").write_text(
        json.dumps({"banners": data["banners"]}, ensure_ascii=False, indent=1) + "\n",
        encoding="utf-8",
    )

    print(f"Mahsulotlar soni: {data['catalog']['productCount']}")
    for cat in data["categories"]:
        print(f"  {cat['name']:<45} {cat['productCount']:>4}  ({len(cat['subcategories'])} kichik bo'lim)")
    print("Yozildi: data/catalog.json, data/categories.json, data/banners.json")


if __name__ == "__main__":
    main()
