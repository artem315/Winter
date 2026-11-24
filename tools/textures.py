#!/usr/bin/env python3
"""
Generates the block, item, armor and GUI textures of the Winter mod as pixel art (no external assets needed).
Entity textures belong to the Blockbench models (blockbench/*.bbmodel, see tools/models.py).

Usage:  python3 tools/textures.py            -> writes PNGs into src/main/resources/assets/winter/textures
        python3 tools/textures.py --preview  -> also writes build/texture-preview.png (contact sheet)

Requires Pillow (pip install pillow). Output is deterministic (fixed random seeds).
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "winter", "textures")
PREVIEW = []


# =====================================================================================================
# Helpers
# =====================================================================================================

def hexc(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def mix(c1, c2, t):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def scale(c, f):
    return (max(0, min(255, int(c[0] * f))), max(0, min(255, int(c[1] * f))), max(0, min(255, int(c[2] * f))), c[3])


def jitter(c, rnd, amount):
    d = rnd.randint(-amount, amount)
    return (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), c[3])


def new(w=16, h=16):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def sprite(rows, palette, width=16):
    for i, r in enumerate(rows):
        assert len(r) == width, f"row {i} has width {len(r)}: {r!r}"
    im = new(width, len(rows))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                im.putpixel((x, y), palette[ch])
    return im


def outline(im, color, diagonal=False):
    """Adds a 1px outline around all opaque pixels."""
    w, h = im.size
    src = im.copy()
    for y in range(h):
        for x in range(w):
            if src.getpixel((x, y))[3] != 0:
                continue
            neigh = [(1, 0), (-1, 0), (0, 1), (0, -1)]
            if diagonal:
                neigh += [(1, 1), (-1, -1), (1, -1), (-1, 1)]
            for dx, dy in neigh:
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and src.getpixel((nx, ny))[3] > 0:
                    im.putpixel((x, y), color)
                    break
    return im


def value_noise(w, h, rnd, cell=4):
    """Smooth-ish noise in [0,1], tileable."""
    gw, gh = w // cell + 1, h // cell + 1
    grid = [[rnd.random() for _ in range(gw)] for _ in range(gh)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            gx, gy = x / cell, y / cell
            x0, y0 = int(gx), int(gy)
            fx, fy = gx - x0, gy - y0
            x1, y1 = (x0 + 1) % (gw - 1 if gw > 1 else 1), (y0 + 1) % (gh - 1 if gh > 1 else 1)
            x0 %= max(1, gw - 1)
            y0 %= max(1, gh - 1)
            sx, sy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
            a = grid[y0][x0] + (grid[y0][x1] - grid[y0][x0]) * sx
            b = grid[y1][x0] + (grid[y1][x1] - grid[y1][x0]) * sx
            out[y][x] = a + (b - a) * sy
    return out


def save(im, *path, preview=True):
    p = os.path.join(OUT, *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)
    if preview:
        PREVIEW.append(("/".join(path), im))


def save_mcmeta(frametime, *path, interpolate=False):
    p = os.path.join(OUT, *path) + ".mcmeta"
    with open(p, "w") as f:
        f.write('{\n  "animation": {\n    "frametime": %d%s\n  }\n}\n' % (frametime, ',\n    "interpolate": true' if interpolate else ""))


def sphere(im, cx, cy, r, base, light, dark, highlight, outline_c=None):
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            if d <= r:
                # light from the top-left
                lum = (-dx - dy) / (r * 1.6) + 0.15
                c = base
                if lum > 0.45:
                    c = light
                elif lum < -0.35:
                    c = dark
                im.putpixel((x, y), c)
    hx, hy = int(cx - r * 0.45), int(cy - r * 0.45)
    im.putpixel((hx, hy), highlight)
    if 0 <= hx + 1 < im.size[0]:
        im.putpixel((hx + 1, hy), mix(highlight, light, 0.5))
    if outline_c:
        outline(im, outline_c)
    return im


def ellipse_fill(im, cx, cy, rx, ry, color):
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0:
                im.putpixel((x, y), color)


# =====================================================================================================
# Items
# =====================================================================================================

CRYSTAL = {"o": hexc("1D4E7E"), "D": hexc("4C9BD6"), "L": hexc("9FDCF7"), "W": hexc("E8FBFF"),
           "M": hexc("6CBCEB")}
WOOD = {"H": hexc("7A5230"), "h": hexc("4E3218")}


def item_frost_crystal():
    rows = [
        "................",
        ".......oo.......",
        "......oWLo......",
        "......oWLDo.....",
        ".....oWWLDo.....",
        ".....oWLLDDo....",
        ".o...oWLLDDo..o.",
        "oLo..oWLLDDo.oLo",
        "oWLo.oWLLDDooWDo",
        "oWLDooWLLDDooLDo",
        ".oLDooWLLDDoLDo.",
        ".oLDDoLLLDDoLDo.",
        "..oDDoLLDDDoDo..",
        "...oDDDDDDDDo...",
        "....oooooooo....",
        "................",
    ]
    return sprite(rows, CRYSTAL)


def item_tools():
    pal = dict(CRYSTAL)
    pal.update(WOOD)
    pal["G"] = hexc("2F6FA8")
    pal["o"] = hexc("1B2E45")
    tools = {
        "frost_sword": [
            ".............ooo",
            "............oWLo",
            "...........oWLDo",
            "..........oWLDo.",
            ".........oWLDo..",
            "........oWLDo...",
            ".......oWLDo....",
            "..oo..oWLDo.....",
            "..oGooWLDo......",
            "...oGWLDo.......",
            "....oGDo........",
            "...oHoGGo.......",
            "..oHo..oGo......",
            ".oHo....oo......",
            "oHo.............",
            "oo..............",
        ],
        "frost_pickaxe": [
            "................",
            "....oooooo......",
            "...oWWLLLLoo....",
            "....ooooLLDDo...",
            "........oHoLDo..",
            ".......oHo.oLDo.",
            "......oHo...oLo.",
            ".....oHo....oDo.",
            "....oHo......o..",
            "...oHo..........",
            "..oHo...........",
            ".oHo............",
            "oHo.............",
            "oo..............",
            "................",
            "................",
        ],
        "frost_axe": [
            "................",
            ".......oooo.....",
            "......oWWLLo....",
            ".....oWLLLDDo...",
            ".....oWLLoHDo...",
            "......oLoHoDo...",
            ".......oHo.oo...",
            "......oHo.......",
            ".....oHo........",
            "....oHo.........",
            "...oHo..........",
            "..oHo...........",
            ".oHo............",
            ".oo.............",
            "................",
            "................",
        ],
        "frost_shovel": [
            "................",
            "...........ooo..",
            "..........oWLLo.",
            ".........oWLLDDo",
            ".........oLLLDDo",
            "..........oLDDo.",
            ".........oHooo..",
            "........oHo.....",
            ".......oHo......",
            "......oHo.......",
            ".....oHo........",
            "....oHo.........",
            "...oHo..........",
            "..oHo...........",
            "..oo............",
            "................",
        ],
        "frost_hoe": [
            "................",
            "......ooooo.....",
            ".....oWWLLDo....",
            "......ooooHDo...",
            ".........oHoo...",
            "........oHo.....",
            ".......oHo......",
            "......oHo.......",
            ".....oHo........",
            "....oHo.........",
            "...oHo..........",
            "..oHo...........",
            ".oHo............",
            ".oo.............",
            "................",
            "................",
        ],
    }
    return {k: sprite(v, pal) for k, v in tools.items()}


def item_snow_shovel():
    pal = {"o": hexc("1F2A36"), "H": hexc("8A5F36"), "B": hexc("3A8FD9"), "b": hexc("2A6CB0"),
           "S": hexc("C9D3DC"), "L": hexc("6FB4EE")}
    return sprite([
        "................",
        ".....oooooo.....",
        ".....oHooHo.....",
        ".....oHHHHo.....",
        "......oHHo......",
        "......oHHo......",
        "......oHHo......",
        "......oHHo......",
        "......oHHo......",
        "..oooooHHooooo..",
        "..oLLLLLLLLLLo..",
        "..oBBBBBBBBBBo..",
        "..oBbBBBBBBbBo..",
        "..obbbbbbbbbbo..",
        "..oSSSSSSSSSSo..",
        "..oooooooooooo..",
    ], pal)


def item_frost_staff():
    pal = dict(CRYSTAL)
    pal.update({"S": hexc("C9D1D9"), "s": hexc("7A8794"), "o": hexc("1B2E45"), "*": hexc("FFFFFF")})
    return sprite([
        "........*.......",
        "............o...",
        "...........oWo.*",
        "..........oWLLo.",
        ".........oWWLLDo",
        "..........oLLDo.",
        "...*.......oDo..",
        "..........oSo...",
        ".........oSo....",
        "........oso.....",
        ".......oSo......",
        "......oSo.......",
        ".....oSo........",
        "....oso.........",
        "...oSo..........",
        "...oo...........",
    ], pal)


def item_ice_ball():
    im = new()
    sphere(im, 8, 8.5, 5.6, hexc("BFE6FA"), hexc("E8F8FF"), hexc("86BFE3"), hexc("FFFFFF"), hexc("2F5F8C"))
    for (x, y) in ((9, 9), (10, 10), (8, 11), (6, 8)):
        im.putpixel((x, y), hexc("9CCFEE"))
    return im


def item_mandarin():
    im = new()
    sphere(im, 8, 9.5, 5.5, hexc("F7861E"), hexc("FFB04D"), hexc("D2600F"), hexc("FFE3B0"), hexc("7A3305"))
    rnd = random.Random(3)
    for _ in range(6):
        x, y = rnd.randint(5, 11), rnd.randint(7, 13)
        p = im.getpixel((x, y))
        if p[3] and p != hexc("7A3305"):
            im.putpixel((x, y), scale(p, 0.9))
    im.putpixel((8, 4), hexc("5A3A1E"))
    im.putpixel((8, 3), hexc("5A3A1E"))
    for (x, y) in ((9, 3), (10, 2), (11, 2), (10, 3), (9, 2)):
        im.putpixel((x, y), hexc("4F9B3A"))
    im.putpixel((11, 3), hexc("2F6E25"))
    im.putpixel((12, 2), hexc("2F6E25"))
    return im


def item_bauble(color):
    base, light, dark = {
        "red": (hexc("D3232F"), hexc("F2545B"), hexc("8F121B")),
        "gold": (hexc("E3B23C"), hexc("FFE07A"), hexc("A87A1A")),
        "blue": (hexc("2E6FD6"), hexc("6FA6F5"), hexc("1A438F")),
    }[color]
    im = new()
    sphere(im, 8, 10, 5.0, base, light, dark, hexc("FFFFFF"), scale(dark, 0.6))
    cap = hexc("C9CED6") if color == "gold" else hexc("E0B43A")
    capd = scale(cap, 0.75)
    for x in range(6, 10):
        im.putpixel((x, 4), cap)
        im.putpixel((x, 3), capd)
    im.putpixel((7, 2), capd)
    im.putpixel((8, 2), capd)
    im.putpixel((7, 1), capd)
    im.putpixel((8, 1), capd)
    return im


def bottle(liquid, liquid_light, foam):
    pal = {"o": hexc("2C3B47"), "c": hexc("8B5A2B"), "g": hexc("D8EEF7", 220), "w": hexc("FFFFFF", 230),
           "b": liquid, "B": liquid_light, "f": foam}
    return sprite([
        "................",
        "......oooo......",
        "......occo......",
        "......occo......",
        ".......oo.......",
        "......oggo......",
        ".....ogggwo.....",
        "....offfffgo....",
        "...offffffffo...",
        "...owbbbbbbbo...",
        "...owBbbbbbbo...",
        "...obBbbbbbbo...",
        "...obbbbbbbbo...",
        "....obbbbbbo....",
        ".....oooooo.....",
        "................",
    ], pal)


def item_gingerbread():
    pal = {"o": hexc("5A2E10"), "B": hexc("B5682E"), "w": hexc("FFFFFF"), "R": hexc("D7263D")}
    return sprite([
        "................",
        "......oooo......",
        ".....oBBBBo.....",
        ".....owBBwo.....",
        ".....oBBBBo.....",
        "..ooooBwwBoooo..",
        ".oBBBBBBBBBBBBo.",
        ".oBwBBBRRBBBwBo.",
        "..ooooBBBBoooo..",
        ".....oBRRBo.....",
        ".....oBBBBo.....",
        "....oBBooBBo....",
        "...oBBo..oBBo...",
        "...owBo..oBwo...",
        "....oo....oo....",
        "................",
    ], pal)


def item_candy_cane():
    mask = [
        "................",
        "..........ccc...",
        ".........ccccc..",
        "........cc...cc.",
        "........cc...cc.",
        ".......cc....cc.",
        ".......cc.......",
        "......cc........",
        ".....ccc........",
        ".....cc.........",
        "....ccc.........",
        "....cc..........",
        "...ccc..........",
        "...cc...........",
        "..cc............",
        "................",
    ]
    im = new()
    red, white = hexc("D7263D"), hexc("F7F3F0")
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch == "c":
                im.putpixel((x, y), red if ((x + y) // 2) % 2 == 0 else white)
    return outline(im, hexc("5E0B12"))


def item_olivier():
    pal = {"o": hexc("4A2F14"), "S": hexc("EFE2B0"), "g": hexc("6DBE45"), "p": hexc("E89A9A"),
           "c": hexc("F08A2A"), "b": hexc("B9874B"), "B": hexc("8E6232"), "e": hexc("FFF7DA")}
    return sprite([
        "................",
        "................",
        "................",
        "................",
        "......oooo......",
        "....ooSeSSoo....",
        "...oSSgSSpSSo...",
        "..oSpSeScSSgSo..",
        ".oSSSgSSSSpSeSo.",
        ".obbbbbbbbbbbbo.",
        ".oBbbbbbbbbbbBo.",
        "..oBbbbbbbbbBo..",
        "...oBBbbbbBBo...",
        "....oooooooo....",
        "................",
        "................",
    ], pal)


def item_pelmeni(cooked):
    im = new()
    if cooked:
        base, shade_c, edge, hl = hexc("F5E3B8"), hexc("E0C68C"), hexc("8C6A35"), hexc("FFFBEA")
    else:
        base, shade_c, edge, hl = hexc("F3EAD7"), hexc("D8CBB0"), hexc("9C8B6B"), hexc("FFFFFF")
    for cx, cy in ((5.0, 7.5), (11.0, 7.0), (8.0, 11.5)):
        layer = new()
        ellipse_fill(layer, cx, cy, 3.4, 2.6, base)
        for y in range(16):
            for x in range(16):
                p = layer.getpixel((x, y))
                if p[3] and y + 0.5 > cy + 0.8:
                    layer.putpixel((x, y), shade_c)
        outline(layer, edge)
        # crimped edge on top
        for x in range(int(cx - 2), int(cx + 3), 2):
            y = int(cy - 2.6)
            if 0 <= y < 16 and layer.getpixel((x, y))[3]:
                layer.putpixel((x, y), edge)
        layer.putpixel((int(cx - 1), int(cy - 1)), hl)
        im.alpha_composite(layer)
    if cooked:
        for (x, y) in ((4, 7), (12, 6), (9, 11), (7, 12), (6, 6)):
            im.putpixel((x, y), hexc("4E9A3A"))
    return im


def item_thermometer():
    pal = {"o": hexc("3B3B4F"), "W": hexc("E6F2F8"), "s": hexc("7A8A99"), "R": hexc("D62828"),
           "r": hexc("FF8A8A"), "w": hexc("FFFFFF")}
    return sprite([
        "................",
        ".......oo.......",
        "......owWo......",
        "......oWso......",
        "......owWo......",
        "......oWso......",
        "......orRo......",
        "......oRso......",
        "......orRo......",
        "......oRso......",
        "......orRo......",
        ".....oRRRRo.....",
        "....oRrrRRRo....",
        "....oRrRRRRo....",
        ".....oRRRRo.....",
        "......oooo......",
    ], pal)


def item_yeti_fur():
    pal = {"o": hexc("7E93A6"), "W": hexc("F4F8FA"), "L": hexc("C9D6E0")}
    return sprite([
        "................",
        "................",
        ".......o........",
        "......oWo..o....",
        "...o..oWo.oWo...",
        "..oWo.oWWooWo...",
        "..oWWooWWoWWo...",
        "...oWWWWWWWWo...",
        "..oWWLWWWWLWWo..",
        ".oWWLLWWWLLWWWo.",
        ".oWLLWWWLLWWLWo.",
        "..oLLWLLLWWLLo..",
        "...oLLLLLLLLo...",
        "....oooooooo....",
        "................",
        "................",
    ], pal)


def item_clothing():
    out = {}
    out["ushanka"] = sprite([
        "................",
        "................",
        ".....oooooo.....",
        "...ooFLLLLFoo...",
        "..oFLLLLLLLLFo..",
        "..oFFFLLLLFFFo..",
        "..oFFFFFFFFFFo..",
        ".oBBBBBBBBBBBBo.",
        ".oBbBBBYYBBBbBo.",
        ".oBBBBBBBBBBBBo.",
        ".oBBooooooooBBo.",
        ".oBBo......oBBo.",
        ".obBo......oBbo.",
        ".oBBo......oBBo.",
        "..oo........oo..",
        "................",
    ], {"o": hexc("3A2412"), "F": hexc("6E4A2B"), "L": hexc("8C6239"), "B": hexc("A8896A"),
        "b": hexc("C4A887"), "Y": hexc("E0B43A")})
    out["sweater"] = sprite([
        "................",
        "................",
        ".....oooooo.....",
        "..oooWWWWWWooo..",
        ".oRRRoWWWWoRRRo.",
        "oRRRRRRRRRRRRRRo",
        "oRRRRRRRRRRRRRRo",
        "oRWRRWRRRRWRRWRo",
        "oWWoWRWWWWRWoWWo",
        ".oo.oRRRRRRo.oo.",
        "....oRRWRRRo....",
        "....oRRRRRRo....",
        "....oWWWWWWo....",
        "....oooooooo....",
        "................",
        "................",
    ], {"o": hexc("4E0D0D"), "R": hexc("C62828"), "W": hexc("F5F5F5")})
    out["quilted_pants"] = sprite([
        "................",
        "................",
        "...oooooooooo...",
        "...oPPPPPPPPo...",
        "...oppppppppo...",
        "...oPPPPPPPPo...",
        "...oPPPooPPPo...",
        "...oppo..oppo...",
        "...oPPo..oPPo...",
        "...oPPo..oPPo...",
        "...oppo..oppo...",
        "...oPPo..oPPo...",
        "...oPPo..oPPo...",
        "...oppo..oppo...",
        "...oooo..oooo...",
        "................",
    ], {"o": hexc("1F242E"), "P": hexc("4A5568"), "p": hexc("343C4A")})
    boot = [
        "........",
        "........",
        ".ooooo..",
        ".oLGGo..",
        ".oRWRo..",
        ".oLGGo..",
        ".oLGGo..",
        ".oLGGo..",
        ".oLGGo..",
        ".oLGGo..",
        ".oLGGGo.",
        ".oLGGGGo",
        ".oDDDDDo",
        ".ooooooo",
        "........",
        "........",
    ]
    out["valenki"] = sprite([a + b for a, b in zip(boot, boot)],
                            {"o": hexc("2E2E2E"), "G": hexc("8E8E8E"), "L": hexc("ABABAB"), "D": hexc("5E5E5E"),
                             "R": hexc("C62828"), "W": hexc("F5F5F5")})
    out["ice_skates"] = sprite([
        "................",
        "................",
        "....ooooo.......",
        "....oWWWo.......",
        "....oWlWo.......",
        "....oWlWo.......",
        "....oWlWo.......",
        "....oWWWoooo....",
        "....oWWWWWWWo...",
        "....oWWWWWWWWo..",
        "....oDDDDDDDDo..",
        "....oooooooooo..",
        ".....s.....s....",
        "...ossssssssso..",
        "....oooooooooo..",
        "................",
    ], {"o": hexc("2C3440"), "W": hexc("F2F4F6"), "l": hexc("9AA6B2"), "D": hexc("6B7785"), "s": hexc("C9D3DC")})
    return out


def gen_items():
    save(item_frost_crystal(), "item", "frost_crystal.png")
    for name, im in item_tools().items():
        save(im, "item", f"{name}.png")
    save(item_snow_shovel(), "item", "snow_shovel.png")
    save(item_frost_staff(), "item", "frost_staff.png")
    save(item_ice_ball(), "item", "ice_ball.png")
    save(item_mandarin(), "item", "mandarin.png")
    for color in ("red", "gold", "blue"):
        save(item_bauble(color), "item", f"{color}_bauble.png")
    save(bottle(hexc("6B3B1F"), hexc("8E5631"), hexc("F2E6D0")), "item", "hot_chocolate.png")
    save(bottle(hexc("A8441A"), hexc("CF6A2C"), hexc("F3C969")), "item", "sbiten.png")
    save(item_gingerbread(), "item", "gingerbread_cookie.png")
    save(item_candy_cane(), "item", "candy_cane.png")
    save(item_olivier(), "item", "olivier_salad.png")
    save(item_pelmeni(False), "item", "raw_pelmeni.png")
    save(item_pelmeni(True), "item", "pelmeni.png")
    save(item_thermometer(), "item", "thermometer.png")
    save(item_yeti_fur(), "item", "yeti_fur.png")
    for name, im in item_clothing().items():
        save(im, "item", f"{name}.png")


# =====================================================================================================
# Blocks
# =====================================================================================================

def bricks(base, light, dark, mortar, seed, brick_h=4, brick_w=8, sparkle=None):
    rnd = random.Random(seed)
    im = new()
    for y in range(16):
        row = y // brick_h
        offset = brick_w // 2 if row % 2 else 0
        for x in range(16):
            ly = y % brick_h
            lx = (x + offset) % brick_w
            if ly == brick_h - 1 or lx == brick_w - 1:
                c = jitter(mortar, rnd, 4)
            else:
                c = base
                if ly == 0 or lx == 0:
                    c = light
                elif ly == brick_h - 2 or lx == brick_w - 2:
                    c = dark
                c = jitter(c, rnd, 5)
            im.putpixel((x, y), c)
    if sparkle:
        for _ in range(5):
            x, y = rnd.randrange(16), rnd.randrange(16)
            if (y % brick_h) != brick_h - 1:
                im.putpixel((x, y), sparkle)
    return im


SNOWFLAKE_7 = [
    "W..W..W",
    ".W.W.W.",
    "..WWW..",
    "WWW.WWW",
    "..WWW..",
    ".W.W.W.",
    "W..W..W",
]


def draw_snowflake(im, ox, oy, color):
    for y, row in enumerate(SNOWFLAKE_7):
        for x, ch in enumerate(row):
            if ch == "W":
                im.putpixel((ox + x, oy + y), color)


def block_chiseled_ice(top=False):
    rnd = random.Random(11 if top else 12)
    im = new()
    base, light, dark, frame = hexc("8FC0E8"), hexc("C7E6FA"), hexc("6FA3D4"), hexc("E3F4FF")
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            inner = x in (2, 13) or y in (2, 13)
            if edge:
                c = frame
            elif x in (1, 14) or y in (1, 14):
                c = dark
            elif inner:
                c = light
            else:
                c = base
            im.putpixel((x, y), jitter(c, rnd, 4))
    if top:
        for i in range(4, 12):
            im.putpixel((i, i), light)
            im.putpixel((15 - i, i), light)
        im.putpixel((7, 7), hexc("FFFFFF"))
        im.putpixel((8, 8), hexc("FFFFFF"))
    else:
        draw_snowflake(im, 4, 4, hexc("F4FBFF"))
        im.putpixel((7, 7), hexc("FFFFFF"))
    return im


def block_frosted_glass():
    rnd = random.Random(21)
    im = new()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                im.putpixel((x, y), hexc("D6ECFA", 230))
                continue
            d = min(x, y, 15 - x, 15 - y)
            p = max(0.0, 1.0 - d / 5.5)
            if rnd.random() < p * 0.85:
                a = int(150 + 70 * p)
                im.putpixel((x, y), hexc("F2FAFF", a))
            else:
                im.putpixel((x, y), hexc("CFE8F8", 45))
    # fern-like frost streaks from the corners
    for sx, sy, dx, dy in ((1, 1, 1, 1), (14, 1, -1, 1), (1, 14, 1, -1), (14, 14, -1, -1)):
        x, y = sx, sy
        for _ in range(6):
            im.putpixel((x, y), hexc("FFFFFF", 235))
            if rnd.random() < 0.5:
                x += dx
            else:
                y += dy
            x = max(1, min(14, x))
            y = max(1, min(14, y))
    return im


def stone_base(seed, base, light, dark, deepslate=False):
    rnd = random.Random(seed)
    n = value_noise(16, 16, rnd, 4)
    n2 = value_noise(16, 16, rnd, 2)
    im = new()
    for y in range(16):
        for x in range(16):
            v = n[y][x] * 0.6 + n2[y][x] * 0.4
            if deepslate:
                v = v * 0.6 + (0.5 + 0.5 * math.sin(y * 1.3 + x * 0.15)) * 0.4
            c = base
            if v > 0.66:
                c = light
            elif v < 0.36:
                c = dark
            im.putpixel((x, y), jitter(c, rnd, 4))
    return im


def add_crystals(im, spots):
    o, d, l, w = hexc("2B5F94"), hexc("4FA3DE"), hexc("9EE0FA"), hexc("F0FCFF")
    for (x, y) in spots:
        shape = {(0, -1): l, (-1, 0): d, (0, 0): w, (1, 0): l, (0, 1): d, (1, -1): l, (-1, 1): o, (1, 1): o, (0, 2): o}
        for (dx, dy), c in shape.items():
            px, py = x + dx, y + dy
            if 0 <= px < 16 and 0 <= py < 16:
                im.putpixel((px, py), c)
    return im


def block_frost_crystal_block():
    rnd = random.Random(31)
    im = new()
    top, left, right, bottom = hexc("BDEBFF"), hexc("8ED3F5"), hexc("5FB2E6"), hexc("3E8ACB")
    for y in range(16):
        for x in range(16):
            u, v = x - 7.5, y - 7.5
            if x in (0, 15) or y in (0, 15):
                c = hexc("2F6FA8")
            elif abs(u) + abs(v) < 4:
                c = hexc("E6F9FF")
            elif abs(v) >= abs(u):
                c = top if v < 0 else bottom
            else:
                c = left if u < 0 else right
            if abs(abs(u) - abs(v)) < 0.6 and not (x in (0, 15) or y in (0, 15)):
                c = mix(c, hexc("FFFFFF"), 0.35)
            im.putpixel((x, y), jitter(c, rnd, 3))
    for (x, y) in ((3, 3), (12, 4), (4, 12), (11, 11)):
        im.putpixel((x, y), hexc("FFFFFF"))
    return im


def block_fur(seed):
    rnd = random.Random(seed)
    im = new()
    phases = [rnd.random() * 6.28 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            v = math.sin(y * 0.9 + phases[x]) * 0.5 + 0.5
            c = hexc("EEF3F6")
            if v > 0.82:
                c = hexc("FFFFFF")
            elif v < 0.2:
                c = hexc("C8D5DE")
            im.putpixel((x, y), jitter(c, rnd, 4))
    return im


def block_snowy_leaves():
    rnd = random.Random(41)
    n = value_noise(16, 16, rnd, 4)
    im = new()
    greens = [hexc("1F4A33"), hexc("2A5E3F"), hexc("356F4A"), hexc("427F56")]
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            snow = n[y][x] > 0.58 and (y % 4) < 3
            if snow:
                c = hexc("F2F8FC") if r > 0.3 else hexc("D6E4EE")
            elif r < 0.14:
                continue  # hole (cutout)
            else:
                c = greens[min(3, int(r * 4))]
            im.putpixel((x, y), c)
    return im


def block_icicle_sprite():
    """Flat icicle picture used for the item icon."""
    im = new()
    edge, mid, core, white = hexc("8CC3E6", 235), hexc("BFE4F8", 235), hexc("E4F6FF", 240), hexc("FFFFFF", 245)
    for x in range(16):
        im.putpixel((x, 0), mid)
        if x % 3 != 1:
            im.putpixel((x, 1), edge)
    for cx, length in ((3, 10), (8, 15), (12, 7)):
        for y in range(1, length):
            half = max(0, int(round(1.6 * (1 - y / length))))
            for dx in range(-half, half + 1):
                x = cx + dx
                if 0 <= x < 16:
                    im.putpixel((x, y), core if dx == 0 else (mid if abs(dx) < half else edge))
        im.putpixel((cx, length), edge)
        im.putpixel((cx - 1 if cx > 0 else cx, 2), white)
    return im


def block_icicle():
    """Ice material for the 3D icicle model (made in Blockbench)."""
    rnd = random.Random(14)
    im = new()
    phases = [rnd.random() * 6.28 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            v = math.sin(y * 0.5 + phases[x]) * 0.5 + 0.5
            c = mix(hexc("D9F1FC"), hexc("A8D6F2"), y / 15.0)
            if v > 0.85:
                c = hexc("F4FCFF")
            elif v < 0.15:
                c = mix(c, hexc("7FB8E0"), 0.6)
            im.putpixel((x, y), jitter(c, rnd, 3))
    return im


def block_sapling():
    im = new()
    trunk = hexc("5A3A1E")
    for y in range(12, 16):
        im.putpixel((7, y), trunk)
        im.putpixel((8, y), scale(trunk, 0.8))
    greens = (hexc("2D6A45"), hexc("3D8556"), hexc("1E4D32"))
    tiers = ((3, 2, 6), (6, 3, 9), (9, 4, 12))
    for top, half, bottom in tiers:
        for y in range(top, bottom):
            w = int((y - top + 1) / (bottom - top) * half) + 1
            for x in range(8 - w, 8 + w):
                im.putpixel((x, y), greens[(x + y) % 2] if x < 8 else greens[2])
    for (x, y), c in (((5, 8), hexc("D3232F")), ((10, 10), hexc("E3B23C")), ((7, 5), hexc("2E6FD6")),
                      ((9, 7), hexc("D3232F")), ((4, 11), hexc("E3B23C"))):
        im.putpixel((x, y), c)
    for (x, y) in ((7, 1), (8, 1), (7, 2), (8, 2), (6, 2), (9, 2), (7, 0), (8, 0)):
        pass
    im.putpixel((7, 1), hexc("FFE14D"))
    im.putpixel((8, 1), hexc("FFE14D"))
    im.putpixel((7, 2), hexc("FFC21A"))
    im.putpixel((8, 2), hexc("FFC21A"))
    return im


def block_star():
    big = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(big)
    cx, cy, ro, ri = 7.5, 8.0, 7.6, 3.1
    pts = []
    for i in range(10):
        ang = -math.pi / 2 + i * math.pi / 5
        r = ro if i % 2 == 0 else ri
        pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    draw.polygon(pts, fill=hexc("F5C542"))
    for y in range(16):
        for x in range(16):
            p = big.getpixel((x, y))
            if p[3]:
                d = math.hypot(x - cx, y - cy)
                c = hexc("FFF6C2") if d < 1.8 else (hexc("FFE073") if d < 3.5 else hexc("F5B82E"))
                if x > cx + 1 and y > cy:
                    c = scale(c, 0.85)
                big.putpixel((x, y), c)
    return outline(big, hexc("A8650C"))


def block_garland_frames():
    frames = []
    colors = [hexc("FF3B3B"), hexc("FFD23F"), hexc("3BD16F"), hexc("3B8BFF")]
    wire = hexc("1E5A2E")
    wire_points = []
    for row_y in (2, 9):
        for x in range(16):
            y = row_y + int(round(3.2 * math.sin(math.pi * (x + 0.5) / 16)))
            wire_points.append((x, y, row_y))
    for f in range(4):
        im = new()
        for x, y, _ in wire_points:
            im.putpixel((x, y), wire)
        idx = 0
        for x, y, row_y in wire_points:
            if x % 3 == 1:
                c = colors[(idx + f) % 4]
                lit = (idx + f) % 2 == 0
                if not lit:
                    c = scale(c, 0.55)
                im.putpixel((x, y + 1), c)
                im.putpixel((x, y + 2), mix(c, hexc("FFFFFF"), 0.45) if lit else c)
                idx += 1
        frames.append(im)
    return frames


def block_bauble(color):
    """Layout for the Blockbench bauble model: ball material 8x8 at (0,0), cap 3x2 at (9,0), string 1x6 at (13,0)."""
    base, light, dark = {
        "red": (hexc("D3232F"), hexc("F2545B"), hexc("8F121B")),
        "gold": (hexc("E3B23C"), hexc("FFE07A"), hexc("A87A1A")),
        "blue": (hexc("2E6FD6"), hexc("6FA6F5"), hexc("1A438F")),
    }[color]
    im = new()
    for y in range(8):
        for x in range(8):
            t = (x + y) / 14.0
            c = mix(light, base, min(1.0, t * 1.6)) if t < 0.5 else mix(base, dark, (t - 0.5) * 1.6)
            im.putpixel((x, y), c)
    for (x, y) in ((2, 2), (3, 2), (2, 3)):
        im.putpixel((x, y), hexc("FFFFFF"))
    cap = hexc("C9CED6") if color == "gold" else hexc("E0B43A")
    for x in range(9, 12):
        im.putpixel((x, 0), cap)
        im.putpixel((x, 1), scale(cap, 0.75))
    for y in range(6):
        im.putpixel((13, y), hexc("C9A227"))
    return im


def block_gift(wrap, wrap_dark, ribbon, ribbon_dark, dots):
    """Layout for the Blockbench gift model.
    side: rows 0-2 ribbon (bow), rows 3-5 lid band, rows 6-15 box side. top: the lid."""
    rnd = random.Random(sum(wrap))
    side, top = new(), new()
    for im in (side, top):
        for y in range(16):
            for x in range(16):
                c = wrap
                if (x + 2 * (y // 4)) % 4 == 0 and y % 4 == 1:
                    c = dots
                im.putpixel((x, y), jitter(c, rnd, 3))
    for x in range(16):
        side.putpixel((x, 0), ribbon)
        side.putpixel((x, 1), ribbon if x % 4 else mix(ribbon, ribbon_dark, 0.5))
        side.putpixel((x, 2), ribbon_dark)
        for y in (3, 4):
            side.putpixel((x, y), jitter(scale(wrap, 1.1), rnd, 3))
        side.putpixel((x, 5), wrap_dark)
        side.putpixel((x, 15), wrap_dark)
    for y in range(3, 16):
        side.putpixel((7, y), ribbon)
        side.putpixel((8, y), ribbon_dark)
    for y in range(6, 16):
        side.putpixel((2, y), mix(wrap, wrap_dark, 0.6))
        side.putpixel((13, y), mix(wrap, wrap_dark, 0.6))
    for i in range(16):
        top.putpixel((7, i), ribbon)
        top.putpixel((8, i), ribbon_dark)
        top.putpixel((i, 7), ribbon)
        top.putpixel((i, 8), ribbon_dark)
    for i in range(1, 15):
        for (x, y) in ((1, i), (14, i), (i, 1), (i, 14)):
            if top.getpixel((x, y)) not in (ribbon, ribbon_dark):
                top.putpixel((x, y), mix(wrap, wrap_dark, 0.6))
    return side, top


def block_fireplace():
    """Textures for the 3D fireplace model (made in Blockbench)."""
    out = {}
    out["fireplace_side"] = bricks(hexc("A0442C"), hexc("B85A3E"), hexc("7E3220"), hexc("C9B9A6"), 51)
    out["fireplace_soot"] = bricks(hexc("3E2C26"), hexc("4C3730"), hexc("2C1F1B"), hexc("1C1310"), 54)
    rnd = random.Random(52)
    top = new()
    for y in range(16):
        for x in range(16):
            c = hexc("8A8A8A")
            if x in (0, 15) or y in (0, 15):
                c = hexc("6E6E6E")
            elif x == 7 or y == 7:
                c = hexc("7A7A7A")
            elif x in (1, 8) or y in (1, 8):
                c = hexc("9E9E9E")
            top.putpixel((x, y), jitter(c, rnd, 4))
    out["fireplace_top"] = top

    mantel = new()
    for y in range(16):
        for x in range(16):
            c = hexc("6B4626")
            if y % 4 == 3:
                c = hexc("472C15")
            elif (x * 3 + y * 7) % 11 == 0:
                c = hexc("7E5531")
            mantel.putpixel((x, y), jitter(c, rnd, 3))
    out["fireplace_mantel"] = mantel

    log = new()
    phases = [rnd.random() * 6.28 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            v = math.sin(x * 1.7 + phases[y // 4]) * 0.5 + 0.5
            c = hexc("5A3A1E") if v > 0.4 else hexc("3E2812")
            if v > 0.9:
                c = hexc("6E4A2A")
            log.putpixel((x, y), jitter(c, rnd, 3))
    out["fireplace_log"] = log

    log_top = new()
    for y in range(16):
        for x in range(16):
            r = max(abs(x - 7.5), abs(y - 7.5))
            if r > 6.5:
                c = hexc("4E3218")
            elif int(r) % 2 == 0:
                c = hexc("A87A4A")
            else:
                c = hexc("8B5E34")
            log_top.putpixel((x, y), jitter(c, rnd, 3))
    out["fireplace_log_top"] = log_top

    # animated fire: 8 frames, flames rising from the bottom, transparent above
    frames = []
    for f in range(8):
        frnd = random.Random(500 + f)
        im = new()
        heights = []
        for x in range(16):
            base = 7 + 5 * math.sin((x + f * 0.9) * 0.7) * math.cos(x * 0.33 - f * 0.5)
            heights.append(max(2, min(15, int(base + frnd.uniform(-1.5, 2.5)))))
        for x in range(16):
            h = heights[x]
            for k in range(h):
                y = 15 - k
                t = k / max(1, h - 1)
                if t < 0.25:
                    c = hexc("FFF4C2")
                elif t < 0.5:
                    c = hexc("FFD23F")
                elif t < 0.8:
                    c = hexc("F7931E")
                else:
                    c = hexc("E2462B")
                if x in (0, 15) and t > 0.5:
                    continue
                im.putpixel((x, y), c)
        frames.append(im)
    out["fireplace_fire_frames"] = frames
    return out


def gen_blocks():
    ice = bricks(hexc("8FC0E8"), hexc("C7E6FA"), hexc("6FA3D4"), hexc("E3F4FF"), 1, sparkle=hexc("FFFFFF"))
    save(ice, "block", "ice_bricks.png")
    save(block_chiseled_ice(), "block", "chiseled_ice_bricks.png")
    save(block_chiseled_ice(True), "block", "chiseled_ice_bricks_top.png")
    snow = bricks(hexc("EEF3F7"), hexc("FFFFFF"), hexc("D6E1EA"), hexc("B5C5D4"), 2)
    save(snow, "block", "snow_bricks.png")
    save(block_frosted_glass(), "block", "frosted_glass.png")

    ore = stone_base(61, hexc("7F7F7F"), hexc("979797"), hexc("676767"))
    save(add_crystals(ore, ((3, 3), (11, 5), (6, 10), (12, 12))), "block", "frost_crystal_ore.png")
    dore = stone_base(62, hexc("4D4D52"), hexc("5E5E64"), hexc("3A3A3F"), deepslate=True)
    save(add_crystals(dore, ((4, 4), (11, 3), (8, 10), (2, 12))), "block", "deepslate_frost_crystal_ore.png")
    save(block_frost_crystal_block(), "block", "frost_crystal_block.png")
    save(block_fur(71), "block", "yeti_fur_block.png")
    save(block_snowy_leaves(), "block", "snowy_spruce_leaves.png")
    save(block_icicle(), "block", "icicle.png")
    save(block_icicle_sprite(), "item", "icicle.png")
    save(block_sapling(), "block", "festive_spruce_sapling.png")
    save(block_star(), "block", "star.png")

    frames = block_garland_frames()
    garland = new(16, 64)
    for i, fr in enumerate(frames):
        garland.paste(fr, (0, i * 16))
    save(garland, "block", "garland.png", preview=False)
    PREVIEW.append(("block/garland.png", frames[0]))
    save_mcmeta(10, "block", "garland.png")

    for color in ("red", "gold", "blue"):
        save(block_bauble(color), "block", f"{color}_bauble.png")
    gifts = {
        "red": (hexc("C8102E"), hexc("8A0A1F"), hexc("F2C14E"), hexc("C99520"), hexc("F7E7E7")),
        "green": (hexc("1E7B3A"), hexc("124F25"), hexc("D7263D"), hexc("9A1424"), hexc("F2C14E")),
        "blue": (hexc("1F5FBF"), hexc("143E80"), hexc("EEF3F7"), hexc("B5C5D4"), hexc("CFE3FF")),
    }
    for color, args in gifts.items():
        side, top = block_gift(*args)
        save(side, "block", f"{color}_gift_side.png")
        save(top, "block", f"{color}_gift_top.png")

    fp = block_fireplace()
    for name in ("fireplace_side", "fireplace_soot", "fireplace_top", "fireplace_mantel", "fireplace_log",
                 "fireplace_log_top"):
        save(fp[name], "block", f"{name}.png")
    fire = new(16, 16 * len(fp["fireplace_fire_frames"]))
    for i, fr in enumerate(fp["fireplace_fire_frames"]):
        fire.paste(fr, (0, i * 16))
    save(fire, "block", "fireplace_fire.png", preview=False)
    PREVIEW.append(("block/fireplace_fire.png", fp["fireplace_fire_frames"][0]))
    save_mcmeta(3, "block", "fireplace_fire.png")


# =====================================================================================================
# Box-UV painting (armor & entities)
# =====================================================================================================

def paint_box(im, u, v, w, h, d, painter, faces=("top", "bottom", "right", "front", "left", "back")):
    """painter(face, x, y, fw, fh) -> color or None. Standard Minecraft cuboid UV layout."""
    rects = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }
    for face in faces:
        rx, ry, fw, fh = rects[face]
        for y in range(fh):
            for x in range(fw):
                c = painter(face, x, y, fw, fh)
                if c is not None:
                    im.putpixel((rx + x, ry + y), c)


# --- Armor -----------------------------------------------------------------------------------------

def armor_textures():
    rnd = random.Random(81)
    layer1 = new(64, 32)
    layer2 = new(64, 32)
    skates = new(64, 32)

    fur, fur_l, fur_d = hexc("6E4A2B"), hexc("8C6239"), hexc("4E3218")
    brim, brim_l = hexc("A8896A"), hexc("C4A887")

    def ushanka(face, x, y, fw, fh):
        if face == "top":
            return jitter(fur_l if (x + y) % 3 == 0 else fur, rnd, 6)
        if face == "bottom":
            return None
        if face == "front":
            if y <= 1:
                if y == 1 and x in (3, 4):
                    return hexc("E0B43A")
                return jitter(brim_l if (x + y) % 2 else brim, rnd, 6)
            if y == 2:
                return jitter(brim, rnd, 6)
            return None
        if face in ("right", "left"):
            if y <= 2:
                return jitter(fur, rnd, 6)
            if 2 <= x <= 5:
                return jitter(brim_l if (x + y) % 2 else brim, rnd, 6)  # ear flaps
            return jitter(brim, rnd, 6) if y <= 3 else None
        if face == "back":
            if y <= 2:
                return jitter(fur, rnd, 6)
            if y <= 6:
                return jitter(brim if (x + y) % 2 else brim_l, rnd, 6)
            return None
        return None

    paint_box(layer1, 0, 0, 8, 8, 8, ushanka)

    red, red_d, white = hexc("C62828"), hexc("9E1E1E"), hexc("F5F5F5")

    def sweater_body(face, x, y, fw, fh):
        if face in ("top", "bottom"):
            return red
        if y == 0 and face == "front" and 2 <= x <= 5:
            return white  # collar
        if y in (3, 4):
            return white if (x + y) % 2 == 0 else red  # nordic band
        if y == 5 and x % 4 == 1:
            return white
        if y == fh - 1:
            return white
        return jitter(red if (x + y) % 5 else red_d, rnd, 4)

    def sweater_arm(face, x, y, fw, fh):
        if face == "top":
            return red
        if face == "bottom":
            return white
        if y in (3, 4):
            return white if (x + y) % 2 == 0 else red
        if y >= fh - 2:
            return white
        return jitter(red if (x + y) % 5 else red_d, rnd, 4)

    paint_box(layer1, 16, 16, 8, 12, 4, sweater_body)
    paint_box(layer1, 40, 16, 4, 12, 4, sweater_arm)

    felt, felt_l, felt_d = hexc("8E8E8E"), hexc("ABABAB"), hexc("5E5E5E")

    def valenki(face, x, y, fw, fh):
        if face == "top":
            return None
        if face == "bottom":
            return felt_d
        if y < 4:
            return None
        if y == 4:
            return red if x % 2 == 0 else white
        if y == fh - 1:
            return felt_d
        return jitter(felt_l if (x + y) % 4 == 0 else felt, rnd, 5)

    paint_box(layer1, 0, 16, 4, 12, 4, valenki)

    pants, pants_d = hexc("4A5568"), hexc("343C4A")

    def pants_body(face, x, y, fw, fh):
        if face in ("top", "bottom"):
            return pants if face == "bottom" else None
        if y < 7:
            return None
        if y == 7:
            return hexc("2A2F3A")  # waistband
        return pants_d if (y - 8) % 3 == 0 else jitter(pants, rnd, 4)

    def pants_leg(face, x, y, fw, fh):
        if face == "top":
            return pants
        if face == "bottom":
            return None
        return pants_d if y % 3 == 0 or (face == "front" and x == fw - 1) else jitter(pants, rnd, 4)

    paint_box(layer2, 16, 16, 8, 12, 4, pants_body)
    paint_box(layer2, 0, 16, 4, 12, 4, pants_leg)

    boot_w, lace = hexc("F2F4F6"), hexc("9AA6B2")

    def skate(face, x, y, fw, fh):
        if face == "top":
            return None
        if face == "bottom":
            return hexc("C9D3DC")
        if y < 6:
            return None
        if y == fh - 1:
            return hexc("6B7785")
        if face == "front" and x in (1, 2) and y < fh - 2 and y % 2 == 0:
            return lace
        return jitter(boot_w, rnd, 3)

    paint_box(skates, 0, 16, 4, 12, 4, skate)
    return layer1, layer2, skates


# Entity textures (yeti, Father Frost) are painted in tools/models.py, next to the Blockbench models.

# =====================================================================================================
# GUI: effect icon, mod icon
# =====================================================================================================

def effect_warmth():
    im = new(18, 18)
    for y in range(18):
        for x in range(18):
            h = (16 - y) / 14.0  # 0 at bottom, 1 at top
            if h < 0 or h > 1:
                continue
            width = 6.5 * math.sin(math.pi * min(1.0, h ** 0.75)) * (1 - h * 0.35)
            dx = x + 0.5 - 9 - math.sin(h * 3.0) * 1.2 * h
            if abs(dx) <= width:
                t = abs(dx) / max(0.5, width)
                if t < 0.35 and h < 0.6:
                    c = hexc("FFF4C2")
                elif t < 0.6 and h < 0.8:
                    c = hexc("FFD23F")
                elif t < 0.85:
                    c = hexc("F7931E")
                else:
                    c = hexc("E2462B")
                im.putpixel((x, y), c)
    return outline(im, hexc("7A2E00"))


def mod_icon():
    rnd = random.Random(111)
    s = new(32, 32)
    top, bottom = hexc("0B1E3F"), hexc("24588A")
    for y in range(32):
        for x in range(32):
            s.putpixel((x, y), mix(top, bottom, y / 31))
    for _ in range(26):
        x, y = rnd.randrange(32), rnd.randrange(24)
        s.putpixel((x, y), hexc("FFFFFF") if rnd.random() < 0.5 else hexc("BFD9F2"))
    for y in range(26, 32):
        for x in range(32):
            if y > 26 + int(1.5 * math.sin(x / 5.0)):
                s.putpixel((x, y), hexc("F2F8FC") if (x + y) % 5 else hexc("D6E4EE"))
    # tree
    for y in range(24, 28):
        s.putpixel((15, y), hexc("5A3A1E"))
        s.putpixel((16, y), hexc("4A2E16"))
    greens = (hexc("1E5A36"), hexc("2D7A4A"), hexc("3D8F5A"))
    for top_y, height, half in ((6, 7, 5), (10, 8, 8), (15, 10, 11)):
        for y in range(top_y, top_y + height):
            w = int((y - top_y + 1) / height * half) + 1
            for x in range(16 - w, 16 + w):
                s.putpixel((x, y), greens[(x * 3 + y) % 3])
            if y == top_y + height - 1:
                for x in range(16 - w, 16 + w):
                    if x % 2 == 0:
                        s.putpixel((x, y), hexc("F2F8FC"))
    ornaments = [(13, 11, "D3232F"), (18, 13, "E3B23C"), (11, 16, "2E6FD6"), (20, 18, "D3232F"),
                 (15, 19, "E3B23C"), (9, 22, "E3B23C"), (22, 23, "2E6FD6"), (16, 9, "D3232F"), (13, 23, "D3232F")]
    for x, y, c in ornaments:
        s.putpixel((x, y), hexc(c))
    for (x, y) in ((15, 3), (16, 3), (14, 4), (15, 4), (16, 4), (17, 4), (15, 5), (16, 5), (15, 2), (16, 2)):
        s.putpixel((x, y), hexc("FFE14D"))
    s.putpixel((13, 4), hexc("FFC21A"))
    s.putpixel((18, 4), hexc("FFC21A"))
    # gift
    for y in range(24, 29):
        for x in range(23, 29):
            s.putpixel((x, y), hexc("C8102E"))
    for y in range(24, 29):
        s.putpixel((25, y), hexc("F2C14E"))
        s.putpixel((26, y), hexc("F2C14E"))
    for x in range(23, 29):
        s.putpixel((x, 25), hexc("F2C14E"))
    s.putpixel((24, 23), hexc("F2C14E"))
    s.putpixel((27, 23), hexc("F2C14E"))
    return s.resize((128, 128), Image.NEAREST)


def preview_sheet():
    cell = 72
    cols = 10
    rows = (len(PREVIEW) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * cell, rows * cell), hexc("3C4450"))
    draw = ImageDraw.Draw(sheet)
    for i, (name, im) in enumerate(PREVIEW):
        x, y = (i % cols) * cell, (i // cols) * cell
        # checkerboard behind transparent pixels
        for yy in range(0, 64, 8):
            for xx in range(0, 64, 8):
                c = hexc("59626F") if (xx + yy) // 8 % 2 else hexc("4A525E")
                draw.rectangle([x + 4 + xx, y + 2 + yy, x + 4 + xx + 7, y + 2 + yy + 7], fill=c)
        f = 64 / max(im.size)
        big = im.resize((max(1, int(im.size[0] * f)), max(1, int(im.size[1] * f))), Image.NEAREST)
        sheet.alpha_composite(big, (x + 4, y + 2))
        draw.text((x + 2, y + 62), name.split("/")[-1][:12], fill=(255, 255, 255, 255))
    return sheet


def main():
    gen_items()
    gen_blocks()
    l1, l2, sk = armor_textures()
    save(l1, "models", "armor", "winter_clothing_layer_1.png")
    save(l2, "models", "armor", "winter_clothing_layer_2.png")
    save(sk, "models", "armor", "ice_skates_layer_1.png")
    save(effect_warmth(), "mob_effect", "warmth.png")
    icon = mod_icon()
    icon_path = os.path.join(OUT, "..", "icon.png")
    icon.save(icon_path)
    PREVIEW.append(("icon.png", icon))
    print(f"Wrote {len(PREVIEW)} textures")
    if "--preview" in sys.argv:
        out = os.path.join(HERE, "..", "build", "texture-preview.png")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        preview_sheet().save(out)
        print("Preview:", os.path.abspath(out))


if __name__ == "__main__":
    main()
