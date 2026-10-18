#!/usr/bin/env python3
"""Har bir kategoriya uchun karta rasmi va zaxira (fallback) rasm yaratish.

Chiqadi:
    data/images/categories/<categoryId>.webp   — katalog kartasi uchun (480x320)
    data/images/fallback/<categoryId>.webp     — mahsulot rasmi topilmaganda (320x320)

Rasmlar loyiha ichida generatsiya qilingan original illustratsiyalar bo'lib,
har bir kategoriya o'z rang palitrasi va o'ziga tegishli kichik bo'lim
ikonkalaridan tuziladi (bitta rasm hamma joyda takrorlanmaydi).

Ishga tushirish:
    python3 data/scripts/generate_category_images.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))

from catalog_registry import SUBCATEGORIES  # noqa: E402
from icons import (  # noqa: E402
    ICON_DRAWERS,
    Canvas,
    hex_to_rgb,
    lighten,
    make_palette,
    shift_hue,
)

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
IMG_DIR = DATA_DIR / "images"

CARD_SIZE = (480, 320)
FALLBACK_SIZE = (320, 320)

FONT_PATH = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_PATH_REGULAR = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


def load_font(path: str, size: int):
    try:
        from PIL import ImageFont

        return ImageFont.truetype(path, size)
    except OSError:
        from PIL import ImageFont

        return ImageFont.load_default()


def vertical_gradient(size: tuple[int, int], top, bottom) -> Image.Image:
    w, h = size
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / (h - 1)
        d.line(
            [(0, y), (w, y)],
            fill=(
                int(top[0] + (bottom[0] - top[0]) * t),
                int(top[1] + (bottom[1] - top[1]) * t),
                int(top[2] + (bottom[2] - top[2]) * t),
            ),
        )
    return img


def icon_layer(icon_key: str, palette: dict, size: int) -> Image.Image:
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    canvas = Canvas(d, (0, 0, size, size), palette)
    ICON_DRAWERS.get(icon_key, ICON_DRAWERS["box"])(canvas)
    return layer


def palette_for(color_hex: str) -> dict:
    base = hex_to_rgb(color_hex)
    palette = make_palette(color_hex, hue_shift=0.0)
    palette["main"] = shift_hue(base, -18)
    palette["light"] = lighten(palette["main"], 0.34)
    palette["lighter"] = lighten(palette["main"], 0.62)
    palette["dark"] = shift_hue(base, -26)
    palette["darker"] = shift_hue(base, -40)
    return palette


def make_category_card(category: dict, icon_keys: list[str]) -> Image.Image:
    """Katalog kartasi: gradient fon + 3 ta kichik bo'lim ikonkasi."""
    w, h = CARD_SIZE
    base_rgb = hex_to_rgb(category["color"])
    img = vertical_gradient(
        CARD_SIZE, lighten(base_rgb, 0.40), lighten(base_rgb, 0.04)
    ).convert("RGBA")
    palette = palette_for(category["color"])

    keys = (icon_keys + ["box", "box", "box"])[:3]
    box = 132
    gap = 18
    total = box * 3 + gap * 2
    start_x = (w - total) // 2
    y = (h - box) // 2 - 6

    for index, key in enumerate(keys):
        x = start_x + index * (box + gap)
        layer = icon_layer(key, palette, box)
        shadow = Image.new("RGBA", (box, box), (0, 0, 0, 0))
        sd = ImageDraw.Draw(shadow)
        sd.ellipse([box * 0.12, box * 0.82, box * 0.88, box * 0.99], fill=(20, 30, 40, 60))
        shadow = shadow.filter(ImageFilter.GaussianBlur(box * 0.05))
        img.alpha_composite(shadow, (x, y))
        img.alpha_composite(layer, (x, y))

    return img


def make_fallback(category: dict, icon_key: str) -> Image.Image:
    """Mahsulot rasmi topilmaganda ko'rsatiladigan kategoriya rasmi."""
    size = FALLBACK_SIZE[0]
    base_rgb = hex_to_rgb(category["color"])
    img = vertical_gradient(
        FALLBACK_SIZE, lighten(base_rgb, 0.36), lighten(base_rgb, 0.02)
    ).convert("RGBA")
    palette = palette_for(category["color"])

    box = 210
    layer = icon_layer(icon_key, palette, box)
    x = (size - box) // 2
    y = (size - box) // 2 - 8
    shadow = Image.new("RGBA", (box, box), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.ellipse([box * 0.12, box * 0.82, box * 0.88, box * 0.99], fill=(20, 30, 40, 60))
    shadow = shadow.filter(ImageFilter.GaussianBlur(box * 0.05))
    img.alpha_composite(shadow, (x, y))
    img.alpha_composite(layer, (x, y))
    return img


def main() -> int:
    categories = json.loads((DATA_DIR / "categories.json").read_text(encoding="utf-8"))["categories"]
    catalog = json.loads((DATA_DIR / "catalog.json").read_text(encoding="utf-8"))
    icon_by_sub = {sid: vals[1] for sid, vals in SUBCATEGORIES.items()}

    # Har bir kategoriya uchun eng ko'p mahsulotli kichik bo'limlar
    sub_icons: dict[str, list[tuple[int, str]]] = {}
    counts: dict[str, dict[str, int]] = {}
    for product in catalog["products"]:
        cid = product["categoryId"]
        sub_icons.setdefault(cid, {})
        sub_icons[cid].setdefault(product["subcategoryId"], product["subcategoryId"])
        counts.setdefault(cid, {})
        counts[cid][product["subcategoryId"]] = counts[cid].get(product["subcategoryId"], 0) + 1

    out_cards = IMG_DIR / "categories"
    out_fallback = IMG_DIR / "fallback"
    out_cards.mkdir(parents=True, exist_ok=True)
    out_fallback.mkdir(parents=True, exist_ok=True)

    card_bytes = 0
    fallback_bytes = 0
    for category in categories:
        cid = category["id"]
        ordered = sorted(
            counts.get(cid, {}).items(), key=lambda item: (-item[1], item[0])
        )
        icon_keys = [icon_by_sub.get(sub, "box") for sub, _ in ordered]
        # Takrorlanuvchi ikonkalarni olib tashlab, 3 xilini olamiz.
        deduped: list[str] = []
        for key in icon_keys:
            if key == "box" and len(icon_keys) > 1:
                continue
            if key in ICON_DRAWERS and key not in deduped:
                deduped.append(key)
            if len(deduped) == 3:
                break
        icon_keys = deduped or ["box"]
        main_icon = icon_keys[0]

        card = make_category_card(category, icon_keys)
        card_path = out_cards / f"{cid}.webp"
        card.save(card_path, "WEBP", quality=82, method=4)
        card_bytes += card_path.stat().st_size

        fallback = make_fallback(category, main_icon)
        fallback_path = out_fallback / f"{cid}.webp"
        fallback.save(fallback_path, "WEBP", quality=82, method=4)
        fallback_bytes += fallback_path.stat().st_size

        print(f"  {cid}: {', '.join(icon_keys)}")

    print(f"Kategoriya rasmlari: {len(categories)} ta, {card_bytes / 1024:.0f} KB -> {out_cards}")
    print(f"Zaxira rasmlar:      {len(categories)} ta, {fallback_bytes / 1024:.0f} KB -> {out_fallback}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
