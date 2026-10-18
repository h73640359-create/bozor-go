#!/usr/bin/env python3
"""Har bir mahsulot uchun kategoriyasiga mos, noyob rasm yaratish.

Natija: data/images/products/prd-XXXX.webp  (320x320, WebP)
        data/images/banners/bnr-00X.webp     (800x400, WebP)

Rasmlar loyiha ichida maxsus generatsiya qilingan original vektor
illustratsiyalardir (real fotosurat emas, demo katalog uchun).
Har bir mahsulot o'z kichik kategoriyasiga mos ikonka + o'ziga xos
fon gradienti + o'lchov yorlig'ini oladi.

Ishga tushirish:
    python3 data/scripts/generate_images.py
    python3 data/scripts/generate_images.py --limit 20 --sheet /tmp/sheet.png
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))

from catalog_registry import SUBCATEGORIES  # noqa: E402
from icons import ICON_DRAWERS, hex_to_rgb, lighten, make_palette, shift_hue  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
IMG_DIR = DATA_DIR / "images"

SIZE = 320
FONT_PATH = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_PATH_REGULAR = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


def load_font(path: str, size: int) -> ImageFont.FreeTypeFont:
    try:
        return ImageFont.truetype(path, size)
    except OSError:
        return ImageFont.load_default()


def seed_from(text: str) -> int:
    return int(hashlib.sha256(text.encode("utf-8")).hexdigest()[:8], 16)


def vertical_gradient(size: int, top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGB", (size, size))
    d = ImageDraw.Draw(img)
    r1, g1, b1 = top
    r2, g2, b2 = bottom
    for y in range(size):
        t = y / (size - 1)
        d.line(
            [(0, y), (size, y)],
            fill=(
                int(r1 + (r2 - r1) * t),
                int(g1 + (g2 - g1) * t),
                int(b1 + (b2 - b1) * t),
            ),
        )
    return img


def radial_glow(size: int) -> Image.Image:
    """Markazdagi yumshoq yorug'lik (RGBA)."""
    layer = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(layer)
    d.ellipse([size * 0.06, size * 0.02, size * 0.94, size * 0.80], fill=90)
    layer = layer.filter(ImageFilter.GaussianBlur(size * 0.09))
    glow = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    white = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    glow.paste(white, (0, 0), layer)
    return glow


