"""Mahsulot rasmlari uchun vektor ikonka kutubxonasi (Pillow asosida).

Har bir ikonka 200x200 bo'lgan normallashtirilgan koordinatalar sistemasida
chiziladi va keyin kerakli o'lchamga masshtablanadi.

Bu yerda chizilgan tasvirlar — BozorGo loyihasi uchun maxsus yaratilgan
original vektor illustratsiyalar (demo ma'lumotlar uchun). Ular real fotosurat
emas, lekin har bir mahsulot o'z kategoriyasiga mos tasvir oladi.
"""

from __future__ import annotations

import colorsys

from PIL import ImageDraw


# ---------------------------- rang yordamchilari ----------------------------


def hex_to_rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4))  # type: ignore[return-value]


def rgb_to_hex(rgb: tuple[int, int, int]) -> str:
    return "#%02X%02X%02X" % rgb


def shift_hue(rgb: tuple[int, int, int], degrees: float) -> tuple[int, int, int]:
    r, g, b = [v / 255 for v in rgb]
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    h = (h + degrees / 360.0) % 1.0
    r, g, b = colorsys.hls_to_rgb(h, l, s)
    return (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)))


def lighten(rgb: tuple[int, int, int], amount: float) -> tuple[int, int, int]:
    r, g, b = [v / 255 for v in rgb]
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    l = min(1.0, l + (1 - l) * amount)
    r, g, b = colorsys.hls_to_rgb(h, l, s)
    return (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)))


def darken(rgb: tuple[int, int, int], amount: float) -> tuple[int, int, int]:
    r, g, b = [v / 255 for v in rgb]
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    l = max(0.0, l * (1 - amount))
    r, g, b = colorsys.hls_to_rgb(h, l, s)
    return (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)))


def with_alpha(rgb: tuple[int, int, int], alpha: int) -> tuple[int, int, int, int]:
    return (rgb[0], rgb[1], rgb[2], alpha)


def make_palette(base_hex: str, hue_shift: float = 0.0, accent_hex: str | None = None):
    """Kategoriya rangidan to'liq palitra yasash."""
    base = shift_hue(hex_to_rgb(base_hex), hue_shift)
    accent = hex_to_rgb(accent_hex) if accent_hex else shift_hue(base, 150)
    return {
        "main": base,
        "light": lighten(base, 0.32),
        "lighter": lighten(base, 0.62),
        "dark": darken(base, 0.42),
        "darker": darken(base, 0.6),
        "accent": accent,
        "accent_light": lighten(accent, 0.35),
        "white": (255, 255, 255),
        "glass": (255, 255, 255),
    }


# ------------------------------ chizish kanvasi -----------------------------


class Canvas:
    """200x200 normallashtirilgan koordinatalarda chizish."""

    SIZE = 200.0

    def __init__(self, draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], palette: dict):
        self.d = draw
        self.x0, self.y0, self.x1, self.y1 = box
        self.p = palette
        self.k = (self.x1 - self.x0) / self.SIZE

    # -- koordinatalar --
    def sx(self, v: float) -> float:
        return self.x0 + v * self.k

    def sy(self, v: float) -> float:
        return self.y0 + v * self.k

    def pt(self, x: float, y: float) -> tuple[float, float]:
        return (self.sx(x), self.sy(y))

    def box(self, x0: float, y0: float, x1: float, y1: float) -> list[float]:
        return [self.sx(x0), self.sy(y0), self.sx(x1), self.sy(y1)]

    def scale(self, v: float) -> float:
        return v * self.k

    # -- primitivlar --
    def ellipse(self, x0, y0, x1, y1, fill, outline=None, width=0):
        o = None if outline is None else outline
        self.d.ellipse(self.box(x0, y0, x1, y1), fill=fill, outline=o, width=int(self.scale(width)))

    def circle(self, cx, cy, r, fill, outline=None, width=0):
        self.ellipse(cx - r, cy - r, cx + r, cy + r, fill, outline, width)

    def rounded(self, x0, y0, x1, y1, radius, fill, outline=None, width=0):
        self.d.rounded_rectangle(
            self.box(x0, y0, x1, y1),
            radius=int(self.scale(radius)),
            fill=fill,
            outline=outline,
            width=int(self.scale(width)),
        )

    def rect(self, x0, y0, x1, y1, fill, outline=None, width=0):
        self.d.rectangle(self.box(x0, y0, x1, y1), fill=fill, outline=outline, width=int(self.scale(width)))

    def poly(self, points, fill, outline=None, width=0):
        pts = [self.pt(x, y) for x, y in points]
        self.d.polygon(pts, fill=fill, outline=outline)
        if outline and width:
            pts.append(pts[0])
            self.d.line(pts, fill=outline, width=int(self.scale(width)), joint="curve")

    def line(self, points, fill, width=6, joint="curve"):
        pts = [self.pt(x, y) for x, y in points]
        self.d.line(pts, fill=fill, width=int(self.scale(width)), joint=joint)

    def arc(self, x0, y0, x1, y1, start, end, fill, width=8):
        self.d.arc(
            self.box(x0, y0, x1, y1),
            start=start,
            end=end,
            fill=fill,
            width=int(self.scale(width)),
        )

    def pieslice(self, x0, y0, x1, y1, start, end, fill):
        self.d.pieslice(self.box(x0, y0, x1, y1), start=start, end=end, fill=fill)

    # -- palitra --
    def c(self, key: str, alpha: int | None = None):
        color = self.p[key]
        if alpha is None:
            return color
        return with_alpha(color, alpha)

    def shine(self, cx, cy, r, alpha=90):
        """Yaltiroq nuqta."""
        self.ellipse(cx - r * 0.55, cy - r, cx - r * 0.1, cy - r * 0.4, with_alpha((255, 255, 255), alpha))


