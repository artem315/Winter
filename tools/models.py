#!/usr/bin/env python3
"""
Creates the Blockbench projects of the Winter mod's 3D models: blockbench/*.bbmodel.

    yeti.bbmodel, ded_moroz.bbmodel     - entity models ("Modded Entity" format, box UV), textures included
    fireplace.bbmodel, fireplace_on.bbmodel, template_gift.bbmodel,
    template_bauble.bbmodel, icicle.bbmodel, star.bbmodel   - block models ("Java Block/Item" format)

This script was used to build the first version of every model. From then on the .bbmodel files are the
source of truth: open them in Blockbench (https://web.blockbench.net or the desktop app), edit, save, and run

    node tools/blockbench_export.js

which loads each project in Blockbench and exports the game files (Java model code, block JSON, entity
textures) plus preview renders. Re-running this script overwrites the projects, so it refuses to do that
unless you pass --force.

Requires Pillow and the block textures from tools/textures.py.
"""
import base64
import io
import json
import math
import os
import random
import sys
import uuid

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from textures import OUT as TEXTURES, hexc, jitter, mix, new, paint_box, scale  # noqa: E402

BB_DIR = os.path.join(HERE, "..", "blockbench")
UUID_NS = uuid.UUID("6b1c7a3e-3d1f-4b8e-9c55-1f0c2a4e7d10")
FACES = ("north", "east", "south", "west", "up", "down")


def uid(*parts):
    return str(uuid.uuid5(UUID_NS, "/".join(parts)))


