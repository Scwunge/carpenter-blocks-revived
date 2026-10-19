"""The door family: Door, Hatch and Garage Door. Their frames are built in Java (DoorFrames) at render time, so the
block models here only give the particle texture and a stand-in shape. As the original, the door shows as a flat icon
in inventories; the hatch and garage door item models follow the original's inventory renders."""

from .common import ASSETS, AXE_MINEABLE, DATA, MOD, shaped_recipe, write
from .redstone import boxes_model

SOLID = f"{MOD}:block/general/solid"
IRON = "minecraft:block/iron_block"

DOOR_BLOCK = [((0, 0, 0, 16, 16, 3), SOLID)]
HATCH_BLOCK = [((0, 0, 0, 16, 3, 16), SOLID)]
HATCH_ITEM = [
    ((0, 7, 0, 16, 9, 16), SOLID),
    ((1, 9, 6, 2, 10, 7), IRON),
    ((1, 9, 9, 2, 10, 10), IRON),
    ((1, 10, 6, 2, 11, 10), IRON),
]
GARAGE_DOOR_BLOCK = [((0, 0, 2, 16, 16, 4), SOLID)]
GARAGE_DOOR_ITEM = [
    ((7, 12, 0, 9, 16, 16), SOLID),
    ((7, 10, 0, 9, 12, 5), SOLID),
    ((7, 10, 11, 9, 12, 16), SOLID),
    ((7, 0, 0, 9, 10, 16), SOLID),
]


def conditional_loot(name: str, properties: dict) -> None:
    """Drops the block only from the state with these properties, surviving explosions."""
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
                    "properties": properties,
                }],
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    })


def block(name: str, block_boxes, item_model) -> None:
    write(ASSETS / f"models/block/{name}.json", boxes_model(block_boxes, SOLID))
    write(ASSETS / f"models/item/{name}.json", item_model)
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    AXE_MINEABLE.append(f"{MOD}:{name}")


def generate() -> None:
    frame = {"item": f"{MOD}:carpenters_block"}

    door = "carpenters_door"
    block(door, DOOR_BLOCK, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{door}"}})
    # One door per door, from the lower half, as vanilla's
    conditional_loot(door, {"half": "lower"})
    shaped_recipe(door, "redstone", ["XX", "XX", "XX"], {"X": frame}, 1)

    hatch = "carpenters_hatch"
    block(hatch, HATCH_BLOCK, boxes_model(HATCH_ITEM, SOLID))
    write(DATA / f"{MOD}/loot_table/blocks/{hatch}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"{MOD}:{hatch}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/{hatch}",
    })
    shaped_recipe(hatch, "redstone", ["XXX", "XXX"], {"X": frame}, 1)

    garage = "carpenters_garage_door"
    block(garage, GARAGE_DOOR_BLOCK, boxes_model(GARAGE_DOOR_ITEM, SOLID))
    # Only the top piece drops the door, as the original
    conditional_loot(garage, {"host": "true"})
    shaped_recipe(garage, "redstone", ["XXX", "X X", "XXX"], {"X": frame}, 8)
