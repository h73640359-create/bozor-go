#!/usr/bin/env python3
"""Katalog va rasmlarni Android assets papkasiga ko'chirish.

Ishlatish:
    python3 data/scripts/sync_assets.py

data/catalog.json, data/categories.json, data/banners.json   -> app/src/main/assets/seed/
data/images/products/*.webp                                   -> app/src/main/assets/products/
data/images/banners/*.webp                                    -> app/src/main/assets/banners/
data/images/categories/*.webp                                 -> app/src/main/assets/categories/
data/images/fallback/*.webp                                   -> app/src/main/assets/fallback/
"""
from __future__ import annotations

import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app" / "src" / "main" / "assets"

JOBS = [
    (ROOT / "data" / "catalog.json", ASSETS / "seed" / "catalog.json"),
    (ROOT / "data" / "categories.json", ASSETS / "seed" / "categories.json"),
    (ROOT / "data" / "banners.json", ASSETS / "seed" / "banners.json"),
]

DIR_JOBS = [
    (ROOT / "data" / "images" / "products", ASSETS / "products"),
    (ROOT / "data" / "images" / "banners", ASSETS / "banners"),
    (ROOT / "data" / "images" / "categories", ASSETS / "categories"),
    (ROOT / "data" / "images" / "fallback", ASSETS / "fallback"),
]


def main() -> int:
    copied = 0
    for src, dst in JOBS:
        if not src.exists():
            print(f"XATO: {src} topilmadi. Avval build_catalog.py ni ishga tushiring.")
            return 1
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        copied += 1
        print(f"{src.relative_to(ROOT)} -> {dst.relative_to(ROOT)}")

    for src_dir, dst_dir in DIR_JOBS:
        if not src_dir.is_dir():
            print(f"OGOHLANTIRISH: {src_dir} topilmadi, o'tkazib yuborildi.")
            continue
        dst_dir.mkdir(parents=True, exist_ok=True)
        for image in sorted(src_dir.glob("*.webp")):
            target = dst_dir / image.name
            if not target.exists() or target.stat().st_mtime < image.stat().st_mtime:
                shutil.copy2(image, target)
                copied += 1
        print(f"{src_dir.relative_to(ROOT)} -> {dst_dir.relative_to(ROOT)}")

    print(f"Tayyor: {copied} ta fayl yangilandi/yuklandi.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
