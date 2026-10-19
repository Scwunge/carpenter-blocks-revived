"""The Carpenter's Bed. Its frame and fabric are built in Java (BedFrames) at render time, so the block model here only
gives the particle texture and a stand-in shape; as in the original, the item is a flat bed icon."""

from .common import ASSETS, AXE_MINEABLE, DATA, MOD, shaped_recipe, write
from .redstone import boxes_model

SOLID = f"{MOD}:block/general/solid"


def generate() -> None:
    name = "carpenters_bed"
    write(ASSETS / f"models/block/{name}.json", boxes_model([((0, 0, 0, 16, 10, 16), SOLID)], SOLID))
    write(ASSETS / f"models/item/{name}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}})
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    AXE_MINEABLE.append(f"{MOD}:{name}")

    # Only the head drops the bed, as vanilla's beds
    write(DATA / f"{MOD}/loot_table/blocks/{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": f"{MOD}:{name}",
                "conditions": [{
                    "condition": "minecraft:block_state_property",
                    "block": f"{MOD}:{name}",
                    "properties": {"part": "head"},
                }],
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    })

    # Wool over frames, as the original
    shaped_recipe(name, "misc", ["XXX", "YYY"],
                  {"X": {"tag": "minecraft:wool"}, "Y": {"item": f"{MOD}:carpenters_block"}}, 1)
