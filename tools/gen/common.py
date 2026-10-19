"""Shared helpers for the resource generators. Each family module adds its files and lang strings through these."""

import json
from pathlib import Path

from .config_options import recipe_quantity_option

ROOT = Path("common/src/main/resources")
ASSETS = ROOT / "assets/carpentersblocks"
DATA = ROOT / "data"
MOD = "carpentersblocks"

# en_us strings collected from every module, written once at the end
LANG = {}

# Block ids for the shared tags, collected from every module
AXE_MINEABLE = []


def write(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8", newline="\n")


def block_loot(name: str) -> None:
    """A loot table that drops the block itself, unless destroyed by an explosion."""
    write(DATA / f"{MOD}/loot_table/blocks/{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"{MOD}:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    })


def handheld_item(name: str, texture: str) -> None:
    write(ASSETS / f"models/item/{name}.json",
          {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/{texture}"}})


def shaped_recipe(name: str, category: str, pattern, key, count: int, result: str = None) -> None:
    """A shaped recipe. When the result has a recipeQuantity* config option, the recipe takes its count from it
    (carpentersblocks:configured_shaped); count is then the option's default, which RecipeQuantityTest checks."""
    recipe = {
        "type": "minecraft:crafting_shaped",
        "category": category,
        "pattern": pattern,
        "key": key,
        "result": {"id": f"{MOD}:{result or name}", "count": count},
    }
    option = recipe_quantity_option(result or name)
    if option:
        recipe["type"] = f"{MOD}:configured_shaped"
        recipe["quantity_option"] = option
    write(DATA / f"{MOD}/recipe/{name}.json", recipe)
