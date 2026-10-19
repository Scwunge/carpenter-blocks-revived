"""The Carpenter's Safe. Its frame is built in Java (SafeFrames) at render time, so the block model here only gives the
particle texture and a stand-in shape. The item model is the closed, locked, empty safe, front to the viewer."""

from .common import ASSETS, AXE_MINEABLE, MOD, block_loot, shaped_recipe, write
from .redstone import boxes_model

SOLID = f"{MOD}:block/general/solid"
IRON = "minecraft:block/iron_block"
LIGHT = f"{MOD}:block/safe/safe_light"

# As SafeGeometry, in the original's block units, for a south-facing safe
BODY = [
    (0.0, 0.0, 0.0, 0.0625, 1.0, 1.0),
    (0.9375, 0.0, 0.0, 1.0, 1.0, 1.0),
    (0.0625, 0.0, 0.0625, 0.3125, 0.0625, 1.0),
    (0.375, 0.0, 0.0625, 0.9375, 0.0625, 1.0),
    (0.0625, 0.9375, 0.0625, 0.3125, 1.0, 1.0),
    (0.375, 0.9375, 0.0625, 0.9375, 1.0, 1.0),
    (0.0625, 0.0, 0.0, 0.3125, 1.0, 0.0625),
    (0.375, 0.0, 0.0, 0.9375, 1.0, 0.0625),
    (0.3125, 0.0, 0.0, 0.375, 1.0, 1.0),
    (0.375, 0.625, 0.0625, 0.9375, 0.6875, 0.875),
    (0.375, 0.3125, 0.0625, 0.9375, 0.375, 0.875),
    (0.375, 0.0625, 0.875, 0.9375, 0.9375, 0.9375),  # the door, closed
]
METAL = [
    (0.125, 0.875, 0.9375, 0.25, 0.9375, 1.0),
    (0.0625, 0.0625, 0.9375, 0.125, 0.9375, 1.0),
    (0.25, 0.0625, 0.9375, 0.3125, 0.9375, 1.0),
    (0.125, 0.6875, 0.9375, 0.25, 0.75, 1.0),
    (0.125, 0.0625, 0.9375, 0.25, 0.125, 1.0),
    (0.0625, 0.0625, 0.875, 0.3125, 0.9375, 0.9375),
    (0.8125, 0.375, 0.9375, 0.875, 0.625, 1.0),  # the handle
]
LIGHTS = [(0.125, 0.125, 0.9375, 0.25, 0.6875, 1.0), (0.125, 0.75, 0.9375, 0.25, 0.875, 1.0)]  # capacity, lock


def north(box):
    """A south-facing box (block units) turned to face north, in pixels: the side the inventory view shows."""
    x1, y1, z1, x2, y2, z2 = (v * 16 for v in box)
    return (16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1)


def generate() -> None:
    name = "carpenters_safe"
    write(ASSETS / f"models/block/{name}.json", boxes_model([((0, 0, 0, 16, 16, 16), SOLID)], SOLID))
    item = [(north(b), SOLID) for b in BODY] + [(north(b), IRON) for b in METAL] + [(north(b), LIGHT) for b in LIGHTS]
    write(ASSETS / f"models/item/{name}.json", boxes_model(item, SOLID))
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    AXE_MINEABLE.append(f"{MOD}:{name}")
    block_loot(name)
    frame = {"item": f"{MOD}:carpenters_block"}
    shaped_recipe(name, "misc", ["XXX", "XYX", "XZX"],
                  {"X": frame, "Y": {"item": "minecraft:iron_block"}, "Z": {"item": "minecraft:redstone"}}, 1)
