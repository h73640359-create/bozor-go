#!/usr/bin/env python3
"""BozorGo brend aktivlarini (ilova ikonkasi, logotip) yaratish.

Chiquvchi fayllar:
  app/src/main/res/mipmap-*/ic_launcher.png          — odatiy ikonka
  app/src/main/res/mipmap-*/ic_launcher_round.png    — yumaloq ikonka
  app/src/main/res/mipmap-*/ic_launcher_foreground.png — adaptive foreground
  app/src/main/res/drawable/ic_launcher_background.png — adaptive background
  app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
  app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
  docs/brand/logo-512.png, docs/brand/logo-horizontal.png
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "app" / "src" / "main" / "res"
BRAND_DIR = ROOT / "docs" / "brand"

BRAND_GREEN_TOP = (22, 183, 106)      # #16B76A
BRAND_GREEN_BOTTOM = (8, 133, 80)     # #088550
BRAND_ORANGE = (255, 138, 0)          # #FF8A00
WHITE = (255, 255, 255, 255)

DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xxhdpi": 3.0,
    "xhdpi": 2.0,
    "xxxhdpi": 4.0,
}
LAUNCHER_SIZE = 48.0  # dp


def draw_glyph(size: int) -> Image.Image:
    """Oq savatcha (sumka) belgisi — shaffof fonda."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = size / 200.0

    def px(v: float) -> float:
        return v * s

    # Sumka tanasi
    d.rounded_rectangle(
        [px(44), px(72), px(156), px(178)],
        radius=int(px(18)),
        fill=WHITE,
    )
    # Sumka tutqichi (ichki bo'shliq bilan)
    d.arc(
        [px(64), px(38), px(136), px(126)],
        start=180,
        end=360,
        fill=WHITE,
        width=int(px(15)),
    )
    # Old chiziq (dizayn detali)
    d.rounded_rectangle(
        [px(76), px(104), px(124), px(146)],
        radius=int(px(8)),
        fill=(22, 183, 106, 255),
    )
    # Yashil barg aksenti
    d.polygon(
        [(px(112), px(96)), (px(146), px(112)), (px(118), px(134)), (px(104), px(120))],
        fill=(255, 138, 0, 255),
    )
    return img


def draw_background(size: int) -> Image.Image:
    img = Image.new("RGB", (size, size), BRAND_GREEN_TOP)
    d = ImageDraw.Draw(img)
    for y in range(size):
        t = y / max(1, size - 1)
        d.line(
            [(0, y), (size, y)],
            fill=(
                int(BRAND_GREEN_TOP[0] + (BRAND_GREEN_BOTTOM[0] - BRAND_GREEN_TOP[0]) * t),
                int(BRAND_GREEN_TOP[1] + (BRAND_GREEN_BOTTOM[1] - BRAND_GREEN_TOP[1]) * t),
                int(BRAND_GREEN_TOP[2] + (BRAND_GREEN_BOTTOM[2] - BRAND_GREEN_TOP[2]) * t),
            ),
        )
    # diagonal yorug'lik
    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.polygon(
        [(0, size * 0.62), (size * 0.72, 0), (size, 0), (size * 0.18, size)],
        fill=(255, 255, 255, 26),
    )
    img = img.convert("RGBA")
    img.alpha_composite(overlay)
    return img.convert("RGB")


def launcher_icon(size: int, round_icon: bool = False) -> Image.Image:
    bg = draw_background(size)
    if round_icon:
        mask = Image.new("L", (size * 4, size * 4), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, size * 4, size * 4), fill=255)
        mask = mask.resize((size, size), Image.LANCZOS)
        out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        out.paste(bg, (0, 0), mask)
        bg = out.convert("RGBA")
    else:
        bg = bg.convert("RGBA")
    glyph = draw_glyph(int(size * 0.72))
    bg.alpha_composite(glyph, (int(size * 0.14), int(size * 0.14)))
    return bg


def main() -> None:
    (BRAND_DIR).mkdir(parents=True, exist_ok=True)

    # Adaptive icon: 108 dp -> 432 px (xxxhdpi)
    adaptive = 432
    (RES / "drawable").mkdir(parents=True, exist_ok=True)
    draw_background(adaptive).save(RES / "drawable" / "ic_launcher_background.png")

    for name, scale in DENSITIES.items():
        size = int(LAUNCHER_SIZE * scale)
        folder = RES / f"mipmap-{name}"
        folder.mkdir(parents=True, exist_ok=True)
        launcher_icon(size).save(folder / "ic_launcher.png")
        launcher_icon(size, round_icon=True).save(folder / "ic_launcher_round.png")
        # Adaptive foreground: 108 dp, faqat markaziy 72 dp xavfsiz zona ishlatiladi
        fg_size = int(108 * scale)
        fg = Image.new("RGBA", (fg_size, fg_size), (0, 0, 0, 0))
        glyph = draw_glyph(int(fg_size * 0.62))
        fg.alpha_composite(glyph, (int(fg_size * 0.19), int(fg_size * 0.19)))
        fg.save(folder / "ic_launcher_foreground.png")

    anydpi = RES / "mipmap-anydpi-v26"
    anydpi.mkdir(parents=True, exist_ok=True)
    adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"""
    (anydpi / "ic_launcher.xml").write_text(adaptive_xml, encoding="utf-8")
    (anydpi / "ic_launcher_round.xml").write_text(adaptive_xml, encoding="utf-8")

    # Play Store / hujjatlar uchun
    launcher_icon(512).save(BRAND_DIR / "logo-512.png")

    # Gorizontal logotip (hujjatlar uchun)
    w, h = 900, 260
    banner = Image.new("RGBA", (w, h), (255, 255, 255, 0))
    mark = launcher_icon(220)
    banner.alpha_composite(mark, (20, 20))
    d = ImageDraw.Draw(banner)
    try:
        from PIL import ImageFont

        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 96)
        small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 40)
    except OSError:
        font = small = ImageFont.load_default()
    d.text((270, 78), "Bozor", font=font, fill=(12, 92, 60, 255))
    d.text((560, 78), "Go", font=font, fill=(255, 138, 0, 255))
    d.text((275, 178), "Online do'kon — O'zbekiston", font=small, fill=(90, 100, 110, 255))
    banner.save(BRAND_DIR / "logo-horizontal.png")

    print("Brend aktivlari yaratildi:")
    print(f"  {RES/'drawable'/'ic_launcher_background.png'}")
    print(f"  mipmap-* ikonkalar ({', '.join(DENSITIES)})")
    print(f"  {anydpi/'ic_launcher.xml'}")
    print(f"  {BRAND_DIR/'logo-512.png'}, {BRAND_DIR/'logo-horizontal.png'}")


if __name__ == "__main__":
    main()
