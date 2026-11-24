#!/usr/bin/env python3
"""
Generates all JSON resources of the Winter mod:
blockstates, block/item models, translations, recipes, loot tables, tags,
advancements, enchantments and world generation features.

Usage:  python3 tools/datagen.py
Output: src/main/resources/{assets,data}

The generated files are committed, so you only need to re-run this after editing it.
"""
import json
import os
import shutil

MOD = "winter"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", MOD)
DATA = os.path.join(ROOT, "data")

WRITTEN = []


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    WRITTEN.append(path)


def m(name):
    """Namespaced id of this mod."""
    return name if ":" in name else f"{MOD}:{name}"


def asset(*parts):
    return os.path.join(ASSETS, *parts)


def data(namespace, *parts):
    return os.path.join(DATA, namespace, *parts)


# =====================================================================================================
# Blockstates & models
# =====================================================================================================

def blockstate(name, obj):
    write(asset("blockstates", f"{name}.json"), obj)


def block_model(name, obj):
    write(asset("models", "block", f"{name}.json"), obj)


def item_model(name, obj):
    write(asset("models", "item", f"{name}.json"), obj)


def simple_state(name, model=None):
    blockstate(name, {"variants": {"": {"model": m(f"block/{model or name}")}}})


def item_from_block(name, model=None):
    item_model(name, {"parent": m(f"block/{model or name}")})


def item_generated(name, texture=None, parent="minecraft:item/generated"):
    item_model(name, {"parent": parent, "textures": {"layer0": m(texture or f"item/{name}")}})


def cube_all(name, texture=None):
    block_model(name, {"parent": "minecraft:block/cube_all", "textures": {"all": m(f"block/{texture or name}")}})
    simple_state(name)
    item_from_block(name)


def stairs(name, texture):
    t = m(f"block/{texture}")
    tex = {"bottom": t, "top": t, "side": t}
    block_model(name, {"parent": "minecraft:block/stairs", "textures": tex})
    block_model(f"{name}_inner", {"parent": "minecraft:block/inner_stairs", "textures": tex})
    block_model(f"{name}_outer", {"parent": "minecraft:block/outer_stairs", "textures": tex})
    base_y = {"east": 0, "south": 90, "west": 180, "north": 270}
    variants = {}
    for facing, y0 in base_y.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                model = name if shape == "straight" else f"{name}_{shape.split('_')[0]}"
                left = shape.endswith("left")
                right = shape.endswith("right")
                if half == "bottom":
                    y = (y0 - 90) % 360 if left else y0
                    x = 0
                else:
                    y = (y0 + 90) % 360 if right else y0
                    x = 180
                v = {"model": m(f"block/{model}")}
                if x:
                    v["x"] = x
                if y:
                    v["y"] = y
                if x or y:
                    v["uvlock"] = True
                variants[f"facing={facing},half={half},shape={shape}"] = v
    blockstate(name, {"variants": variants})
    item_from_block(name)


def slab(name, texture, full_block):
    t = m(f"block/{texture}")
    tex = {"bottom": t, "top": t, "side": t}
    block_model(name, {"parent": "minecraft:block/slab", "textures": tex})
    block_model(f"{name}_top", {"parent": "minecraft:block/slab_top", "textures": tex})
    blockstate(name, {"variants": {
        "type=bottom": {"model": m(f"block/{name}")},
        "type=top": {"model": m(f"block/{name}_top")},
        "type=double": {"model": m(f"block/{full_block}")},
    }})
    item_from_block(name)


def wall(name, texture):
    t = {"wall": m(f"block/{texture}")}
    block_model(f"{name}_post", {"parent": "minecraft:block/template_wall_post", "textures": t})
    block_model(f"{name}_side", {"parent": "minecraft:block/template_wall_side", "textures": t})
    block_model(f"{name}_side_tall", {"parent": "minecraft:block/template_wall_side_tall", "textures": t})
    block_model(f"{name}_inventory", {"parent": "minecraft:block/wall_inventory", "textures": t})
    parts = [{"when": {"up": "true"}, "apply": {"model": m(f"block/{name}_post")}}]
    for height, suffix in (("low", "side"), ("tall", "side_tall")):
        for direction, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            apply = {"model": m(f"block/{name}_{suffix}"), "uvlock": True}
            if y:
                apply["y"] = y
            parts.append({"when": {direction: height}, "apply": apply})
    blockstate(name, {"multipart": parts})
    item_from_block(name, f"{name}_inventory")


def cross(name, texture=None, item_texture=None):
    block_model(name, {"parent": "minecraft:block/cross", "textures": {"cross": m(f"block/{texture or name}")}})
    simple_state(name)
    item_generated(name, item_texture or f"block/{texture or name}")


def horizontal_variants(models_by_key):
    """models_by_key: {extra_props_string: model}. Rotates a north-facing model."""
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for extra, model in models_by_key.items():
            key = f"facing={facing}" + (f",{extra}" if extra else "")
            v = {"model": m(f"block/{model}")}
            if y:
                v["y"] = y
            variants[key] = v
    return variants