# ------------------------------- ikonkalar ---------------------------------
# Har bir funksiya: draw_icon(c: Canvas) -> None


def _leaf(c: Canvas, x: float, y: float, w: float, h: float, color) -> None:
    c.poly([(x, y), (x + w, y + h * 0.45), (x + w * 0.45, y + h), (x - w * 0.1, y + h * 0.6)], color)


def apple(c: Canvas) -> None:
    p = c.c
    c.ellipse(28, 58, 172, 188, p("main"))
    c.ellipse(58, 46, 142, 96, p("main"))
    c.ellipse(48, 70, 96, 130, p("light"), width=0)
    c.line([(102, 62), (108, 30)], p("darker"), width=9)
    _leaf(c, 106, 30, 46, 34, p("accent"))
    c.shine(72, 96, 26)


def carrot(c: Canvas) -> None:
    p = c.c
    c.poly([(62, 44), (138, 44), (104, 190), (96, 190)], p("main"))
    c.poly([(62, 44), (100, 44), (96, 190), (88, 190)], p("light"))
    for i, y in enumerate((70, 100, 130, 160)):
        off = (y - 44) * 0.14
        c.line([(70 + off, y + 8), (130 - off, y + 4)], p("darker"), width=4)
    _leaf(c, 84, 42, 34, 46, p("accent"))
    _leaf(c, 100, 40, -34, 46, p("accent"))
    _leaf(c, 92, 36, 14, 52, p("accent_light"))


def leaf(c: Canvas) -> None:
    p = c.c
    c.line([(100, 190), (100, 120)], p("darker"), width=8)
    _leaf(c, 100, 100, 62, 70, p("main"))
    _leaf(c, 100, 110, -54, 58, p("light"))
    _leaf(c, 100, 80, 42, 52, p("accent"))
    c.line([(100, 100), (150, 132)], p("darker"), width=4)
    c.line([(100, 110), (58, 152)], p("darker"), width=4)


def nuts(c: Canvas) -> None:
    p = c.c
    c.ellipse(34, 60, 112, 168, p("main"))
    c.ellipse(88, 60, 166, 168, p("main"))
    c.ellipse(48, 76, 98, 152, p("light"))
    c.ellipse(102, 76, 152, 152, p("light"))
    c.line([(100, 66), (100, 162)], p("darker"), width=5)
    c.shine(70, 100, 22)


def steak(c: Canvas) -> None:
    p = c.c
    c.ellipse(26, 56, 174, 176, p("dark"))
    c.ellipse(34, 64, 166, 168, p("main"))
    c.ellipse(62, 88, 140, 148, p("light"))
    c.ellipse(80, 104, 124, 134, p("lighter"))
    c.shine(60, 92, 20)


def drumstick(c: Canvas) -> None:
    p = c.c
    c.rounded(88, 140, 126, 190, 20, p("white"))
    c.circle(107, 142, 22, p("white"))
    c.circle(88, 184, 20, p("white"))
    c.ellipse(56, 40, 158, 150, p("main"))
    c.ellipse(74, 56, 140, 128, p("light"))
    c.shine(96, 74, 24)


def sausage(c: Canvas) -> None:
    p = c.c
    c.rounded(34, 58, 166, 104, 24, p("main"))
    c.rounded(34, 108, 166, 154, 24, p("main"))
    c.rounded(44, 66, 150, 92, 14, p("light"))
    c.rounded(44, 116, 150, 142, 14, p("light"))
    c.circle(40, 81, 8, p("darker"))
    c.circle(160, 81, 8, p("darker"))
    c.circle(40, 131, 8, p("darker"))
    c.circle(160, 131, 8, p("darker"))


def fish(c: Canvas) -> None:
    p = c.c
    c.poly([(140, 108), (194, 72), (194, 148)], p("main"))
    c.ellipse(24, 62, 148, 158, p("main"))
    c.ellipse(40, 74, 132, 146, p("light"))
    c.poly([(76, 74), (104, 40), (112, 78)], p("dark"))
    c.poly([(96, 146), (120, 176), (128, 144)], p("dark"))
    c.circle(58, 100, 12, p("white"))
    c.circle(56, 100, 6, p("darker"))
    c.line([(70, 88), (128, 84)], p("darker"), width=4)


def milk_carton(c: Canvas) -> None:
    p = c.c
    c.poly([(58, 52), (100, 22), (142, 52)], p("light"))
    c.rounded(58, 50, 142, 186, 10, p("white"))
    c.rect(58, 118, 142, 164, p("main"))
    c.circle(100, 141, 15, p("white"))
    c.line([(100, 130), (100, 158)], p("white"), width=4)
    c.line([(86, 76), (114, 76)], p("main"), width=8)
    c.line([(86, 92), (114, 92)], p("main"), width=8)