def png_data_url(im):
    buf = io.BytesIO()
    im.save(buf, "PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode("ascii")


def texture_entry(model, key, name, folder, image, frame_time=1):
    w, h = image.size
    return {
        "path": "", "name": f"{name}.png", "folder": folder, "namespace": "winter", "id": key,
        "group": "", "width": w, "height": h, "uv_width": w if folder == "entity" else 16,
        "uv_height": h if folder == "entity" else 16, "particle": False, "use_as_default": False,
        "layers_enabled": False, "sync_to_project": "", "render_mode": "default", "render_sides": "auto",
        "pbr_channel": "color", "frame_time": frame_time, "frame_order_type": "loop", "frame_order": "",
        "frame_interpolate": False, "visible": True, "internal": True, "saved": False,
        "uuid": uid(model, "texture", key), "source": png_data_url(image),
    }


# =====================================================================================================
# Entity models (Modded Entity, box UV). Blockbench coordinates: y up, feet at y=0, the model looks
# towards -Z (north); the entity's right side is +X.
# =====================================================================================================

class ECube:
    def __init__(self, name, frm, to, paint=None, inflate=0.0, mirror_of=None):
        self.name, self.frm, self.to, self.paint, self.inflate = name, frm, to, paint, inflate
        self.mirror_of = mirror_of  # path "bone/cube" of a cube whose texture is reused mirrored
        self.uv = None

    @property
    def size(self):
        return tuple(int(round(self.to[i] - self.frm[i])) for i in range(3))


class Bone:
    def __init__(self, name, origin, rotation=(0, 0, 0), cubes=(), bones=()):
        self.name, self.origin, self.rotation = name, origin, rotation
        self.cubes, self.bones = list(cubes), list(bones)


def walk(bones, path=""):
    for b in bones:
        p = f"{path}/{b.name}" if path else b.name
        yield p, b
        yield from walk(b.bones, p)


def pack_uv(bones, tex_w, tex_h):
    """Places box-UV footprints (2d+2w x d+h) of all cubes into the texture: largest first,
    each at the top-most, then left-most free spot."""
    cubes = {}
    for path, bone in walk(bones):
        for c in bone.cubes:
            cubes[f"{path}/{c.name}"] = c
    own = [c for c in cubes.values() if c.mirror_of is None]
    own.sort(key=lambda c: (2 * c.size[2] + 2 * c.size[0]) * (c.size[2] + c.size[1]), reverse=True)
    used = [[False] * tex_w for _ in range(tex_h)]

    def free(x, y, fw, fh):
        return all(not any(used[yy][x:x + fw]) for yy in range(y, y + fh))

    for c in own:
        w, h, d = c.size
        fw, fh = 2 * d + 2 * w, d + h
        spot = next(((x, y) for y in range(tex_h - fh + 1) for x in range(tex_w - fw + 1) if free(x, y, fw, fh)), None)
        assert spot, f"texture {tex_w}x{tex_h} too small for {c.name}"
        c.uv = spot
        for yy in range(spot[1], spot[1] + fh):
            for xx in range(spot[0], spot[0] + fw):
                used[yy][xx] = True
    for c in cubes.values():
        if c.mirror_of is not None:
            c.uv = cubes[c.mirror_of].uv
    return cubes


def entity_model(name, bones, tex_w, tex_h, hitbox, seed):
    cubes = pack_uv(bones, tex_w, tex_h)
    image = new(tex_w, tex_h)
    rnd = random.Random(seed)
    for c in cubes.values():
        if c.mirror_of is None and c.paint is not None:
            w, h, d = c.size
            paint_box(image, c.uv[0], c.uv[1], w, h, d, lambda f, x, y, fw, fh, c=c: c.paint(f, x, y, fw, fh, rnd))

    elements, color = [], 0

    def group(path, bone):
        nonlocal color
        children = []
        for c in bone.cubes:
            e = {
                "name": c.name, "box_uv": True, "rescale": False, "locked": False, "render_order": "default",
                "allow_mirror_modeling": True, "from": list(c.frm), "to": list(c.to), "autouv": 0,
                "color": color % 8, "origin": list(bone.origin), "uv_offset": list(c.uv),
                "type": "cube", "uuid": uid(name, path, c.name),
            }
            if c.inflate:
                e["inflate"] = c.inflate
            if c.mirror_of is not None:
                e["mirror_uv"] = True
            elements.append(e)
            children.append(e["uuid"])
        color += 1
        for b in bone.bones:
            children.append(group(f"{path}/{b.name}", b))
        return {"name": bone.name, "origin": list(bone.origin), "rotation": list(bone.rotation), "color": 0,
                "uuid": uid(name, path), "export": True, "mirror_uv": False, "isOpen": True, "locked": False,
                "visibility": True, "autouv": 0, "children": children}

    outliner = [group(b.name, b) for b in bones]
    tex = texture_entry(name, "0", name, "entity", image)
    tex["particle"] = True
    return {
        "meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
        "name": name, "model_identifier": name, "modded_entity_entity_class": "",
        "modded_entity_version": "1.17", "modded_entity_flip_y": True,
        "visible_box": list(hitbox), "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": tex_w, "height": tex_h},
        "elements": elements, "outliner": outliner, "textures": [tex], "animations": [],
    }, image


# --- Yeti -------------------------------------------------------------------------------------------