def gen_models():
    # Building blocks
    cube_all("ice_bricks")
    stairs("ice_brick_stairs", "ice_bricks")
    slab("ice_brick_slab", "ice_bricks", "ice_bricks")
    wall("ice_brick_wall", "ice_bricks")
    block_model("chiseled_ice_bricks", {"parent": "minecraft:block/cube_column", "textures": {
        "end": m("block/chiseled_ice_bricks_top"), "side": m("block/chiseled_ice_bricks")}})
    simple_state("chiseled_ice_bricks")
    item_from_block("chiseled_ice_bricks")

    cube_all("snow_bricks")
    stairs("snow_brick_stairs", "snow_bricks")
    slab("snow_brick_slab", "snow_bricks", "snow_bricks")
    wall("snow_brick_wall", "snow_bricks")

    cube_all("frosted_glass")
    cube_all("frost_crystal_ore")
    cube_all("deepslate_frost_crystal_ore")
    cube_all("frost_crystal_block")
    cube_all("yeti_fur_block")
    block_model("snowy_spruce_leaves", {"parent": "minecraft:block/cube_all",
                                        "textures": {"all": m("block/snowy_spruce_leaves")}})
    simple_state("snowy_spruce_leaves")
    item_from_block("snowy_spruce_leaves")

    block_model("yeti_fur_carpet", {"parent": "minecraft:block/carpet", "textures": {"wool": m("block/yeti_fur_block")}})
    simple_state("yeti_fur_carpet")
    item_from_block("yeti_fur_carpet")

    cross("festive_spruce_sapling")
    # 3D models made in Blockbench (blockbench/*.bbmodel -> tools/blockbench_export.js)
    simple_state("icicle")
    item_generated("icicle", "item/icicle")
    simple_state("star")
    item_generated("star", "block/star")

    # Garland: a flat plane on the wall, like a ladder.
    block_model("garland", {
        "ambientocclusion": False,
        "textures": {"particle": m("block/garland"), "texture": m("block/garland")},
        "elements": [{
            "from": [0, 0, 15.2], "to": [16, 16, 15.2], "shade": False,
            "faces": {
                "north": {"uv": [0, 0, 16, 16], "texture": "#texture"},
                "south": {"uv": [16, 0, 0, 16], "texture": "#texture"},
            },
        }],
    })
    blockstate("garland", {"variants": horizontal_variants({"": "garland"})})
    item_generated("garland", "block/garland")

    # Baubles and gifts: the template models are made in Blockbench, the colours are child models.
    for color in ("red", "gold", "blue"):
        name = f"{color}_bauble"
        block_model(name, {"parent": m("block/template_bauble"),
                            "textures": {"bauble": m(f"block/{name}"), "particle": m(f"block/{name}")}})
        simple_state(name)
        item_generated(name, f"item/{name}")

    for color in ("red", "green", "blue"):
        name = f"{color}_gift"
        block_model(name, {"parent": m("block/template_gift"), "textures": {
            "side": m(f"block/{name}_side"), "top": m(f"block/{name}_top"), "particle": m(f"block/{name}_side")}})
        simple_state(name)
        item_from_block(name)

    # Fireplace: models fireplace / fireplace_on are made in Blockbench
    blockstate("fireplace", {"variants": horizontal_variants({"lit=false": "fireplace", "lit=true": "fireplace_on"})})
    item_from_block("fireplace")

    # Plain items
    for name in ("frost_crystal", "yeti_fur", "hot_chocolate", "sbiten", "gingerbread_cookie", "mandarin",
                 "olivier_salad", "raw_pelmeni", "pelmeni", "ice_ball", "thermometer",
                 "ushanka", "sweater", "quilted_pants", "valenki", "ice_skates"):
        item_generated(name)
    for name in ("frost_sword", "frost_pickaxe", "frost_axe", "frost_shovel", "frost_hoe", "snow_shovel",
                 "frost_staff", "candy_cane"):
        item_generated(name, parent="minecraft:item/handheld")
    for name in ("yeti_spawn_egg", "ded_moroz_spawn_egg"):
        item_model(name, {"parent": "minecraft:item/template_spawn_egg"})


# =====================================================================================================
# Translations
# =====================================================================================================

NAMES = {
    # id: (english, russian)
    "frost_crystal": ("Frost Crystal", "Морозный кристалл"),
    "yeti_fur": ("Yeti Fur", "Мех йети"),
    "hot_chocolate": ("Hot Chocolate", "Горячий шоколад"),
    "sbiten": ("Sbiten", "Сбитень"),
    "gingerbread_cookie": ("Gingerbread Cookie", "Имбирный пряник"),
    "candy_cane": ("Candy Cane", "Леденцовая тросточка"),
    "mandarin": ("Mandarin", "Мандарин"),
    "olivier_salad": ("Olivier Salad", "Салат оливье"),
    "raw_pelmeni": ("Raw Pelmeni", "Сырые пельмени"),
    "pelmeni": ("Pelmeni", "Пельмени"),
    "frost_sword": ("Frost Sword", "Морозный меч"),
    "frost_pickaxe": ("Frost Pickaxe", "Морозная кирка"),
    "frost_axe": ("Frost Axe", "Морозный топор"),
    "frost_shovel": ("Frost Shovel", "Морозная лопата"),
    "frost_hoe": ("Frost Hoe", "Морозная мотыга"),
    "snow_shovel": ("Snow Shovel", "Снеговая лопата"),
    "frost_staff": ("Staff of Father Frost", "Посох Деда Мороза"),
    "ice_ball": ("Ice Ball", "Ледышка"),
    "thermometer": ("Thermometer", "Термометр"),
    "ushanka": ("Ushanka", "Шапка-ушанка"),
    "sweater": ("Knitted Sweater", "Вязаный свитер"),
    "quilted_pants": ("Quilted Pants", "Ватные штаны"),
    "valenki": ("Valenki", "Валенки"),
    "ice_skates": ("Ice Skates", "Коньки"),
    "yeti_spawn_egg": ("Yeti Spawn Egg", "Яйцо призыва йети"),
    "ded_moroz_spawn_egg": ("Father Frost Spawn Egg", "Яйцо призыва Деда Мороза"),
    # blocks
    "ice_bricks": ("Ice Bricks", "Ледяные кирпичи"),
    "ice_brick_stairs": ("Ice Brick Stairs", "Ступеньки из ледяного кирпича"),
    "ice_brick_slab": ("Ice Brick Slab", "Плита из ледяного кирпича"),
    "ice_brick_wall": ("Ice Brick Wall", "Стена из ледяного кирпича"),
    "chiseled_ice_bricks": ("Chiseled Ice Bricks", "Резные ледяные кирпичи"),
    "snow_bricks": ("Snow Bricks", "Снежные кирпичи"),
    "snow_brick_stairs": ("Snow Brick Stairs", "Ступеньки из снежного кирпича"),
    "snow_brick_slab": ("Snow Brick Slab", "Плита из снежного кирпича"),
    "snow_brick_wall": ("Snow Brick Wall", "Стена из снежного кирпича"),
    "frosted_glass": ("Frosted Glass", "Морозное стекло"),
    "frost_crystal_ore": ("Frost Crystal Ore", "Руда морозного кристалла"),
    "deepslate_frost_crystal_ore": ("Deepslate Frost Crystal Ore", "Глубинносланцевая руда морозного кристалла"),
    "frost_crystal_block": ("Block of Frost Crystal", "Блок морозного кристалла"),
    "icicle": ("Icicle", "Сосулька"),
    "snowy_spruce_leaves": ("Snowy Spruce Leaves", "Заснеженная хвоя"),
    "festive_spruce_sapling": ("Festive Spruce Sapling", "Саженец новогодней ёлки"),
    "garland": ("Garland", "Гирлянда"),
    "red_bauble": ("Red Bauble", "Красный ёлочный шар"),
    "gold_bauble": ("Gold Bauble", "Золотой ёлочный шар"),
    "blue_bauble": ("Blue Bauble", "Синий ёлочный шар"),
    "star": ("Christmas Star", "Новогодняя звезда"),
    "red_gift": ("Red Gift", "Красный подарок"),
    "green_gift": ("Green Gift", "Зелёный подарок"),
    "blue_gift": ("Blue Gift", "Синий подарок"),
    "fireplace": ("Fireplace", "Камин"),
    "yeti_fur_block": ("Yeti Fur Block", "Блок меха йети"),
    "yeti_fur_carpet": ("Yeti Fur Carpet", "Ковёр из меха йети"),
}
BLOCKS = {
    "ice_bricks", "ice_brick_stairs", "ice_brick_slab", "ice_brick_wall", "chiseled_ice_bricks",
    "snow_bricks", "snow_brick_stairs", "snow_brick_slab", "snow_brick_wall", "frosted_glass",
    "frost_crystal_ore", "deepslate_frost_crystal_ore", "frost_crystal_block", "icicle", "snowy_spruce_leaves",
    "festive_spruce_sapling", "garland", "red_bauble", "gold_bauble", "blue_bauble", "star",
    "red_gift", "green_gift", "blue_gift", "fireplace", "yeti_fur_block", "yeti_fur_carpet",
}