def yogurt_cup(c: Canvas) -> None:
    p = c.c
    c.poly([(62, 86), (138, 86), (128, 186), (72, 186)], p("white"))
    c.rounded(52, 62, 148, 92, 14, p("main"))
    c.rect(62, 130, 138, 168, p("light"))
    c.circle(100, 149, 13, p("accent"))
    c.line([(74, 110), (126, 110)], p("accent_light"), width=6)
    c.shine(80, 100, 16)


def cheese(c: Canvas) -> None:
    p = c.c
    c.poly([(30, 150), (170, 150), (170, 96), (30, 124)], p("main"))
    c.poly([(30, 150), (170, 150), (170, 140), (30, 142)], p("dark"))
    c.circle(70, 132, 12, p("dark"))
    c.circle(112, 126, 9, p("dark"))
    c.circle(142, 138, 7, p("dark"))
    c.shine(140, 112, 16)


def butter(c: Canvas) -> None:
    p = c.c
    c.poly([(46, 74), (154, 74), (154, 96), (46, 96)], p("light"))
    c.poly([(46, 96), (154, 96), (154, 150), (46, 150)], p("main"))
    c.rect(46, 118, 154, 150, p("accent"), width=0)
    c.rect(46, 118, 154, 142, p("accent_light"))
    c.line([(60, 104), (140, 104)], p("darker"), width=3)


def egg(c: Canvas) -> None:
    p = c.c
    c.ellipse(62, 34, 138, 186, p("white"))
    c.ellipse(72, 46, 128, 174, p("lighter"))
    c.shine(90, 78, 22, alpha=140)


def ice_cream(c: Canvas) -> None:
    p = c.c
    c.poly([(70, 108), (130, 108), (100, 192)], p("accent"))
    c.line([(80, 128), (120, 128)], p("darker"), width=4)
    c.line([(86, 148), (114, 148)], p("darker"), width=4)
    c.line([(92, 168), (108, 168)], p("darker"), width=4)
    c.circle(78, 96, 30, p("main"))
    c.circle(122, 96, 30, p("light"))
    c.circle(100, 62, 32, p("main"))
    c.circle(100, 44, 10, p("accent"))
    c.shine(88, 54, 18, alpha=110)


def bread(c: Canvas) -> None:
    p = c.c
    c.ellipse(18, 40, 182, 184, p("main"))
    c.ellipse(30, 50, 170, 174, p("light"))
    c.ellipse(56, 74, 144, 152, p("lighter"))
    for i in range(8):
        import math

        a = i * math.pi / 4
        c.circle(100 + 28 * math.cos(a), 113 + 26 * math.sin(a), 6, p("accent"))
    c.circle(100, 113, 10, p("accent"))
    c.shine(56, 74, 20, alpha=80)


def samsa(c: Canvas) -> None:
    p = c.c
    c.poly([(100, 32), (176, 180), (24, 180)], p("main"))
    c.poly([(100, 56), (158, 172), (42, 172)], p("light"))
    c.line([(100, 100), (100, 150)], p("darker"), width=6)
    c.circle(100, 122, 12, p("accent"))
    c.shine(78, 140, 18, alpha=90)


def cake(c: Canvas) -> None:
    p = c.c
    c.rounded(44, 128, 156, 182, 12, p("main"))
    c.rounded(48, 96, 152, 134, 12, p("light"))
    for x in (60, 84, 108, 132):
        c.circle(x, 92, 16, p("lighter"))
    c.circle(100, 78, 14, p("white"))
    c.circle(100, 62, 10, p("accent"))
    c.line([(44, 152), (156, 152)], p("darker"), width=4)
    c.circle(70, 165, 6, p("accent"))
    c.circle(120, 165, 6, p("accent"))


def chocolate(c: Canvas) -> None:
    p = c.c
    c.rounded(38, 58, 162, 172, 10, p("main"))
    c.rounded(38, 58, 162, 82, 10, p("dark"))
    c.line([(100, 82), (100, 172)], p("darker"), width=6)
    c.line([(38, 127), (162, 127)], p("darker"), width=6)
    c.rect(48, 92, 92, 118, p("light"))
    c.rect(108, 137, 152, 163, p("light"))
    c.shine(60, 100, 14, alpha=70)


def cookie(c: Canvas) -> None:
    p = c.c
    c.circle(100, 104, 74, p("main"))
    c.circle(100, 104, 62, p("light"))
    for cx, cy in (72, 78), (118, 72), (88, 124), (128, 118), (104, 150):
        c.circle(cx, cy, 12, p("darker"))
    c.shine(70, 74, 20, alpha=80)


def halva(c: Canvas) -> None:
    p = c.c
    c.rounded(32, 74, 168, 158, 12, p("main"))
    c.rounded(32, 74, 168, 96, 12, p("light"))
    for y in (104, 118, 132, 146):
        c.line([(44, y), (156, y)], p("darker"), width=4)
    c.circle(60, 108, 7, p("accent"))
    c.circle(128, 138, 7, p("accent"))
    c.shine(52, 86, 16, alpha=70)


