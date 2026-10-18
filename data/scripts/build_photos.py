#!/usr/bin/env python3
"""Generatsiya qilingan xom rasmlarni katalog rasmi (WebP) ga aylantirish.

Kirish:  RAW_DIR/<kichik-bo'lim>__<variant>.png   (masalan fresh-fruits__0.png)
Chiqish: data/images/products/<kichik-bo'lim>__<variant>.webp  (448x448, WebP)

Nima qiladi:
  1. Oq fon chetlarini avtomatik kesadi (mahsulot kadrga to'liq chiqadi);
  2. Kvadratga o'tkazib, 448x448 ga kichraytiradi;
  3. WebP (quality 82) sifatida saqlaydi — APK hajmi oshmaydi.

Foto bo'lmagan kichik bo'limlar uchun avvalgi chizma illustratsiya
(`products/<mahsulot-id>.webp`) saqlanib qoladi — bo'sh karta hech qachon chiqmaydi.

Ishlatish:
    python3 data/scripts/build_photos.py [--raw ~/photos_raw] [--size 448]
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
OUT_DIR = DATA_DIR / "images" / "products"
DEFAULT_RAW = Path("/home/user/photos_raw")

BG_TOLERANCE = 248  # shu qiymatdan yuqori (har 3 kanalda) piksel — fon
CONTENT_FILL = 0.94  # mahsulot kadrning necha foizini egallashi kerak


def trim_white(image: Image.Image, tolerance: int = BG_TOLERANCE) -> Image.Image:
    """Atrofdagi oq fonni kesib tashlaydi."""
    rgb = image.convert("RGB")
    background = Image.new("RGB", rgb.size, (255, 255, 255))
    diff = ImageChops.difference(rgb, background)
    # farq kichik bo'lgan joylar fon hisoblanadi
    mask = diff.convert("L").point(lambda value: 255 if value > 255 - tolerance + 240 else 0)
    bbox = mask.point(lambda value: 255 if value == 0 else 0).getbbox()
    # getbbox() fon qismi uchun; teskarisini hisoblash
    inverted = mask.point(lambda value: 0 if value else 255)
    bbox = inverted.getbbox()
    if bbox:
        width, height = rgb.size
        # kadrning 15% dan kam qismini egallagan kesma — xato, e'tibor bermaymiz
        box_area = (bbox[2] - bbox[0]) * (bbox[3] - bbox[1])
        if box_area > 0.05 * width * height:
            return rgb.crop(bbox)
    return rgb


def fit_square(image: Image.Image, size: int, fill: float = CONTENT_FILL) -> Image.Image:
    """Kesilgan mahsulotni oq kvadrat fonning o'rtasiga joylashtiradi."""
    image = image.copy()
    image.thumbnail((int(size * fill), int(size * fill)), Image.LANCZOS)
    canvas = Image.new("RGB", (size, size), (255, 255, 255))
    offset = ((size - image.width) // 2, (size - image.height) // 2)
    canvas.paste(image, offset)
    return canvas


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--raw", type=str, default=str(DEFAULT_RAW))
    parser.add_argument("--size", type=int, default=448)
    parser.add_argument("--quality", type=int, default=82)
    args = parser.parse_args()

    raw_dir = Path(args.raw).expanduser()
    if not raw_dir.is_dir():
        print(f"XATO: xom rasmlar papkasi topilmadi: {raw_dir}")
        return 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    total_bytes = 0
    count = 0
    for path in sorted(raw_dir.glob("*.png")):
        stem = path.stem
        if "__" not in stem:
            print(f"  OGOHLANTIRISH: nom noto'g'ri, o'tkazib yuborildi: {path.name}")
            continue
        image = Image.open(path)
        image = trim_white(image)
        image = fit_square(image, args.size)
        out_path = OUT_DIR / f"{stem}.webp"
        image.save(out_path, "WEBP", quality=args.quality, method=4)
        total_bytes += out_path.stat().st_size
        count += 1

    if count == 0:
        print("XATO: hech qanday rasm qayta ishlanmadi.")
        return 1

    print(f"Tayyor: {count} ta rasm -> {OUT_DIR}")
    print(f"Jami hajm: {total_bytes / 1024 / 1024:.2f} MB ({total_bytes // count // 1024} KB o'rtacha)")

    pruned = prune_unused()
    if pruned:
        print(f"O'chirildi: {pruned} ta ishlatilmayotgan eski rasm")
    return 0


def prune_unused() -> int:
    """Katalogda ishlatilmayotgan eski rasmlarni o'chiradi (APK shishib ketmasin).

    Fotoga o'tkazilgan kichik bo'limlarning eski chizma illustratsiyalari
    endi kerak emas — ular data/ va app/src/main/assets/ dan tozalanadi.
    """
    import json

    catalog_path = DATA_DIR / "catalog.json"
    if not catalog_path.is_file():
        print("  OGOHLANTIRISH: catalog.json yo'q, eski rasmlar o'chirilmadi")
        return 0
    products = json.loads(catalog_path.read_text(encoding="utf-8"))["products"]
    used = {p["image"] for p in products if p.get("image")}

    removed = 0
    for folder in (OUT_DIR, ROOT / "app" / "src" / "main" / "assets" / "products"):
        if not folder.is_dir():
            continue
        for path in folder.glob("prd-*.webp"):  # faqat eski chizma illustratsiyalar
            if f"products/{path.name}" not in used:
                path.unlink()
                removed += 1
    return removed


if __name__ == "__main__":
    sys.exit(main())