# Tooltip lines shown under the item name (see WinterTooltips on the client).
TOOLTIPS = {
    "frost_crystal": [("Found deep under snowy biomes", "Встречается под снежными биомами")],
    "yeti_fur": [("Warm and fluffy. Dropped by yetis", "Тёплый и пушистый. Выпадает из йети")],
    "hot_chocolate": [("Gives Warmth for 3 minutes", "Даёт «Тепло» на 3 минуты")],
    "sbiten": [("Hot honey drink: Warmth and Regeneration", "Горячий медовый напиток: «Тепло» и регенерация")],
    "candy_cane": [("Sugar rush: a short burst of speed", "Сахарный заряд: ненадолго ускоряет")],
    "olivier_salad": [("The main dish of the New Year's table", "Главное блюдо новогоднего стола")],
    "pelmeni": [("Hearty and warming", "Сытные и согревающие")],
    "frost_sword": [("Freezes and slows its victims", "Замораживает и замедляет врагов")],
    "snow_shovel": [("Clears snow in a 3x3 area", "Расчищает снег площадью 3x3"),
                    ("Sneak to dig a single block", "Присядьте, чтобы копать по одному блоку")],
    "frost_staff": [("Fires frost bolts that freeze enemies", "Стреляет ледяными зарядами, замораживая врагов"),
                    ("Turns water into ice", "Превращает воду в лёд")],
    "ice_ball": [("Hurts more than a snowball", "Бьёт больнее снежка")],
    "thermometer": [("Shows the temperature and your insulation", "Показывает температуру и утепление")],
    "ushanka": [("Winter clothing: +2 insulation", "Зимняя одежда: +2 к утеплению")],
    "sweater": [("Winter clothing: +2 insulation", "Зимняя одежда: +2 к утеплению")],
    "quilted_pants": [("Winter clothing: +2 insulation", "Зимняя одежда: +2 к утеплению")],
    "valenki": [("Winter clothing: +2 insulation", "Зимняя одежда: +2 к утеплению")],
    "ice_skates": [("Much faster on ice", "Очень быстро по льду")],
    "red_gift": [("Right-click to unwrap", "ПКМ — развернуть")],
    "green_gift": [("Right-click to unwrap", "ПКМ — развернуть")],
    "blue_gift": [("Right-click to unwrap", "ПКМ — развернуть")],
    "fireplace": [("Light with flint and steel, put out with a shovel", "Разжигается огнивом, тушится лопатой"),
                  ("Keeps you warm in the cold", "Согревает в мороз")],
    "festive_spruce_sapling": [("Grows into a decorated Christmas tree", "Вырастает в наряженную ёлку")],
    "icicle": [("Hangs from ceilings, melts in warmth", "Свисает с потолка, тает в тепле")],
}

EXTRA = {
    "itemGroup.winter": ("Winter", "Зима"),
    "entity.winter.yeti": ("Yeti", "Йети"),
    "entity.winter.ded_moroz": ("Father Frost", "Дед Мороз"),
    "entity.winter.ice_ball": ("Ice Ball", "Ледышка"),
    "effect.winter.warmth": ("Warmth", "Тепло"),
    "enchantment.winter.insulation": ("Insulation", "Утепление"),
    "enchantment.winter.frost_aspect": ("Frost Aspect", "Ледяное касание"),
    "gamerule.winterFreezing": ("Winter: freezing in cold biomes", "Зима: замерзание в холодных биомах"),
    "gamerule.winterFreezing.description": (
        "Players without warm clothes slowly freeze in snowy biomes",
        "Игроки без тёплой одежды постепенно замерзают в снежных биомах"),
    "gamerule.winterFreezingDamage": ("Winter: freezing deals damage", "Зима: замерзание наносит урон"),
    "gamerule.winterFreezingDamage.description": (
        "Whether fully frozen players take damage from the cold",
        "Получают ли полностью замёрзшие игроки урон от холода"),
    "item.winter.thermometer.reading": ("Temperature: %s  Insulation: %s  %s", "Температура: %s  Утепление: %s  %s"),
    "winter.cold.status.warmth": ("You feel warm inside", "Вам тепло изнутри"),
    "winter.cold.status.near_fire": ("Warming up by the fire", "Греетесь у огня"),
    "winter.cold.status.comfortable": ("Comfortable", "Комфортно"),
    "winter.cold.status.dressed": ("Dressed for the weather", "Одеты по погоде"),
    "winter.cold.status.freezing": ("You are freezing!", "Вы мёрзнете!"),
    "winter.cold.status.frozen": ("You are frozen to the bone!", "Вы промёрзли до костей!"),
    "tag.item.winter.warm_fur": ("Warm Fur", "Тёплый мех"),
    "tag.item.winter.insulation.strong": ("Warm Clothing", "Тёплая одежда"),
    "tag.item.winter.insulation.light": ("Light Insulation", "Лёгкое утепление"),
}