def water_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(72, 76, 128, 190, 12, p("lighter"))
    c.rect(88, 52, 112, 82, p("lighter"))
    c.rounded(80, 34, 120, 58, 8, p("main"))
    c.rect(72, 110, 128, 160, p("main"))
    c.circle(100, 135, 16, p("white"))
    c.line([(84, 92), (116, 92)], p("main"), width=6)


def juice_box(c: Canvas) -> None:
    p = c.c
    c.rounded(66, 52, 134, 188, 10, p("white"))
    c.rect(66, 52, 134, 108, p("main"))
    c.rect(66, 132, 134, 178, p("light"))
    c.circle(100, 160, 16, p("accent"))
    c.line([(112, 52), (150, 18)], p("darker"), width=8)
    c.line([(84, 82), (116, 82)], p("white"), width=7)


def soda_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(74, 72, 126, 190, 14, p("main"))
    c.rect(88, 50, 112, 78, p("main"))
    c.rounded(82, 32, 118, 56, 8, p("dark"))
    c.rect(74, 108, 126, 152, p("white"))
    c.circle(88, 168, 7, p("lighter"))
    c.circle(108, 176, 5, p("lighter"))
    c.circle(96, 158, 4, p("lighter"))
    c.shine(88, 90, 14, alpha=90)


def cup_hot(c: Canvas) -> None:
    p = c.c
    c.poly([(58, 84), (142, 84), (132, 176), (68, 176)], p("white"))
    c.ellipse(46, 84, 154, 104, p("light"))
    c.ellipse(56, 90, 144, 100, p("main"))
    c.arc(132, 100, 178, 156, 300, 80, p("white"), width=10)
    c.line([(84, 66), (78, 44), (88, 30)], p("lighter"), width=6)
    c.line([(104, 62), (98, 38), (108, 24)], p("lighter"), width=6)
    c.ellipse(40, 176, 160, 196, p("light"))


def can(c: Canvas) -> None:
    p = c.c
    c.rounded(66, 46, 134, 186, 12, p("main"))
    c.ellipse(66, 40, 134, 62, p("light"))
    c.rect(66, 96, 134, 138, p("white"))
    c.line([(80, 110), (120, 110)], p("main"), width=6)
    c.line([(80, 124), (108, 124)], p("main"), width=6)
    c.ellipse(66, 178, 134, 196, p("dark"))
    c.shine(80, 70, 14, alpha=90)


def sack(c: Canvas) -> None:
    p = c.c
    c.poly([(56, 62), (144, 62), (162, 186), (38, 186)], p("main"))
    c.poly([(56, 62), (100, 62), (100, 186), (38, 186)], p("light"))
    c.rounded(48, 44, 152, 70, 10, p("dark"))
    c.rounded(66, 100, 134, 162, 8, p("lighter"))
    c.line([(80, 120), (120, 120)], p("main"), width=6)
    c.line([(80, 138), (110, 138)], p("main"), width=6)
    c.circle(100, 176, 8, p("accent"))


def oil_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(66, 82, 134, 190, 14, p("main"))
    c.rect(88, 56, 112, 88, p("main"))
    c.rounded(82, 38, 118, 62, 8, p("dark"))
    c.rect(66, 116, 134, 166, p("white"))
    c.poly([(100, 126), (116, 146), (100, 166), (84, 146)], p("accent"))
    c.circle(100, 146, 8, p("main"))
    c.shine(80, 100, 14, alpha=110)


def spice_jar(c: Canvas) -> None:
    p = c.c
    c.rounded(62, 78, 138, 190, 12, p("lighter"))
    c.poly([(62, 130), (138, 130), (138, 186), (62, 186)], p("main"))
    c.rounded(56, 48, 144, 86, 10, p("dark"))
    c.circle(84, 152, 6, p("darker"))
    c.circle(112, 164, 6, p("darker"))
    c.circle(96, 176, 6, p("darker"))
    c.rect(74, 96, 126, 116, p("white"))
    c.shine(78, 100, 12, alpha=120)


def box(c: Canvas) -> None:
    p = c.c
    c.poly([(40, 84), (72, 58), (162, 58), (162, 84)], p("light"))
    c.rect(40, 84, 162, 190, p("main"))
    c.rect(52, 110, 150, 168, p("lighter"))
    c.circle(100, 138, 22, p("accent"))
    c.line([(100, 122), (100, 154)], p("white"), width=5)
    c.line([(84, 138), (116, 138)], p("white"), width=5)


def spray_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(70, 88, 130, 190, 12, p("main"))
    c.rect(92, 70, 108, 92, p("main"))
    c.poly([(70, 74), (130, 74), (158, 44), (118, 30)], p("dark"))
    c.rect(148, 40, 176, 52, p("darker"))
    c.rect(70, 120, 130, 162, p("white"))
    c.circle(84, 58, 6, p("lighter"))
    c.circle(102, 42, 6, p("lighter"))
    c.circle(120, 30, 5, p("lighter"))
    c.shine(84, 108, 12, alpha=100)


def paper_roll(c: Canvas) -> None:
    p = c.c
    c.rounded(52, 66, 148, 190, 10, p("white"))
    c.ellipse(52, 44, 148, 84, p("white"))
    c.ellipse(78, 52, 122, 76, p("light"))
    c.ellipse(90, 58, 110, 72, p("main"))
    c.line([(52, 108), (148, 108)], p("light"), width=5)
    for x in range(60, 148, 18):
        c.line([(x, 132), (x + 8, 132)], p("main"), width=4)
    c.shine(70, 140, 16, alpha=120)