def draw_icon_layer(icon_key: str, palette: dict, box_size: int) -> Image.Image:
    """Ikonkani shaffof qatlamga chizish."""
    layer = Image.new("RGBA", (box_size, box_size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    from icons import Canvas  # local import to avoid cycle at module load

    c = Canvas(d, (0, 0, box_size, box_size), palette)
    drawer = ICON_DRAWERS.get(icon_key, ICON_DRAWERS["box"])
    drawer(c)
    return layer


def unit_pill(base: Image.Image, text: str) -> None:
    """Pastki o'ng burchakka o'lchov yorlig'i."""
    d = ImageDraw.Draw(base)
    font = load_font(FONT_PATH, 26)
    bbox = d.textbbox((0, 0), text, font=font)
    w = bbox[2] - bbox[0] + 30
    h = 46
    x1 = SIZE - 18
    x0 = x1 - w
    y1 = SIZE - 18
    y0 = y1 - h
    overlay = Image.new("RGBA", base.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.rounded_rectangle([x0, y0, x1, y1], radius=h // 2, fill=(255, 255, 255, 235))
    base.alpha_composite(overlay)
    d.text((x0 + 15, y0 + 9), text, font=font, fill=(33, 33, 33, 255))


def make_product_image(product: dict, category_color: str, icon_key: str) -> Image.Image:
    seed = seed_from(product["id"] + product["name"])
    hue = ((seed % 1000) / 1000.0) * 26.0 - 13.0  # -13..+13 daraja
    base_rgb = shift_hue(hex_to_rgb(category_color), hue)

    top = lighten(base_rgb, 0.42)
    bottom = lighten(base_rgb, 0.06)
    img = vertical_gradient(SIZE, top, bottom).convert("RGBA")
    img.alpha_composite(radial_glow(SIZE))

    palette = make_palette(
        "#%02X%02X%02X" % shift_hue(base_rgb, 0),
        hue_shift=0.0,
        accent_hex=None,
    )
    # ikonka asosiy rangini fon bilan uyg'unlashtirish
    palette["main"] = shift_hue(base_rgb, -18)
    palette["light"] = lighten(palette["main"], 0.34)
    palette["lighter"] = lighten(palette["main"], 0.62)
    palette["dark"] = shift_hue(base_rgb, -26)
    palette["darker"] = shift_hue(base_rgb, -40)

    icon_box = 196
    icon_layer = draw_icon_layer(icon_key, palette, icon_box)

    # soya
    shadow = Image.new("RGBA", (icon_box, icon_box), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.ellipse(
        [icon_box * 0.12, icon_box * 0.78, icon_box * 0.88, icon_box * 0.99],
        fill=(20, 30, 40, 70),
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(icon_box * 0.05))
    img.alpha_composite(shadow, (int((SIZE - icon_box) / 2), 34))
    img.alpha_composite(icon_layer, (int((SIZE - icon_box) / 2), 34))

    unit_pill(img, product["unitLabel"])
    return img


def make_banner_image(banner: dict) -> Image.Image:
    w, h = 800, 400
    start = hex_to_rgb(banner["startColor"])
    end = hex_to_rgb(banner["endColor"])
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    for x in range(w):
        t = x / (w - 1)
        d.line(
            [(x, 0), (x, h)],
            fill=(
                int(start[0] + (end[0] - start[0]) * t),
                int(start[1] + (end[1] - start[1]) * t),
                int(start[2] + (end[2] - start[2]) * t),
            ),
        )
    # dekorativ doiralar
    overlay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.ellipse([w - 260, -80, w + 60, 220], fill=(255, 255, 255, 30))
    od.ellipse([w - 160, 200, w + 120, 480], fill=(255, 255, 255, 22))
    od.ellipse([-120, 240, 140, 500], fill=(255, 255, 255, 18))
    img = img.convert("RGBA")
    img.alpha_composite(overlay)

    d = ImageDraw.Draw(img)
    title_font = load_font(FONT_PATH, 46)
    sub_font = load_font(FONT_PATH_REGULAR, 30)
    d.text((48, 130), banner["title"], font=title_font, fill=(255, 255, 255, 255))
    d.text((48, 194), banner["subtitle"], font=sub_font, fill=(255, 255, 255, 230))
    d.rounded_rectangle([48, 260, 250, 320], radius=30, fill=(255, 255, 255, 245))
    d.text((80, 274), "Ko'rish", font=load_font(FONT_PATH, 28), fill=(33, 33, 33, 255))
    return img


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, default=0, help="faqat N ta mahsulot")
    parser.add_argument("--sheet", type=str, default="", help="kontakt varaq (PNG) chiqarish")
    parser.add_argument("--quality", type=int, default=80)
    args = parser.parse_args()

    catalog = json.loads((DATA_DIR / "catalog.json").read_text(encoding="utf-8"))
    categories = json.loads((DATA_DIR / "categories.json").read_text(encoding="utf-8"))["categories"]
    banners = json.loads((DATA_DIR / "banners.json").read_text(encoding="utf-8"))["banners"]

    color_by_cat = {c["id"]: c["color"] for c in categories}
    icon_by_sub = {sid: vals[1] for sid, vals in SUBCATEGORIES.items()}

    products = catalog["products"]
    if args.limit:
        products = products[: args.limit]

    out_products = IMG_DIR / "products"
    out_banners = IMG_DIR / "banners"
    out_products.mkdir(parents=True, exist_ok=True)
    out_banners.mkdir(parents=True, exist_ok=True)

    for i, p in enumerate(products, start=1):
        color = color_by_cat.get(p["categoryId"], "#4CAF50")
        icon_key = icon_by_sub.get(p["subcategoryId"], "box")
        img = make_product_image(p, color, icon_key)
        img.save(out_products / f"{p['id']}.webp", "WEBP", quality=args.quality, method=4)
        if i % 200 == 0:
            print(f"  {i}/{len(products)}")

    for b in banners:
        make_banner_image(b).save(out_banners / f"{b['id']}.webp", "WEBP", quality=82, method=4)

    total = sum(f.stat().st_size for f in out_products.glob("*.webp"))
    print(f"Yaratildi: {len(products)} ta mahsulot rasmi")
    print(f"Jami hajmi: {total / 1024 / 1024:.2f} MB ({out_products})")
    print(f"Bannerlar: {len(banners)} ta -> {out_banners}")

    if args.sheet:
        cols = 8
        icons = sorted(ICON_DRAWERS.keys())
        rows = (len(icons) + cols - 1) // cols
        cell = 140
        sheet = Image.new("RGB", (cols * cell, rows * (cell + 22)), (245, 246, 248))
        d = ImageDraw.Draw(sheet)
        font = load_font(FONT_PATH_REGULAR, 14)
        for idx, key in enumerate(icons):
            r, cidx = divmod(idx, cols)
            x, y = cidx * cell, r * (cell + 22)
            tile = Image.new("RGB", (cell, cell), (232, 238, 245))
            layer = draw_icon_layer(key, make_palette("#5B8DEF"), cell - 20)
            tile.paste(layer, (10, 10), layer)
            sheet.paste(tile, (x, y))
            d.text((x + 6, y + cell + 3), key, font=font, fill=(40, 44, 52))
        sheet.save(args.sheet)
        print(f"Kontakt varaq: {args.sheet}")


if __name__ == "__main__":
    main()