ADVANCEMENTS_TEXT = {
    "root": (("Winter", "Зима"),
             ("Winter has come! Beware of the cold", "Зима пришла! Берегитесь холода")),
    "warm_clothes": (("Dressed for the Weather", "Одет по погоде"),
                     ("Wear an ushanka, a sweater, quilted pants and valenki", "Наденьте ушанку, свитер, ватные штаны и валенки")),
    "frost_crystal": (("Cold as Ice", "Холоднее льда"),
                      ("Mine a frost crystal", "Добудьте морозный кристалл")),
    "frost_sword": (("Frostbite", "Обморожение"),
                    ("Craft a frost sword", "Создайте морозный меч")),
    "open_gift": (("A New Year Miracle", "Новогоднее чудо"),
                  ("Unwrap a gift", "Разверните подарок")),
    "kill_yeti": (("Bigfoot Hunter", "Охотник на снежного человека"),
                  ("Defeat a yeti", "Победите йети")),
    "trade_ded_moroz": (("Grandfather Frost Has Come", "Дедушка Мороз пришёл"),
                        ("Trade with Father Frost", "Поторгуйте с Дедом Морозом")),
    "frost_staff": (("Magic Staff", "Волшебный посох"),
                    ("Obtain the staff of Father Frost", "Заполучите посох Деда Мороза")),
    "olivier": (("New Year's Table", "Новогодний стол"),
                ("Eat a bowl of Olivier salad", "Съешьте салат оливье")),
    "skating": (("Slippery Slope", "Скользкая дорожка"),
                ("Ride on ice in ice skates", "Прокатитесь по льду на коньках")),
    "star_on_top": (("The Star on Top", "Звезда на макушке"),
                    ("Place a Christmas star", "Поставьте новогоднюю звезду")),
    "hot_drink": (("Warm Up Inside", "Согреться изнутри"),
                  ("Drink hot chocolate or sbiten", "Выпейте горячий шоколад или сбитень")),
}


def gen_lang():
    en, ru = {}, {}
    for ident, (e, r) in NAMES.items():
        key = f"block.{MOD}.{ident}" if ident in BLOCKS else f"item.{MOD}.{ident}"
        en[key], ru[key] = e, r
    for ident, lines in TOOLTIPS.items():
        prefix = f"block.{MOD}.{ident}" if ident in BLOCKS else f"item.{MOD}.{ident}"
        for i, (e, r) in enumerate(lines):
            en[f"{prefix}.tooltip.{i}"], ru[f"{prefix}.tooltip.{i}"] = e, r
    for key, (e, r) in EXTRA.items():
        en[key], ru[key] = e, r
    for adv, ((te, tr), (de, dr)) in ADVANCEMENTS_TEXT.items():
        en[f"advancements.{MOD}.{adv}.title"], ru[f"advancements.{MOD}.{adv}.title"] = te, tr
        en[f"advancements.{MOD}.{adv}.description"], ru[f"advancements.{MOD}.{adv}.description"] = de, dr
    write(asset("lang", "en_us.json"), dict(sorted(en.items())))
    write(asset("lang", "ru_ru.json"), dict(sorted(ru.items())))


# =====================================================================================================
# Recipes
# =====================================================================================================

def ing(x):
    if x.startswith("#"):
        return {"tag": x[1:] if ":" in x else f"minecraft:{x[1:]}"}
    return {"item": x if ":" in x else f"minecraft:{x}"}


def recipe(name, obj):
    write(data(MOD, "recipe", f"{name}.json"), obj)


def shaped(name, pattern, key, result, count=1, category="misc"):
    recipe(name, {
        "type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
        "key": {k: ing(v) for k, v in key.items()},
        "result": {"id": m(result) if ":" not in result else result, "count": count},
    })


def shapeless(name, ingredients, result, count=1, category="misc"):
    recipe(name, {
        "type": "minecraft:crafting_shapeless", "category": category,
        "ingredients": [ing(i) for i in ingredients],
        "result": {"id": m(result) if ":" not in result else result, "count": count},
    })


def cooking(name, kind, ingredient, result, xp, time, category):
    recipe(name, {
        "type": f"minecraft:{kind}", "category": category, "ingredient": ing(ingredient),
        "result": {"id": m(result)}, "experience": xp, "cookingtime": time,
    })


def stonecutting(name, ingredient, result, count=1):
    recipe(name, {"type": "minecraft:stonecutting", "ingredient": ing(ingredient),
                  "result": {"id": m(result), "count": count}})