def trash_bag(c: Canvas) -> None:
    p = c.c
    c.poly([(58, 62), (142, 62), (166, 186), (34, 186)], p("main"))
    c.poly([(58, 62), (100, 62), (100, 186), (34, 186)], p("light"))
    c.rounded(64, 44, 136, 70, 12, p("dark"))
    c.line([(74, 100), (68, 176)], p("darker"), width=5)
    c.line([(120, 100), (128, 176)], p("darker"), width=5)
    c.circle(100, 168, 10, p("accent"))


def mop(c: Canvas) -> None:
    p = c.c
    c.rounded(92, 26, 116, 122, 10, p("dark"))
    c.rounded(54, 118, 154, 152, 12, p("main"))
    c.rounded(58, 150, 150, 192, 8, p("light"))
    for x in range(66, 148, 14):
        c.line([(x, 152), (x, 192)], p("darker"), width=4)
    c.line([(78, 128), (130, 128)], p("lighter"), width=6)


def toothbrush(c: Canvas) -> None:
    p = c.c
    c.rounded(88, 64, 118, 190, 14, p("main"))
    c.rounded(84, 30, 122, 74, 14, p("white"))
    for x in range(90, 118, 8):
        c.line([(x, 34), (x, 52)], p("light"), width=5)
    c.line([(94, 96), (112, 96)], p("lighter"), width=5)
    c.line([(94, 112), (112, 112)], p("lighter"), width=5)


def shampoo_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(64, 84, 136, 190, 16, p("main"))
    c.poly([(88, 62), (112, 62), (124, 92), (76, 92)], p("main"))
    c.rect(88, 52, 112, 70, p("main"))
    c.rounded(82, 32, 118, 58, 8, p("dark"))
    c.rounded(70, 116, 130, 168, 8, p("white"))
    c.circle(100, 142, 14, p("accent"))
    c.shine(80, 108, 12, alpha=110)


def cream_jar(c: Canvas) -> None:
    p = c.c
    c.rounded(52, 92, 148, 188, 14, p("white"))
    c.rounded(46, 60, 154, 100, 14, p("main"))
    c.rounded(56, 68, 144, 84, 8, p("light"))
    c.circle(100, 140, 20, p("lighter"))
    c.circle(94, 134, 7, p("white"), )
    c.shine(72, 112, 14, alpha=140)


def razor(c: Canvas) -> None:
    p = c.c
    c.rounded(88, 78, 118, 190, 14, p("main"))
    c.poly([(78, 78), (128, 78), (140, 46), (66, 46)], p("light"))
    c.rounded(66, 36, 134, 54, 8, p("dark"))
    for x in (82, 100, 118):
        c.line([(x, 46), (x, 70)], p("white"), width=4)
    c.line([(96, 100), (110, 100)], p("lighter"), width=5)
    c.line([(96, 116), (110, 116)], p("lighter"), width=5)


def soap_bar(c: Canvas) -> None:
    p = c.c
    c.rounded(42, 72, 158, 152, 20, p("main"))
    c.rounded(56, 86, 144, 138, 12, p("light"))
    c.circle(64, 66, 12, p("lighter"))
    c.circle(84, 56, 8, p("lighter"))
    c.circle(140, 62, 9, p("lighter"))
    c.shine(76, 100, 16, alpha=100)


def deodorant(c: Canvas) -> None:
    p = c.c
    c.rounded(74, 88, 126, 190, 14, p("main"))
    c.rounded(66, 38, 134, 100, 16, p("light"))
    c.ellipse(78, 26, 122, 56, p("lighter"))
    c.rect(74, 130, 126, 168, p("white"))
    c.line([(88, 142), (112, 142)], p("main"), width=5)
    c.shine(88, 62, 14, alpha=110)


def baby_bottle(c: Canvas) -> None:
    p = c.c
    c.rounded(74, 80, 126, 190, 16, p("white"))
    c.rounded(88, 34, 112, 62, 14, p("accent_light"))
    c.rounded(78, 60, 122, 86, 8, p("main"))
    for y in (108, 128, 148, 168):
        c.line([(80, y), (96, y)], p("light"), width=5)
    c.shine(86, 110, 12, alpha=140)


def tub(c: Canvas) -> None:
    p = c.c
    c.poly([(38, 96), (162, 96), (146, 178), (54, 178)], p("white"))
    c.rounded(32, 82, 168, 106, 12, p("main"))
    c.ellipse(50, 104, 150, 132, p("accent_light"))
    c.circle(70, 74, 12, p("lighter"))
    c.circle(100, 68, 9, p("lighter"))
    c.circle(130, 76, 10, p("lighter"))
    c.rounded(56, 178, 74, 190, 6, p("light"))
    c.rounded(126, 178, 144, 190, 6, p("light"))


