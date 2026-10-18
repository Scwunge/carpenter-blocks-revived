"""Generates the JSON assets and data for the Carpenter's Block. Run from the repository root."""

import json
from pathlib import Path

ROOT = Path("common/src/main/resources")
ASSETS = ROOT / "assets/carpentersblocks"
DATA = ROOT / "data"
MOD = "carpentersblocks"

# Shape name -> element bounds (from, to), matching FrameShape
SHAPES = {
    "full": ([0, 0, 0], [16, 16, 16]),
    "down": ([0, 0, 0], [16, 8, 16]),
    "up": ([0, 8, 0], [16, 16, 16]),
    "north": ([0, 0, 0], [16, 16, 8]),
    "south": ([0, 0, 8], [16, 16, 16]),
    "west": ([0, 0, 0], [8, 16, 16]),
    "east": ([8, 0, 0], [16, 16, 16]),
}

# Face -> (axis index, boundary value) where that face touches the block edge
FACE_BOUNDARY = {
    "down": (1, 0, "from"),
    "up": (1, 16, "to"),
    "north": (2, 0, "from"),
    "south": (2, 16, "to"),
    "west": (0, 0, "from"),
    "east": (0, 16, "to"),
}


def write(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def block_model(bounds):
    lo, hi = bounds
    faces = {}
    for face, (axis, value, end) in FACE_BOUNDARY.items():
        entry = {"texture": "#frame"}
        coord = lo[axis] if end == "from" else hi[axis]
        if coord == value:
            entry["cullface"] = face
        faces[face] = entry
    # UVs are left out so they follow the element's position, keeping slab textures aligned with full blocks
    return {
        "parent": "minecraft:block/block",
        "textures": {"frame": f"{MOD}:block/frame", "particle": f"{MOD}:block/frame"},
        "elements": [{"from": lo, "to": hi, "faces": faces}],
    }


def main() -> None:
    variants = {}
    for name, bounds in SHAPES.items():
        model_name = "carpenters_block" if name == "full" else f"carpenters_block_{name}"
        write(ASSETS / f"models/block/{model_name}.json", block_model(bounds))
        variants[f"shape={name}"] = {"model": f"{MOD}:block/{model_name}"}
    write(ASSETS / "blockstates/carpenters_block.json", {"variants": variants})

    write(ASSETS / "models/item/carpenters_block.json", {"parent": f"{MOD}:block/carpenters_block"})
    write(ASSETS / "models/item/carpenters_hammer.json",
          {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/carpenters_hammer"}})

    write(ASSETS / "lang/en_us.json", {
        "block.carpentersblocks.carpenters_block": "Carpenter's Block",
        "item.carpentersblocks.carpenters_hammer": "Carpenter's Hammer",
        "itemGroup.carpentersblocks": "Carpenter's Blocks",
    })

    write(DATA / f"{MOD}/loot_table/blocks/carpenters_block.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"{MOD}:carpenters_block"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/carpenters_block",
    })

    # Recipes from the original mod: 8 sticks around planks make 5 blocks; the hammer uses iron and a block
    write(DATA / f"{MOD}/recipe/carpenters_block.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "pattern": ["XXX", "XYX", "XXX"],
        "key": {"X": {"item": "minecraft:stick"}, "Y": {"tag": "minecraft:planks"}},
        "result": {"id": f"{MOD}:carpenters_block", "count": 5},
    })
    write(DATA / f"{MOD}/recipe/carpenters_hammer.json", {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["XX ", " YX", " Y "],
        "key": {"X": {"item": "minecraft:iron_ingot"}, "Y": {"item": f"{MOD}:carpenters_block"}},
        "result": {"id": f"{MOD}:carpenters_hammer", "count": 1},
    })

    write(DATA / "minecraft/tags/block/mineable/axe.json", {"values": [f"{MOD}:carpenters_block"]})


if __name__ == "__main__":
    main()