def yeti():
    fur_base, fur_l, fur_d, fur_dd = hexc("EEF3F6"), hexc("FFFFFF"), hexc("D3DEE6"), hexc("B6C6D2")
    skin, skin_d = hexc("6C8EA8"), hexc("4E6E88")

    def fur(x, y, rnd, darker=0.0):
        v = math.sin(y * 1.1 + (x * 7) % 13) * 0.5 + 0.5
        c = fur_base
        if v > 0.8:
            c = fur_l
        elif v < 0.12:
            c = fur_d
        if rnd.random() < 0.035:
            c = fur_dd
        return jitter(mix(c, fur_dd, darker), rnd, 3)

    def p_fur(darker=0.0):
        return lambda f, x, y, fw, fh, rnd: fur(x, y, rnd, darker)

    def p_torso(f, x, y, fw, fh, rnd):
        if f == "front" and 4 <= x < fw - 4 and 2 <= y < fh - 3:
            return mix(fur(x, y, rnd), fur_l, 0.5)  # lighter belly
        return fur(x, y, rnd)

    def p_skull(f, x, y, fw, fh, rnd):
        if f != "front":
            return fur(x, y, rnd)
        if y <= 1 or x in (0, fw - 1):
            return fur(x, y, rnd)
        if y == 4 and x in (2, fw - 3):
            return hexc("101820")  # eyes
        if y == 4 and x in (3, fw - 4):
            return hexc("7FD4FF")  # icy glint
        return jitter(skin, rnd, 4)

    def p_snout(f, x, y, fw, fh, rnd):
        if f == "front":
            if y == 0:
                return skin_d if x in (2, 3) else skin
            if y == 2:
                return hexc("FFFFFF") if x in (1, fw - 2) else hexc("2A3440")
            if y == 3 and x in (1, fw - 2):
                return hexc("FFFFFF")
            return jitter(skin, rnd, 3)
        if f == "top":
            return skin_d
        return jitter(skin, rnd, 3)

    def p_horn(f, x, y, fw, fh, rnd):
        t = y / max(1, fh - 1)
        c = mix(hexc("F4FCFF"), hexc("7FB8E0"), t)
        if f == "top":
            c = hexc("FFFFFF")
        return c

    def p_fist(f, x, y, fw, fh, rnd):
        if f == "bottom":
            return skin_d
        if f == "top":
            return fur(x, y, rnd)
        if y == 0:
            return fur(x, y, rnd)
        if f == "front" and y == 1 and x % 2 == 1:
            return skin_d  # knuckles
        return jitter(skin, rnd, 4)

    def p_foot(f, x, y, fw, fh, rnd):
        if f == "bottom":
            return skin_d
        if f == "top" or y == 0:
            return fur(x, y, rnd)
        if f == "front" and y == fh - 1 and x % 2 == 0:
            return skin_d  # toes
        return jitter(skin, rnd, 4)

    bones = [
        Bone("body", (0, 15, 2), (-12, 0, 0), cubes=[
            ECube("torso", (-9, 14, -4), (9, 31, 7), p_torso),
            ECube("hump", (-7, 29, -1), (7, 35, 7), p_fur(0.1)),
            ECube("crest", (-2, 34, 1), (2, 37, 5), p_fur(0.3)),
        ]),
        Bone("head", (0, 30, -8), cubes=[
            ECube("skull", (-5, 28, -16), (5, 38, -7), p_skull),
            ECube("brow", (-5, 35, -17), (5, 37, -16), p_fur(0.35)),
            ECube("snout", (-3, 29, -18), (3, 33, -16), p_snout),
            ECube("right_horn_base", (5, 35, -12), (7, 37, -10), p_horn),
            ECube("right_horn_tip", (6, 37, -12), (8, 41, -10), p_horn),
            ECube("left_horn_base", (-7, 35, -12), (-5, 37, -10), mirror_of="head/right_horn_base"),
            ECube("left_horn_tip", (-8, 37, -12), (-6, 41, -10), mirror_of="head/right_horn_tip"),
        ]),
        Bone("right_arm", (10, 28, -2), cubes=[
            ECube("shoulder", (9, 24, -6), (17, 31, 2), p_fur(0.05)),
            ECube("arm", (10, 6, -5), (16, 25, 1), p_fur()),
            ECube("fist", (9, 1, -6), (17, 6, 2), p_fist),
        ]),
        Bone("left_arm", (-10, 28, -2), cubes=[
            ECube("shoulder", (-17, 24, -6), (-9, 31, 2), mirror_of="right_arm/shoulder"),
            ECube("arm", (-16, 6, -5), (-10, 25, 1), mirror_of="right_arm/arm"),
            ECube("fist", (-17, 1, -6), (-9, 6, 2), mirror_of="right_arm/fist"),
        ]),
        Bone("right_leg", (5, 15, 2), cubes=[
            ECube("leg", (2, 3, -1), (9, 16, 6), p_fur()),
            ECube("foot", (1, 0, -4), (10, 3, 6), p_foot),
        ]),
        Bone("left_leg", (-5, 15, 2), cubes=[
            ECube("leg", (-9, 3, -1), (-2, 16, 6), mirror_of="right_leg/leg"),
            ECube("foot", (-10, 0, -4), (-1, 3, 6), mirror_of="right_leg/foot"),
        ]),
    ]
    return entity_model("yeti", bones, 128, 128, (1.5, 2.6, 0), 91)