def teddy(c: Canvas) -> None:
    p = c.c
    c.circle(66, 62, 22, p("main"))
    c.circle(134, 62, 22, p("main"))
    c.circle(66, 62, 10, p("light"))
    c.circle(134, 62, 10, p("light"))
    c.ellipse(52, 96, 148, 186, p("main"))
    c.ellipse(66, 116, 134, 172, p("light"))
    c.circle(100, 78, 36, p("main"))
    c.ellipse(86, 84, 114, 104, p("lighter"))
    c.circle(100, 94, 6, p("darker"))
    c.circle(88, 74, 6, p("darker"))
    c.circle(112, 74, 6, p("darker"))
    c.circle(100, 150, 12, p("accent"))


def tshirt(c: Canvas) -> None:
    p = c.c
    c.poly(
        [
            (64, 52),
            (84, 38),
            (116, 38),
            (136, 52),
            (162, 82),
            (140, 100),
            (138, 186),
            (62, 186),
            (60, 100),
            (38, 82),
        ],
        p("main"),
    )
    c.poly([(84, 38), (116, 38), (108, 58), (92, 58)], p("dark"))
    c.line([(62, 120), (138, 120)], p("light"), width=6)
    c.rect(84, 132, 116, 166, p("accent"))
    c.shine(74, 150, 16, alpha=70)


def dress(c: Canvas) -> None:
    p = c.c
    c.poly(
        [
            (74, 42),
            (126, 42),
            (140, 74),
            (146, 118),
            (168, 188),
            (32, 188),
            (54, 118),
            (60, 74),
        ],
        p("main"),
    )
    c.line([(58, 108), (142, 108)], p("dark"), width=8)
    c.poly([(74, 42), (126, 42), (120, 62), (80, 62)], p("dark"))
    for x in (62, 100, 138):
        c.line([(x, 130), (x, 180)], p("lighter"), width=5)
    c.circle(100, 76, 8, p("accent"))


def sneaker(c: Canvas) -> None:
    p = c.c
    c.poly([(30, 150), (170, 150), (172, 176), (32, 178)], p("white"))
    c.poly(
        [
            (32, 150),
            (40, 112),
            (72, 96),
            (104, 116),
            (118, 134),
            (166, 122),
            (168, 150),
        ],
        p("main"),
    )
    c.poly([(104, 116), (150, 120), (118, 140)], p("accent"))
    c.line([(56, 108), (76, 122)], p("white"), width=5)
    c.line([(72, 100), (92, 118)], p("white"), width=5)
    c.line([(88, 96), (106, 116)], p("white"), width=5)
    c.line([(32, 162), (168, 162)], p("light"), width=4)


def socks(c: Canvas) -> None:
    p = c.c
    c.poly(
        [
            (70, 62),
            (100, 62),
            (104, 116),
            (150, 132),
            (146, 168),
            (104, 154),
            (70, 118),
        ],
        p("main"),
    )
    c.rounded(66, 44, 104, 70, 8, p("light"))
    c.line([(74, 84), (100, 84)], p("white"), width=6)
    c.line([(76, 98), (102, 98)], p("white"), width=6)
    c.poly([(104, 116), (150, 132), (146, 150), (108, 138)], p("accent"))


def cap(c: Canvas) -> None:
    p = c.c
    c.pieslice(46, 58, 154, 162, 180, 360, p("main"))
    c.rounded(28, 152, 172, 178, 12, p("dark"))
    c.arc(46, 58, 154, 162, 180, 360, p("light"), width=6)
    c.circle(100, 60, 10, p("accent"))
    c.line([(100, 62), (100, 152)], p("dark"), width=5)


def smartphone(c: Canvas) -> None:
    p = c.c
    c.rounded(56, 26, 144, 186, 20, p("dark"))
    c.rounded(64, 40, 136, 172, 12, p("main"))
    c.rounded(88, 34, 112, 44, 5, p("darker"))
    c.rounded(78, 152, 122, 160, 4, p("light"))
    c.circle(100, 62, 8, p("dark"))
    c.rounded(72, 84, 128, 138, 6, p("lighter"))


def phone_case(c: Canvas) -> None:
    p = c.c
    c.rounded(54, 26, 146, 188, 22, p("main"))
    c.rounded(66, 42, 134, 172, 14, p("dark"))
    c.rounded(76, 48, 124, 88, 10, p("darker"))
    c.circle(88, 62, 8, p("main"))
    c.circle(112, 62, 8, p("main"))
    c.rounded(88, 150, 112, 158, 4, p("light"))


def headphones(c: Canvas) -> None:
    p = c.c
    c.arc(38, 40, 162, 156, 180, 360, p("main"), width=16)
    c.rounded(30, 118, 66, 182, 14, p("dark"))
    c.rounded(134, 118, 170, 182, 14, p("dark"))
    c.rounded(38, 128, 58, 172, 10, p("light"))
    c.rounded(142, 128, 162, 172, 10, p("light"))
    c.shine(60, 60, 12, alpha=90)


def power_bank(c: Canvas) -> None:
    p = c.c
    c.rounded(56, 44, 144, 190, 18, p("main"))
    c.rounded(70, 62, 130, 104, 10, p("light"))
    c.rounded(84, 76, 116, 90, 6, p("accent"))
    for i, x in enumerate((72, 92, 112)):
        alpha = 255 if i < 2 else 90
        c.rounded(x, 130, x + 14, 156, 5, with_alpha(p("accent"), alpha))
    c.rounded(88, 34, 112, 48, 5, p("dark"))
    c.shine(74, 116, 12, alpha=80)