def gen_recipes():
    W = m  # shorthand for our items
    # Building blocks
    shaped("ice_bricks", ["##", "##"], {"#": "packed_ice"}, "ice_bricks", 4, "building")
    shaped("snow_bricks", ["##", "##"], {"#": "snow_block"}, "snow_bricks", 4, "building")
    for base, prefix in (("ice_bricks", "ice_brick"), ("snow_bricks", "snow_brick")):
        shaped(f"{prefix}_stairs", ["#  ", "## ", "###"], {"#": W(base)}, f"{prefix}_stairs", 4, "building")
        shaped(f"{prefix}_slab", ["###"], {"#": W(base)}, f"{prefix}_slab", 6, "building")
        shaped(f"{prefix}_wall", ["###", "###"], {"#": W(base)}, f"{prefix}_wall", 6, "building")
        stonecutting(f"{prefix}_stairs_from_{base}_stonecutting", W(base), f"{prefix}_stairs")
        stonecutting(f"{prefix}_slab_from_{base}_stonecutting", W(base), f"{prefix}_slab", 2)
        stonecutting(f"{prefix}_wall_from_{base}_stonecutting", W(base), f"{prefix}_wall")
    shaped("chiseled_ice_bricks", ["#", "#"], {"#": W("ice_brick_slab")}, "chiseled_ice_bricks", 1, "building")
    stonecutting("chiseled_ice_bricks_from_ice_bricks_stonecutting", W("ice_bricks"), "chiseled_ice_bricks")
    stonecutting("ice_bricks_from_packed_ice_stonecutting", "packed_ice", "ice_bricks")
    stonecutting("snow_bricks_from_snow_block_stonecutting", "snow_block", "snow_bricks")
    shaped("frosted_glass", ["###", "#S#", "###"], {"#": "glass", "S": "snowball"}, "frosted_glass", 8, "building")
    shaped("frost_crystal_block", ["###", "###", "###"], {"#": W("frost_crystal")}, "frost_crystal_block", 1, "building")
    shapeless("frost_crystal_from_block", [W("frost_crystal_block")], "frost_crystal", 9)
    shaped("snowy_spruce_leaves", ["###", "#S#", "###"], {"#": "spruce_leaves", "S": "snowball"},
           "snowy_spruce_leaves", 8, "building")
    shaped("yeti_fur_block", ["##", "##"], {"#": W("yeti_fur")}, "yeti_fur_block", 1, "building")
    shaped("yeti_fur_carpet", ["##"], {"#": W("yeti_fur_block")}, "yeti_fur_carpet", 3, "building")
    shaped("icicle", ["#", "#"], {"#": "ice"}, "icicle", 4, "building")
    shaped("fireplace", ["BBB", "BCB", "SSS"], {"B": "bricks", "C": "campfire", "S": "stone_bricks"},
           "fireplace", 1, "building")

    # Ore smelting
    for ore in ("frost_crystal_ore", "deepslate_frost_crystal_ore"):
        cooking(f"frost_crystal_from_smelting_{ore}", "smelting", W(ore), "frost_crystal", 1.0, 200, "misc")
        cooking(f"frost_crystal_from_blasting_{ore}", "blasting", W(ore), "frost_crystal", 1.0, 100, "misc")

    # Tools
    C, S = W("frost_crystal"), "stick"
    shaped("frost_sword", ["C", "C", "S"], {"C": C, "S": S}, "frost_sword", 1, "equipment")
    shaped("frost_pickaxe", ["CCC", " S ", " S "], {"C": C, "S": S}, "frost_pickaxe", 1, "equipment")
    shaped("frost_axe", ["CC", "CS", " S"], {"C": C, "S": S}, "frost_axe", 1, "equipment")
    shaped("frost_shovel", ["C", "S", "S"], {"C": C, "S": S}, "frost_shovel", 1, "equipment")
    shaped("frost_hoe", ["CC", " S", " S"], {"C": C, "S": S}, "frost_hoe", 1, "equipment")
    shaped("snow_shovel", ["III", "ISI", " S "], {"I": "iron_ingot", "S": S}, "snow_shovel", 1, "equipment")
    shaped("frost_staff", ["  F", " D ", "B  "], {"F": W("frost_crystal_block"), "D": "diamond", "B": "blaze_rod"},
           "frost_staff", 1, "equipment")
    shapeless("ice_ball", ["snowball", "snowball", "snowball", "snowball", "ice"], "ice_ball", 4, "equipment")
    shaped("thermometer", ["G", "R", "G"], {"G": "glass", "R": "redstone"}, "thermometer", 1, "equipment")

    # Clothing
    F = "#winter:warm_fur"
    shaped("ushanka", ["LFL", "F F"], {"L": "leather", "F": F}, "ushanka", 1, "equipment")
    shaped("sweater", ["W W", "WWW", "WWW"], {"W": "#wool"}, "sweater", 1, "equipment")
    shaped("quilted_pants", ["WLW", "W W", "W W"], {"W": "#wool", "L": "leather"}, "quilted_pants", 1, "equipment")
    shaped("valenki", ["W W", "W W"], {"W": "#wool"}, "valenki", 1, "equipment")
    shaped("ice_skates", ["L L", "L L", "I I"], {"L": "leather", "I": "iron_ingot"}, "ice_skates", 1, "equipment")

    # Food
    shapeless("hot_chocolate", ["milk_bucket", "cocoa_beans", "cocoa_beans", "sugar",
                                "glass_bottle", "glass_bottle", "glass_bottle"], "hot_chocolate", 3, "misc")
    shapeless("sbiten", ["honey_bottle", "sweet_berries", "sweet_berries", "sugar"], "sbiten", 1, "misc")
    shapeless("gingerbread_cookie", ["wheat", "wheat", "sugar", "honeycomb"], "gingerbread_cookie", 6, "misc")
    shapeless("candy_cane", ["sugar", "sugar", "red_dye"], "candy_cane", 3, "misc")
    shapeless("olivier_salad", ["bowl", "baked_potato", "carrot", "egg", "cooked_chicken"], "olivier_salad", 1, "misc")
    shapeless("raw_pelmeni_from_beef", ["wheat", "wheat", "egg", "beef"], "raw_pelmeni", 4, "misc")
    shapeless("raw_pelmeni_from_porkchop", ["wheat", "wheat", "egg", "porkchop"], "raw_pelmeni", 4, "misc")
    cooking("pelmeni", "smelting", W("raw_pelmeni"), "pelmeni", 0.35, 200, "food")
    cooking("pelmeni_from_smoking", "smoking", W("raw_pelmeni"), "pelmeni", 0.35, 100, "food")
    cooking("pelmeni_from_campfire_cooking", "campfire_cooking", W("raw_pelmeni"), "pelmeni", 0.35, 600, "food")

    # Decorations
    shaped("garland", ["RGB", "SSS"], {"R": "red_dye", "G": "glowstone_dust", "B": "blue_dye", "S": "string"},
           "garland", 6, "building")
    for color, dye, nugget in (("red", "red_dye", "iron_nugget"), ("gold", "yellow_dye", "gold_nugget"),
                               ("blue", "blue_dye", "iron_nugget")):
        shaped(f"{color}_bauble", [" N ", "GDG", " G "], {"N": nugget, "G": "glass", "D": dye},
               f"{color}_bauble", 4, "building")
    shaped("star", [" G ", "GDG", " G "], {"G": "gold_ingot", "D": "glowstone_dust"}, "star", 1, "building")
    for color, dye in (("red", "red_dye"), ("green", "green_dye"), ("blue", "blue_dye")):
        shaped(f"{color}_gift", ["SDS", "PEP", "PPP"], {"S": "string", "D": dye, "P": "paper", "E": "emerald"},
               f"{color}_gift", 1, "misc")
    shapeless("festive_spruce_sapling", ["spruce_sapling", "glowstone_dust", "snowball"], "festive_spruce_sapling", 1, "misc")


# =====================================================================================================
# Loot tables
# =====================================================================================================

SILK_TOUCH = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
SHEARS_OR_SILK = {"condition": "minecraft:any_of", "terms": [
    {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}}, SILK_TOUCH]}


def block_loot(name, pools):
    write(data(MOD, "loot_table", "blocks", f"{name}.json"),
          {"type": "minecraft:block", "pools": pools, "random_sequence": m(f"blocks/{name}")})


def drop_self(name):
    block_loot(name, [{"rolls": 1, "bonus_rolls": 0,
                       "entries": [{"type": "minecraft:item", "name": m(name)}],
                       "conditions": [{"condition": "minecraft:survives_explosion"}]}])


def drop_slab(name):
    block_loot(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [{
        "type": "minecraft:item", "name": m(name),
        "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False,
             "conditions": [{"condition": "minecraft:block_state_property", "block": m(name), "properties": {"type": "double"}}]},
            {"function": "minecraft:explosion_decay"},
        ]}]}])


def drop_ore(name, item):
    block_loot(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": m(name), "conditions": [SILK_TOUCH]},
        {"type": "minecraft:item", "name": m(item), "functions": [
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
            {"function": "minecraft:explosion_decay"}]},
    ]}]}])