# --- Ded Moroz (Father Frost) -------------------------------------------------------------------------

def ded_moroz():
    red, red_d, red_l = hexc("B3121F"), hexc("850C16"), hexc("D02232")
    white, white_d = hexc("F7F7F7"), hexc("D5DCE2")
    skin = hexc("F1C27D")
    silver = hexc("CFE3F5")
    gold = hexc("E0B43A")
    felt, felt_d = hexc("9A9A9A"), hexc("6E6E6E")
    clear = (0, 0, 0, 0)

    def coat(x, y, rnd):
        c = red if (x * 3 + y) % 7 else red_l
        if y % 5 == 2 and (x + y) % 6 == 0:
            c = silver  # a few embroidered snowflakes
        return jitter(c, rnd, 3)

    def fur(x, y, rnd):
        return white if (x + y) % 3 else white_d

    def p_torso(f, x, y, fw, fh, rnd):
        if f in ("top", "bottom"):
            return red
        if f == "front" and x in (fw // 2 - 1, fw // 2):
            return fur(x, y, rnd)
        return coat(x, y, rnd)

    def p_fur(f, x, y, fw, fh, rnd):
        return fur(x, y, rnd)

    def p_belt(f, x, y, fw, fh, rnd):
        if f == "front" and x in (fw // 2 - 1, fw // 2):
            return gold
        return white

    def p_head(f, x, y, fw, fh, rnd):
        if f == "top":
            return red
        if f == "bottom":
            return white
        if f == "front":
            if y == 2:
                return white if x in (1, 2, fw - 3, fw - 2) else skin  # bushy brows
            if y == 3:
                if x in (2, fw - 3):
                    return hexc("2E6DB4")
                if x in (1, fw - 2):
                    return hexc("FFFFFF")
            if y == 4 and x in (1, fw - 2):
                return hexc("E89A8A")  # rosy cheeks
            return skin
        if f in ("right", "left"):
            if 3 <= y <= 4 and x in (fw // 2 - 1, fw // 2):
                return hexc("DDA868")  # ear
            return fur(x, y, rnd)
        return fur(x, y, rnd)  # long white hair at the back

    def p_beard(f, x, y, fw, fh, rnd):
        # tapered tip: cut the bottom corners away (the entity layer supports transparency)
        if f in ("front", "back") and ((y >= fh - 2 and x in (0, fw - 1)) or (y == fh - 1 and x in (1, fw - 2))):
            return clear
        if f in ("right", "left") and y >= fh - 2:
            return clear
        return white if (x * 2 + y) % 5 else white_d

    def p_hat(f, x, y, fw, fh, rnd):
        if f == "front" and y == 1 and x in (fw // 2 - 1, fw // 2):
            return silver
        return jitter(red if f != "top" else red_l, rnd, 3)

    def p_mitten(f, x, y, fw, fh, rnd):
        if f == "bottom":
            return red_d
        if f == "front" and y == 1 and x in (1, 2):
            return white
        return jitter(red_l, rnd, 3)

    def p_staff(f, x, y, fw, fh, rnd):
        if f in ("top", "bottom"):
            return hexc("9DB8D4")
        if 3 <= y <= 4:
            return red  # ribbon
        if y % 6 in (0, 1):
            return hexc("3F5C7E")  # spiral band
        return hexc("9DB8D4") if x % 2 == 0 else hexc("7E9CBC")

    def p_crystal(f, x, y, fw, fh, rnd):
        if x == 0 and y == 0:
            return hexc("FFFFFF")
        return mix(hexc("E4F6FF"), hexc("4C9BD6"), (x + y) / max(1, fw + fh - 2))

    def p_leg(f, x, y, fw, fh, rnd):
        if f == "bottom":
            return felt_d
        if f == "top":
            return red_d
        if y >= fh - 4:
            return felt_d if y == fh - 1 else jitter(felt, rnd, 4)  # valenki
        return red_d

    bones = [
        Bone("body", (0, 24, 0), cubes=[
            ECube("torso", (-4, 13, -3), (4, 24, 3), p_torso),
            ECube("skirt", (-5, 4, -4), (5, 14, 4), p_torso),
            ECube("hem", (-5, 3, -4), (5, 5, 4), p_fur, inflate=0.5),
            ECube("belt", (-4, 13, -3), (4, 15, 3), p_belt, inflate=0.25),
            ECube("collar", (-4, 22, -3), (4, 24, 3), p_fur, inflate=0.5),
        ]),
        Bone("head", (0, 24, 0), cubes=[
            ECube("head", (-4, 24, -4), (4, 32, 4), p_head),
            ECube("nose", (-1, 27, -5), (1, 29, -4), lambda f, x, y, fw, fh, rnd: hexc("E07A7A")),
            ECube("moustache", (-3, 26, -6), (3, 27, -5), p_fur),
            ECube("beard", (-4, 16, -5), (4, 27, -3), p_beard),
            ECube("hat_brim", (-4, 30, -4), (4, 32, 4), p_fur, inflate=0.75),
            ECube("hat", (-4, 32, -4), (4, 35, 4), p_hat, inflate=0.25),
            ECube("hat_top", (-3, 35, -3), (3, 37, 3), p_hat),
        ]),
        Bone("right_arm", (5, 23, 0), cubes=[
            ECube("sleeve", (4, 13, -2), (8, 24, 2), p_torso),
            ECube("cuff", (4, 11, -2), (8, 13, 2), p_fur, inflate=0.5),
            ECube("mitten", (4, 8, -2), (8, 11, 2), p_mitten),
        ], bones=[
            Bone("staff", (6, 10, 0), cubes=[
                ECube("pole", (5, 0, -1), (7, 32, 1), p_staff),
            ], bones=[
                Bone("crystal", (6, 34, 0), (45, 0, 45), cubes=[
                    ECube("crystal", (4, 32, -2), (8, 36, 2), p_crystal),
                ]),
            ]),
        ]),
        Bone("left_arm", (-5, 23, 0), cubes=[
            ECube("sleeve", (-8, 13, -2), (-4, 24, 2), mirror_of="right_arm/sleeve"),
            ECube("cuff", (-8, 11, -2), (-4, 13, 2), inflate=0.5, mirror_of="right_arm/cuff"),
            ECube("mitten", (-8, 8, -2), (-4, 11, 2), mirror_of="right_arm/mitten"),
        ]),
        Bone("right_leg", (2, 12, 0), cubes=[ECube("leg", (0, 0, -2), (4, 12, 2), p_leg)]),
        Bone("left_leg", (-2, 12, 0), cubes=[ECube("leg", (-4, 0, -2), (0, 12, 2), mirror_of="right_leg/leg")]),
    ]
    return entity_model("ded_moroz", bones, 128, 64, (0.7, 2.3, 0), 101)


# =====================================================================================================
# Block models (Java Block/Item): coordinates 0..16, y up, the front faces north.
# =====================================================================================================

def default_uv(frm, to, face):
    x1, y1, z1 = frm
    x2, y2, z2 = to
    uv = {
        "north": [16 - x2, 16 - y2, 16 - x1, 16 - y1],
        "south": [x1, 16 - y2, x2, 16 - y1],
        "west": [z1, 16 - y2, z2, 16 - y1],
        "east": [16 - z2, 16 - y2, 16 - z1, 16 - y1],
        "up": [x1, z1, x2, z2],
        "down": [x1, 16 - z2, x2, 16 - z1],
    }[face]
    # keep the UV inside the 16x16 texture for elements that stick out of the block
    for a, b in ((0, 2), (1, 3)):
        lo, hi = uv[a], uv[b]
        if hi - lo > 16:
            uv[a], uv[b] = 0, 16
        elif lo < 0:
            uv[a], uv[b] = 0, hi - lo
        elif hi > 16:
            uv[a], uv[b] = 16 - (hi - lo), 16
    return [round(v, 4) for v in uv]


class BCube:
    def __init__(self, name, frm, to, faces, rotation=None, shade=True, rescale=False):
        """faces: {face: (texture_key, uv or None)} for the faces to draw."""
        self.name, self.frm, self.to, self.faces = name, frm, to, faces
        self.rotation, self.shade, self.rescale = rotation, shade, rescale


def all_faces(key, uv=None, only=FACES, **per_face):
    out = {f: (key, uv(f) if callable(uv) else uv) for f in only}
    for f, v in per_face.items():
        out[f] = v
    return out


def block_model(name, textures, groups, particle):
    """textures: [(key, texture file name)], groups: [(group name, [BCube])]."""
    keys = [k for k, _ in textures]
    tex_entries = []
    for key, file in textures:
        from PIL import Image
        image = Image.open(os.path.join(TEXTURES, "block", f"{file}.png")).convert("RGBA")
        frame_time = 1
        meta = os.path.join(TEXTURES, "block", f"{file}.png.mcmeta")
        if os.path.exists(meta):
            frame_time = json.load(open(meta))["animation"].get("frametime", 1)
        entry = texture_entry(name, key, file, "block", image, frame_time)
        entry["particle"] = key == particle
        tex_entries.append(entry)

    elements, outliner = [], []
    for gi, (gname, cubes) in enumerate(groups):
        children = []
        for c in cubes:
            faces = {}
            for f in FACES:
                if f in c.faces:
                    key, uv = c.faces[f]
                    faces[f] = {"uv": uv if uv is not None else default_uv(c.frm, c.to, f), "texture": keys.index(key)}
                    cull = cullface(c, f)
                    if cull:
                        faces[f]["cullface"] = cull
                else:
                    faces[f] = {"uv": [0, 0, 0, 0], "texture": None}
            rot, origin = [0, 0, 0], [8, 8, 8]
            if c.rotation:
                axis, angle, origin = c.rotation
                rot["xyz".index(axis)] = angle
            e = {"name": c.name, "box_uv": False, "rescale": c.rescale, "locked": False, "render_order": "default",
                 "allow_mirror_modeling": True, "from": list(c.frm), "to": list(c.to), "autouv": 0, "color": gi % 8,
                 "shade": c.shade, "rotation": rot, "origin": list(origin), "faces": faces, "type": "cube",
                 "uuid": uid(name, gname, c.name)}
            elements.append(e)
            children.append(e["uuid"])
        outliner.append({"name": gname, "origin": [8, 8, 8], "color": 0, "uuid": uid(name, gname), "export": True,
                         "mirror_uv": False, "isOpen": True, "locked": False, "visibility": True, "autouv": 0,
                         "children": children})
    return {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": name, "parent": "block/block", "ambientocclusion": True, "front_gui_light": False,
        "visible_box": [1, 1, 0], "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {}, "resolution": {"width": 16, "height": 16},
        "elements": elements, "outliner": outliner, "textures": tex_entries,
    }


def cullface(c, face):
    if c.rotation:
        return None
    x1, y1, z1 = c.frm
    x2, y2, z2 = c.to
    edge = {"north": z1 == 0, "south": z2 == 16, "west": x1 == 0, "east": x2 == 16, "down": y1 == 0, "up": y2 == 16}
    return face if edge[face] else None


def fireplace(lit):
    bricks = lambda **kw: all_faces("bricks", **kw)  # noqa: E731
    log_faces = {f: ("log", None) for f in ("north", "south", "up", "down")}
    log_faces.update({f: ("log_top", [6, 6, 8, 8]) for f in ("east", "west")})
    for f in ("north", "south"):
        log_faces[f] = ("log", [0, 0, 7, 2])
    for f in ("up", "down"):
        log_faces[f] = ("log", [0, 4, 7, 6])
    groups = [
        ("hearth", [BCube("hearth", (0, 0, 0), (16, 2, 16), all_faces("stone"))]),
        ("walls", [
            BCube("pillar_left", (0, 2, 0), (4, 11, 10), bricks()),
            BCube("pillar_right", (12, 2, 0), (16, 11, 10), bricks()),
            BCube("back", (0, 2, 10), (16, 16, 16), bricks(north=("soot", None))),
            BCube("lintel", (0, 11, 0), (16, 13, 10), bricks(down=("soot", None))),
            BCube("chimney", (1, 14.5, 1), (15, 16, 10), bricks()),
        ]),
        ("mantel", [BCube("mantel", (-1, 13, -1), (17, 14.5, 11), all_faces("wood"))]),
        ("logs", [
            BCube("log_front", (4.5, 2, 2.5), (11.5, 4, 4.5), dict(log_faces), rotation=("y", 22.5, (8, 3, 3.5))),
            BCube("log_back", (4, 2, 5), (12, 4, 7), dict(log_faces), rotation=("y", -22.5, (8, 3, 6))),
        ]),
    ]
    textures = [("bricks", "fireplace_side"), ("stone", "fireplace_top"), ("soot", "fireplace_soot"),
                ("wood", "fireplace_mantel"), ("log", "fireplace_log"), ("log_top", "fireplace_log_top")]
    if lit:
        fire = {"north": ("fire", [3.5, 7, 12.5, 16]), "south": ("fire", [3.5, 7, 12.5, 16])}
        groups.append(("fire", [
            BCube("flames_a", (3.5, 2, 6), (12.5, 11, 6), fire, rotation=("y", 45, (8, 6, 6)), shade=False),
            BCube("flames_b", (3.5, 2, 6), (12.5, 11, 6), fire, rotation=("y", -45, (8, 6, 6)), shade=False),
        ]))
        textures.append(("fire", "fireplace_fire"))
    return block_model("fireplace_on" if lit else "fireplace", textures, groups, "bricks")


def gift():
    ribbon = lambda w, h: ("side", [0, 0, w, h])  # noqa: E731
    loop = {"north": ribbon(4, 3), "south": ribbon(4, 3), "east": ribbon(1, 3), "west": ribbon(1, 3),
            "up": ribbon(4, 1), "down": ribbon(4, 1)}
    groups = [
        ("box", [
            BCube("box", (2, 0, 2), (14, 10, 14), all_faces("side", [2, 6, 14, 16], only=("north", "east", "south", "west"),
                                                             down=("top", [2, 2, 14, 14]))),
            BCube("lid", (1.5, 9, 1.5), (14.5, 12, 14.5), all_faces("side", [1.5, 3, 14.5, 6],
                                                                    only=("north", "east", "south", "west"),
                                                                    up=("top", [1.5, 1.5, 14.5, 14.5]),
                                                                    down=("top", [1.5, 1.5, 14.5, 14.5]))),
        ]),
        ("bow", [
            BCube("knot", (7, 12, 7), (9, 13.5, 9), all_faces("side", [7, 0, 9, 1.5])),
            BCube("loop_left", (4, 12, 7.5), (8, 15, 8.5), dict(loop), rotation=("z", -22.5, (8, 12, 8))),
            BCube("loop_right", (8, 12, 7.5), (12, 15, 8.5), dict(loop), rotation=("z", 22.5, (8, 12, 8))),
        ]),
    ]
    return block_model("template_gift", [("side", "red_gift_side"), ("top", "red_gift_top")], groups, "side")


def bauble():
    def ball(c_frm, c_to):
        def uv(face):
            w = {"north": c_to[0] - c_frm[0], "south": c_to[0] - c_frm[0], "east": c_to[2] - c_frm[2],
                 "west": c_to[2] - c_frm[2], "up": c_to[0] - c_frm[0], "down": c_to[0] - c_frm[0]}[face]
            h = {"up": c_to[2] - c_frm[2], "down": c_to[2] - c_frm[2]}.get(face, c_to[1] - c_frm[1])
            return [4 - w / 2, 4 - h / 2, 4 + w / 2, 4 + h / 2]
        return all_faces("bauble", uv)

    parts = [((5, 3, 5), (11, 9, 11)), ((4.5, 4, 5.5), (11.5, 8, 10.5)),
             ((5.5, 4, 4.5), (10.5, 8, 11.5)), ((5.5, 2.5, 5.5), (10.5, 9.5, 10.5))]
    names = ["core", "slab_x", "slab_z", "slab_y"]
    string = ("bauble", [13, 0, 14, 5.5])
    groups = [
        ("ball", [BCube(n, a, b, ball(a, b)) for n, (a, b) in zip(names, parts)]),
        ("hanger", [
            BCube("cap", (6.5, 9, 6.5), (9.5, 10.5, 9.5), all_faces("bauble", [9, 0, 12, 1.5])),
            BCube("string_a", (7.5, 10.5, 8), (8.5, 16, 8), {"north": string, "south": string}, shade=False),
            BCube("string_b", (8, 10.5, 7.5), (8, 16, 8.5), {"east": string, "west": string}, shade=False),
        ]),
    ]
    return block_model("template_bauble", [("bauble", "red_bauble")], groups, "bauble")


def icicle():
    spikes = {
        "big": [((6, 10, 6), (10, 16, 10)), ((6.5, 5, 6.5), (9.5, 10, 9.5)), ((7, 1.5, 7), (9, 5, 9)),
                ((7.5, 0, 7.5), (8.5, 1.5, 8.5))],
        "small_a": [((2, 12, 2), (5, 16, 5)), ((2.5, 9, 2.5), (4.5, 12, 4.5)), ((3, 7, 3), (4, 9, 4))],
        "small_b": [((11, 13, 10), (14, 16, 13)), ((11.5, 10, 10.5), (13.5, 13, 12.5)), ((12, 8.5, 11), (13, 10, 12))],
    }
    groups = [(g, [BCube(f"{g}_{i}", a, b, all_faces("ice")) for i, (a, b) in enumerate(cubes)])
              for g, cubes in spikes.items()]
    return block_model("icicle", [("ice", "icicle")], groups, "ice")


def star():
    full = [0, 0, 16, 16]
    groups = [
        ("star", [
            BCube("plane_a", (0.8, 0, 8), (15.2, 16, 8), {"north": ("star", full), "south": ("star", full)},
                  rotation=("y", 45, (8, 8, 8)), shade=False, rescale=True),
            BCube("plane_b", (8, 0, 0.8), (8, 16, 15.2), {"east": ("star", full), "west": ("star", full)},
                  rotation=("y", 45, (8, 8, 8)), shade=False, rescale=True),
        ]),
        ("socket", [BCube("socket", (6.5, 0, 6.5), (9.5, 1, 9.5), all_faces("star", [6, 7, 9, 8]))]),
    ]
    return block_model("star", [("star", "star")], groups, "star")


def main():
    force = "--force" in sys.argv
    os.makedirs(BB_DIR, exist_ok=True)
    yeti_model, _ = yeti()
    moroz_model, _ = ded_moroz()
    models = {
        "yeti": yeti_model, "ded_moroz": moroz_model,
        "fireplace": fireplace(False), "fireplace_on": fireplace(True),
        "template_gift": gift(), "template_bauble": bauble(), "icicle": icicle(), "star": star(),
    }
    for name, model in models.items():
        path = os.path.join(BB_DIR, f"{name}.bbmodel")
        if os.path.exists(path) and not force:
            print(f"skip {name}.bbmodel (exists; use --force to overwrite your Blockbench edits)")
            continue
        with open(path, "w", encoding="utf-8") as f:
            json.dump(model, f, indent="\t")
        print(f"wrote blockbench/{name}.bbmodel")


if __name__ == "__main__":
    main()