def kettle(c: Canvas) -> None:
    p = c.c
    c.poly([(62, 96), (138, 96), (150, 178), (50, 178)], p("main"))
    c.rounded(70, 82, 130, 100, 8, p("dark"))
    c.circle(100, 74, 8, p("darker"))
    c.poly([(52, 112), (24, 92), (46, 134)], p("main"))
    c.arc(66, 40, 134, 110, 200, 340, p("dark"), width=10)
    c.line([(86, 62), (78, 44)], p("lighter"), width=6)
    c.line([(114, 62), (122, 44)], p("lighter"), width=6)
    c.line([(100, 58), (100, 36)], p("lighter"), width=6)
    c.rect(50, 150, 150, 162, p("light"))


def mouse(c: Canvas) -> None:
    p = c.c
    c.ellipse(62, 46, 138, 190, p("main"))
    c.ellipse(72, 58, 128, 178, p("light"))
    c.rounded(94, 62, 108, 96, 6, p("dark"))
    c.line([(100, 46), (100, 88)], p("dark"), width=4)
    c.shine(84, 74, 14, alpha=110)


def pot(c: Canvas) -> None:
    p = c.c
    c.poly([(46, 92), (154, 92), (142, 180), (58, 180)], p("main"))
    c.ellipse(38, 68, 162, 100, p("light"))
    c.circle(100, 62, 10, p("dark"))
    c.rounded(24, 104, 44, 120, 8, p("dark"))
    c.rounded(156, 104, 176, 120, 8, p("dark"))
    c.rect(58, 140, 142, 152, p("light"))
    c.shine(74, 120, 14, alpha=90)


def knife(c: Canvas) -> None:
    p = c.c
    c.poly([(98, 24), (150, 40), (146, 124), (98, 132)], p("light"))
    c.poly([(98, 24), (118, 30), (112, 128), (98, 132)], p("white"))
    c.rounded(84, 130, 116, 192, 12, p("dark"))
    c.circle(100, 148, 5, p("lighter"))
    c.circle(100, 168, 5, p("lighter"))
    c.line([(102, 40), (100, 120)], p("darker"), width=3)


def plate(c: Canvas) -> None:
    p = c.c
    c.ellipse(22, 52, 178, 172, p("white"))
    c.ellipse(38, 66, 162, 158, p("light"))
    c.ellipse(62, 88, 138, 138, p("main"))
    c.ellipse(62, 88, 138, 138, with_alpha((255, 255, 255), 60))
    c.shine(72, 88, 16, alpha=150)


def container(c: Canvas) -> None:
    p = c.c
    c.rounded(38, 92, 162, 190, 12, p("main"))
    c.rounded(32, 66, 168, 98, 12, p("light"))
    c.rounded(48, 112, 152, 172, 8, p("lighter"))
    c.rounded(84, 60, 116, 74, 5, p("white"))
    c.rounded(58, 180, 74, 190, 5, p("dark"))
    c.rounded(126, 180, 142, 190, 5, p("dark"))


def blanket(c: Canvas) -> None:
    p = c.c
    c.rounded(34, 118, 166, 178, 14, p("main"))
    c.rounded(44, 82, 156, 130, 14, p("light"))
    c.rect(34, 146, 166, 158, p("light"))
    c.line([(56, 96), (144, 96)], p("lighter"), width=5)
    c.line([(56, 112), (144, 112)], p("lighter"), width=5)
    c.circle(100, 76, 12, p("accent"))


def dumbbell(c: Canvas) -> None:
    p = c.c
    c.rounded(62, 92, 138, 112, 8, p("dark"))
    c.rounded(28, 70, 62, 134, 10, p("main"))
    c.rounded(138, 70, 172, 134, 10, p("main"))
    c.rounded(14, 80, 34, 124, 8, p("light"))
    c.rounded(166, 80, 186, 124, 8, p("light"))
    c.rounded(70, 98, 130, 106, 4, p("lighter"))


def ball(c: Canvas) -> None:
    p = c.c
    c.circle(100, 106, 78, p("white"))
    c.poly(
        [
            (100, 62),
            (136, 84),
            (122, 126),
            (78, 126),
            (64, 84),
        ],
        p("main"),
    )
    c.line([(100, 62), (100, 40)], p("dark"), width=6)
    c.line([(136, 84), (158, 74)], p("dark"), width=6)
    c.line([(122, 126), (140, 150)], p("dark"), width=6)
    c.line([(78, 126), (58, 150)], p("dark"), width=6)
    c.line([(64, 84), (42, 74)], p("dark"), width=6)
    c.arc(34, 44, 166, 176, 20, 160, p("dark"), width=5)


def bicycle(c: Canvas) -> None:
    p = c.c
    c.ellipse(26, 118, 78, 170, None, p("dark"), width=8)
    c.ellipse(122, 118, 174, 170, None, p("dark"), width=8)
    c.line([(52, 144), (86, 100), (120, 144), (148, 100)], p("main"), width=9)
    c.line([(86, 100), (104, 144), (148, 100)], p("main"), width=9)
    c.rounded(70, 84, 100, 96, 6, p("accent"))
    c.line([(148, 100), (166, 92)], p("dark"), width=8)
    c.circle(52, 144, 8, p("light"))
    c.circle(148, 144, 8, p("light"))