def gen_loot():
    for name in ("ice_bricks", "ice_brick_stairs", "ice_brick_wall", "chiseled_ice_bricks",
                 "snow_bricks", "snow_brick_stairs", "snow_brick_wall", "frosted_glass", "frost_crystal_block",
                 "icicle", "festive_spruce_sapling", "garland", "red_bauble", "gold_bauble", "blue_bauble", "star",
                 "red_gift", "green_gift", "blue_gift", "fireplace", "yeti_fur_block", "yeti_fur_carpet"):
        drop_self(name)
    drop_slab("ice_brick_slab")
    drop_slab("snow_brick_slab")
    drop_ore("frost_crystal_ore", "frost_crystal")
    drop_ore("deepslate_frost_crystal_ore", "frost_crystal")

    not_shears = [{"condition": "minecraft:inverted", "term": SHEARS_OR_SILK}]
    block_loot("snowy_spruce_leaves", [
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": m("snowy_spruce_leaves"), "conditions": [SHEARS_OR_SILK]},
            {"type": "minecraft:item", "name": m("festive_spruce_sapling"), "conditions": [
                {"condition": "minecraft:survives_explosion"},
                {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                 "chances": [0.05, 0.0625, 0.083333336, 0.1]}]},
        ]}]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": not_shears, "entries": [{
            "type": "minecraft:item", "name": "minecraft:stick",
            "conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                            "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}],
            "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}, "add": False},
                          {"function": "minecraft:explosion_decay"}]}]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": not_shears, "entries": [{
            "type": "minecraft:item", "name": m("mandarin"),
            "conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                            "chances": [0.02, 0.025, 0.03, 0.04, 0.08]}]}]},
    ])

    # Entities
    write(data(MOD, "loot_table", "entities", "yeti.json"), {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": m("yeti_fur"), "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}},
            {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
             "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:snowball", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 6}}]}]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:killed_by_player"},
                                    {"condition": "minecraft:random_chance_with_enchanted_bonus",
                                     "enchantment": "minecraft:looting", "unenchanted_chance": 0.35,
                                     "enchanted_chance": {"type": "minecraft:linear", "base": 0.45, "per_level_above_first": 0.1}}],
         "entries": [{"type": "minecraft:item", "name": m("frost_crystal")}]},
    ], "random_sequence": m("entities/yeti")})
    # Father Frost only brings coal to those who misbehave.
    write(data(MOD, "loot_table", "entities", "ded_moroz.json"), {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:coal", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]}]},
    ], "random_sequence": m("entities/ded_moroz")})

    def item(name, weight, lo=1, hi=1):
        e = {"type": "minecraft:item", "name": name if ":" in name else m(name), "weight": weight}
        if hi > 1:
            e["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
        return e

    write(data(MOD, "loot_table", "gameplay", "gift.json"), {"type": "minecraft:gift", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 3}, "entries": [
            item("mandarin", 20, 2, 5), item("candy_cane", 15, 1, 3), item("gingerbread_cookie", 15, 2, 4),
            item("hot_chocolate", 8), item("sbiten", 6), item("olivier_salad", 4), item("minecraft:snowball", 6, 4, 12),
            item("minecraft:cookie", 6, 2, 6), item("minecraft:sweet_berries", 4, 2, 6),
        ]},
        {"rolls": 1, "entries": [
            item("minecraft:emerald", 20, 1, 3), item("frost_crystal", 12, 1, 2), item("ice_ball", 10, 4, 8),
            item("garland", 10, 2, 4), item("red_bauble", 6, 2, 4), item("gold_bauble", 6, 2, 4), item("blue_bauble", 6, 2, 4),
            item("star", 4), item("festive_spruce_sapling", 4), item("minecraft:iron_ingot", 10, 2, 4),
            item("minecraft:gold_ingot", 8, 1, 3), item("minecraft:diamond", 3), item("thermometer", 3),
            item("ice_skates", 2), item("minecraft:music_disc_cat", 1), item("minecraft:firework_rocket", 8, 3, 6),
            # a lump of coal for those on the naughty list
            item("minecraft:coal", 8, 1, 2),
        ]},
    ]})


# =====================================================================================================
# Tags
# =====================================================================================================

def tag(namespace, kind, name, values, replace=False):
    write(data(namespace, "tags", kind, f"{name}.json"), {"replace": replace, "values": values})