def tent(c: Canvas) -> None:
    p = c.c
    c.poly([(100, 30), (178, 182), (22, 182)], p("main"))
    c.poly([(100, 84), (134, 182), (66, 182)], p("dark"))
    c.line([(100, 30), (100, 182)], p("darker"), width=5)
    c.line([(100, 30), (186, 30)], p("light"), width=6)
    c.circle(100, 24, 8, p("accent"))
    c.line([(10, 182), (190, 182)], p("darker"), width=6)


def backpack(c: Canvas) -> None:
    p = c.c
    c.rounded(52, 66, 148, 190, 26, p("main"))
    c.poly([(52, 66), (148, 66), (148, 116), (52, 116)], p("light"))
    c.rounded(76, 130, 124, 170, 10, p("dark"))
    c.line([(88, 140), (112, 140)], p("lighter"), width=6)
    c.arc(66, 60, 134, 120, 180, 360, p("darker"), width=8)
    c.rounded(92, 44, 108, 68, 8, p("accent"))
    c.shine(70, 140, 12, alpha=70)


def pet_bowl(c: Canvas) -> None:
    p = c.c
    c.poly([(38, 98), (162, 98), (142, 176), (58, 176)], p("main"))
    c.ellipse(32, 82, 168, 116, p("light"))
    c.ellipse(48, 90, 152, 108, p("accent"))
    for cx in (72, 100, 128):
        c.circle(cx, 99, 8, p("dark"))
    c.circle(86, 92, 6, p("darker"))
    c.rounded(58, 176, 142, 190, 6, p("dark"))


def car_freshener(c: Canvas) -> None:
    p = c.c
    c.line([(100, 26), (100, 52)], p("dark"), width=5)
    c.poly([(100, 52), (158, 150), (42, 150)], p("main"))
    c.poly([(100, 76), (142, 150), (58, 150)], p("light"))
    c.circle(100, 110, 10, p("accent"))
    c.circle(78, 132, 6, p("accent"))
    c.circle(122, 132, 6, p("accent"))


def pen(c: Canvas) -> None:
    p = c.c
    c.rounded(86, 52, 114, 168, 8, p("main"))
    c.poly([(86, 168), (114, 168), (100, 194)], p("lighter"))
    c.rounded(82, 26, 118, 62, 8, p("dark"))
    c.line([(114, 32), (114, 58)], p("light"), width=5)
    c.rect(86, 96, 114, 104, p("white"))
    c.circle(100, 180, 5, p("darker"))


def air_freshener(c: Canvas) -> None:
    p = c.c
    c.rounded(70, 82, 130, 190, 14, p("main"))
    c.rounded(88, 62, 112, 88, 6, p("dark"))
    c.arc(56, 30, 104, 78, 200, 340, p("lighter"), width=6)
    c.arc(96, 30, 144, 78, 200, 340, p("lighter"), width=6)
    c.arc(76, 14, 124, 62, 200, 340, p("lighter"), width=6)
    c.rect(70, 118, 130, 158, p("white"))
    c.circle(100, 138, 12, p("accent"))


ICON_DRAWERS = {
    "apple": apple,
    "carrot": carrot,
    "leaf": leaf,
    "nuts": nuts,
    "steak": steak,
    "drumstick": drumstick,
    "sausage": sausage,
    "fish": fish,
    "milk-carton": milk_carton,
    "yogurt-cup": yogurt_cup,
    "cheese": cheese,
    "butter": butter,
    "egg": egg,
    "ice-cream": ice_cream,
    "bread": bread,
    "samsa": samsa,
    "cake": cake,
    "chocolate": chocolate,
    "cookie": cookie,
    "halva": halva,
    "water-bottle": water_bottle,
    "juice-box": juice_box,
    "soda-bottle": soda_bottle,
    "cup-hot": cup_hot,
    "can": can,
    "sack": sack,
    "oil-bottle": oil_bottle,
    "spice-jar": spice_jar,
    "box": box,
    "spray-bottle": spray_bottle,
    "paper-roll": paper_roll,
    "trash-bag": trash_bag,
    "mop": mop,
    "toothbrush": toothbrush,
    "shampoo-bottle": shampoo_bottle,
    "cream-jar": cream_jar,
    "razor": razor,
    "soap-bar": soap_bar,
    "deodorant": deodorant,
    "baby-bottle": baby_bottle,
    "tub": tub,
    "teddy": teddy,
    "tshirt": tshirt,
    "dress": dress,
    "sneaker": sneaker,
    "socks": socks,
    "cap": cap,
    "smartphone": smartphone,
    "phone-case": phone_case,
    "headphones": headphones,
    "power-bank": power_bank,
    "kettle": kettle,
    "mouse": mouse,
    "pot": pot,
    "knife": knife,
    "plate": plate,
    "container": container,
    "blanket": blanket,
    "dumbbell": dumbbell,
    "ball": ball,
    "bicycle": bicycle,
    "tent": tent,
    "backpack": backpack,
    "pet-bowl": pet_bowl,
    "car-freshener": car_freshener,
    "pen": pen,
    "air-freshener": air_freshener,
}