def gen_tags():
    ice_family = ["ice_bricks", "ice_brick_stairs", "ice_brick_slab", "ice_brick_wall", "chiseled_ice_bricks"]
    snow_family = ["snow_bricks", "snow_brick_stairs", "snow_brick_slab", "snow_brick_wall"]
    clothing = [m(x) for x in ("ushanka", "sweater", "quilted_pants", "valenki")]

    # Block tags
    tag("minecraft", "block", "mineable/pickaxe", [m(x) for x in ice_family] + [m(x) for x in (
        "frost_crystal_ore", "deepslate_frost_crystal_ore", "frost_crystal_block", "fireplace", "icicle")])
    tag("minecraft", "block", "mineable/shovel", [m(x) for x in snow_family])
    tag("minecraft", "block", "mineable/hoe", [m("snowy_spruce_leaves")])
    tag("minecraft", "block", "needs_iron_tool", [m("frost_crystal_ore"), m("deepslate_frost_crystal_ore")])
    tag("minecraft", "block", "stairs", [m("ice_brick_stairs"), m("snow_brick_stairs")])
    tag("minecraft", "block", "slabs", [m("ice_brick_slab"), m("snow_brick_slab")])
    tag("minecraft", "block", "walls", [m("ice_brick_wall"), m("snow_brick_wall")])
    tag("minecraft", "block", "leaves", [m("snowy_spruce_leaves")])
    tag("minecraft", "block", "saplings", [m("festive_spruce_sapling")])
    tag("minecraft", "block", "dampens_vibrations", [m("yeti_fur_block"), m("yeti_fur_carpet")])
    tag("minecraft", "block", "occludes_vibration_signals", [m("yeti_fur_block")])
    tag(MOD, "block", "heat_sources", [
        "minecraft:campfire", "minecraft:soul_campfire", "minecraft:fire", "minecraft:soul_fire", "minecraft:lava",
        "minecraft:magma_block", "minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker", m("fireplace")])
    tag(MOD, "block", "snow_shovel_clearable", ["minecraft:snow", "minecraft:snow_block", "minecraft:powder_snow"])
    tag(MOD, "block", "skating_surfaces", ["#minecraft:ice", m("ice_bricks")])

    # Item tags
    tag("minecraft", "item", "stairs", [m("ice_brick_stairs"), m("snow_brick_stairs")])
    tag("minecraft", "item", "slabs", [m("ice_brick_slab"), m("snow_brick_slab")])
    tag("minecraft", "item", "walls", [m("ice_brick_wall"), m("snow_brick_wall")])
    tag("minecraft", "item", "leaves", [m("snowy_spruce_leaves")])
    tag("minecraft", "item", "saplings", [m("festive_spruce_sapling")])
    tag("minecraft", "item", "swords", [m("frost_sword")])
    tag("minecraft", "item", "pickaxes", [m("frost_pickaxe")])
    tag("minecraft", "item", "axes", [m("frost_axe")])
    tag("minecraft", "item", "shovels", [m("frost_shovel"), m("snow_shovel")])
    tag("minecraft", "item", "hoes", [m("frost_hoe")])
    tag("minecraft", "item", "head_armor", [m("ushanka")])
    tag("minecraft", "item", "chest_armor", [m("sweater")])
    tag("minecraft", "item", "leg_armor", [m("quilted_pants")])
    tag("minecraft", "item", "foot_armor", [m("valenki"), m("ice_skates")])
    tag("minecraft", "item", "freeze_immune_wearables", clothing)
    tag("minecraft", "item", "enchantable/durability", [m("frost_staff")])
    tag(MOD, "item", "insulation/strong", clothing)
    tag(MOD, "item", "insulation/light", ["minecraft:leather_helmet", "minecraft:leather_chestplate",
                                          "minecraft:leather_leggings", "minecraft:leather_boots",
                                          m("ice_skates")])
    tag(MOD, "item", "warm_fur", ["minecraft:rabbit_hide", m("yeti_fur")])

    # Entity type tags
    tag("minecraft", "entity_type", "freeze_immune_entity_types", [m("yeti"), m("ded_moroz")])
    tag("minecraft", "entity_type", "powder_snow_walkable_mobs", [m("ded_moroz")])

    # Enchantment tags
    tag("minecraft", "enchantment", "non_treasure", [m("insulation"), m("frost_aspect")])
    tag(MOD, "enchantment", "exclusive_set/elemental_aspect", ["minecraft:fire_aspect", m("frost_aspect")])

    # Biome tags
    snowy = ["minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:snowy_taiga", "minecraft:snowy_slopes",
             "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:grove", "minecraft:frozen_river",
             "minecraft:snowy_beach", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean"]
    tag(MOD, "worldgen/biome", "has_frost_crystal_ore", snowy)
    tag(MOD, "worldgen/biome", "has_gifts", ["minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:grove",
                                             "minecraft:ice_spikes", "minecraft:snowy_slopes"])
    tag(MOD, "worldgen/biome", "has_festive_spruce", ["minecraft:snowy_taiga", "minecraft:snowy_plains", "minecraft:grove"])
    tag(MOD, "worldgen/biome", "spawns_yeti", ["minecraft:snowy_slopes", "minecraft:frozen_peaks", "minecraft:jagged_peaks",
                                               "minecraft:grove", "minecraft:ice_spikes", "minecraft:snowy_taiga",
                                               "minecraft:snowy_plains"])
    tag(MOD, "worldgen/biome", "spawns_ded_moroz", ["minecraft:snowy_plains", "minecraft:snowy_taiga",
                                                    "minecraft:grove", "minecraft:ice_spikes"])


# =====================================================================================================
# Enchantments
# =====================================================================================================

def gen_enchantments():
    write(data(MOD, "enchantment", "insulation.json"), {
        "description": {"translate": "enchantment.winter.insulation"},
        "supported_items": "#minecraft:enchantable/armor",
        "weight": 5,
        "max_level": 2,
        "min_cost": {"base": 5, "per_level_above_first": 10},
        "max_cost": {"base": 25, "per_level_above_first": 10},
        "anvil_cost": 2,
        "slots": ["armor"],
        "effects": {},
    })
    # The freezing itself is applied in code (FrostAspectHandler); this file defines availability.
    write(data(MOD, "enchantment", "frost_aspect.json"), {
        "description": {"translate": "enchantment.winter.frost_aspect"},
        "supported_items": "#minecraft:enchantable/fire_aspect",
        "primary_items": "#minecraft:enchantable/sword",
        "exclusive_set": "#winter:exclusive_set/elemental_aspect",
        "weight": 2,
        "max_level": 2,
        "min_cost": {"base": 10, "per_level_above_first": 20},
        "max_cost": {"base": 60, "per_level_above_first": 20},
        "anvil_cost": 4,
        "slots": ["mainhand"],
        "effects": {},
    })


# =====================================================================================================
# World generation
# =====================================================================================================

def feature(kind, name, obj):
    write(data(MOD, "worldgen", kind, f"{name}.json"), obj)


def gen_worldgen():
    feature("configured_feature", "frost_crystal_ore", {"type": "minecraft:ore", "config": {
        "size": 7, "discard_chance_on_air_exposure": 0.2, "targets": [
            {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"},
             "state": {"Name": m("frost_crystal_ore")}},
            {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"},
             "state": {"Name": m("deepslate_frost_crystal_ore")}},
        ]}})
    feature("placed_feature", "frost_crystal_ore", {"feature": m("frost_crystal_ore"), "placement": [
        {"type": "minecraft:count", "count": 9},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid",
                                                      "min_inclusive": {"absolute": -40}, "max_inclusive": {"absolute": 120}}},
        {"type": "minecraft:biome"},
    ]})

    gift_provider = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 1, "data": {"Name": m("red_gift")}},
        {"weight": 1, "data": {"Name": m("green_gift")}},
        {"weight": 1, "data": {"Name": m("blue_gift")}},
    ]}
    feature("configured_feature", "gift", {"type": "minecraft:simple_block", "config": {"to_place": gift_provider}})
    feature("placed_feature", "gift", {"feature": m("gift"), "placement": [
        {"type": "minecraft:rarity_filter", "chance": 40},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
            {"type": "minecraft:matching_blocks", "blocks": ["minecraft:air", "minecraft:snow"]},
            {"type": "minecraft:any_of", "predicates": [
                {"type": "minecraft:matching_block_tag", "offset": [0, -1, 0], "tag": "minecraft:dirt"},
                {"type": "minecraft:matching_blocks", "offset": [0, -1, 0],
                 "blocks": ["minecraft:snow_block", "minecraft:packed_ice", "minecraft:stone"]},
            ]},
        ]}},
        {"type": "minecraft:biome"},
    ]})

    bauble_provider = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 3, "data": {"Name": m("red_bauble")}},
        {"weight": 2, "data": {"Name": m("gold_bauble")}},
        {"weight": 2, "data": {"Name": m("blue_bauble")}},
    ]}
    feature("configured_feature", "festive_spruce", {"type": "minecraft:tree", "config": {
        "decorators": [
            {"type": "minecraft:attached_to_leaves", "probability": 0.18, "exclusion_radius_xz": 1,
             "exclusion_radius_y": 0, "required_empty_blocks": 1, "block_provider": bauble_provider,
             "directions": ["down"]},
        ],
        "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
        "foliage_placer": {"type": "minecraft:spruce_foliage_placer",
                           "offset": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 2},
                           "radius": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3},
                           "trunk_height": {"type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 2}},
        "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {
            "Name": m("snowy_spruce_leaves"), "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}},
        "force_dirt": False,
        "ignore_vines": True,
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 2, "lower_size": 0, "upper_size": 2},
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 5, "height_rand_a": 2, "height_rand_b": 1},
        "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {
            "Name": "minecraft:spruce_log", "Properties": {"axis": "y"}}},
    }})
    feature("placed_feature", "festive_spruce", {"feature": m("festive_spruce"), "placement": [
        {"type": "minecraft:rarity_filter", "chance": 20},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
        {"type": "minecraft:biome"},
        {"type": "minecraft:block_predicate_filter", "predicate": {
            "type": "minecraft:would_survive", "state": {"Name": m("festive_spruce_sapling"), "Properties": {"stage": "0"}}}},
    ]})

    feature("configured_feature", "icicles", {"type": "minecraft:block_column", "config": {
        "direction": "down",
        "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
        "prioritize_tip": False,
        "layers": [{"height": {"type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 3},
                    "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": m("icicle")}}}],
    }})
    feature("placed_feature", "icicles", {"feature": m("icicles"), "placement": [
        {"type": "minecraft:count", "count": 20},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                                      "min_inclusive": {"absolute": 0}, "max_inclusive": {"absolute": 256}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 12,
         "target_condition": {"type": "minecraft:solid"},
         "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1},
        {"type": "minecraft:biome"},
    ]})


# =====================================================================================================
# Advancements
# =====================================================================================================

def display(icon, name, frame="task", hidden=False, background=None, toast=True, announce=True):
    d = {"icon": {"id": m(icon) if ":" not in icon else icon},
         "title": {"translate": f"advancements.{MOD}.{name}.title"},
         "description": {"translate": f"advancements.{MOD}.{name}.description"},
         "frame": frame, "show_toast": toast, "announce_to_chat": announce, "hidden": hidden}
    if background:
        d["background"] = background
    return d


def advancement(name, parent, icon, criteria, frame="task", requirements=None, **kw):
    obj = {"display": display(icon, name, frame, **kw), "criteria": criteria}
    if parent:
        obj["parent"] = m(parent)
    if requirements:
        obj["requirements"] = requirements
    write(data(MOD, "advancement", f"{name}.json"), obj)


def has_items(*items):
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": [m(i) for i in items]}]}}


def gen_advancements():
    advancement("root", None, "red_gift", {"tick": {"trigger": "minecraft:tick"}},
                background="minecraft:textures/block/snow.png", toast=False, announce=False)
    advancement("frost_crystal", "root", "frost_crystal", {"get": has_items("frost_crystal")})
    advancement("frost_sword", "frost_crystal", "frost_sword", {"get": has_items("frost_sword")})
    advancement("frost_staff", "frost_sword", "frost_staff", {"get": has_items("frost_staff")}, frame="goal")
    advancement("warm_clothes", "root", "ushanka", {"wear": {"trigger": "minecraft:tick", "conditions": {"player": [
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"equipment": {
            "head": {"items": m("ushanka")}, "chest": {"items": m("sweater")},
            "legs": {"items": m("quilted_pants")}, "feet": {"items": m("valenki")}}}}]}}}, frame="goal")
    advancement("hot_drink", "root", "hot_chocolate", {
        "hot_chocolate": {"trigger": "minecraft:consume_item", "conditions": {"item": {"items": m("hot_chocolate")}}},
        "sbiten": {"trigger": "minecraft:consume_item", "conditions": {"item": {"items": m("sbiten")}}},
    }, requirements=[["hot_chocolate", "sbiten"]])
    advancement("olivier", "hot_drink", "olivier_salad", {
        "eat": {"trigger": "minecraft:consume_item", "conditions": {"item": {"items": m("olivier_salad")}}}})
    advancement("open_gift", "root", "green_gift", {"opened": {"trigger": "minecraft:impossible"}})
    advancement("star_on_top", "open_gift", "star", {"place": {"trigger": "minecraft:placed_block", "conditions": {
        "location": [{"condition": "minecraft:block_state_property", "block": m("star")}]}}})
    advancement("trade_ded_moroz", "open_gift", "ded_moroz_spawn_egg", {"trade": {
        "trigger": "minecraft:villager_trade", "conditions": {"villager": [
            {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"type": m("ded_moroz")}}]}}},
                frame="goal")
    advancement("kill_yeti", "root", "yeti_fur", {"kill": {"trigger": "minecraft:player_killed_entity", "conditions": {
        "entity": [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"type": m("yeti")}}]}}},
                frame="challenge")
    advancement("skating", "root", "ice_skates", {"skate": {"trigger": "minecraft:tick", "conditions": {"player": [
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {
            "equipment": {"feet": {"items": m("ice_skates")}},
            "stepping_on": {"block": {"blocks": "#minecraft:ice"}}}}]}}})


# Block models exported from Blockbench by tools/blockbench_export.js; this script must not touch them.
BLOCKBENCH_MODELS = {"fireplace", "fireplace_on", "template_gift", "template_bauble", "icicle", "star"}


def main():
    for sub in (os.path.join(ROOT, "data"), asset("blockstates"), asset("models", "item"), asset("lang")):
        if os.path.isdir(sub):
            shutil.rmtree(sub)
    block_models = asset("models", "block")
    if os.path.isdir(block_models):
        for f in os.listdir(block_models):
            if f[:-len(".json")] not in BLOCKBENCH_MODELS:
                os.remove(os.path.join(block_models, f))
    gen_models()
    gen_lang()
    gen_recipes()
    gen_loot()
    gen_tags()
    gen_enchantments()
    gen_worldgen()
    gen_advancements()
    print(f"Wrote {len(WRITTEN)} files")


if __name__ == "__main__":
    main()
