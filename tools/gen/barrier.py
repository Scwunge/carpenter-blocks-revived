"""The Carpenter's Barrier and Gate. Their frame geometry is built in Java (BarrierGeometry) at render time, so the
block models here only give the particle texture and a stand-in shape; the item models follow the original's
inventory renders."""

from .common import ASSETS, AXE_MINEABLE, DATA, LANG, MOD, block_loot, shaped_recipe, write

FRAME = f"{MOD}:block/frame"

# Boxes in pixels, from the original's renderInventoryBlock
BARRIER_ITEM = [(2, 0, 6, 6, 16, 10), (10, 0, 6, 14, 16, 10), (0, 13, 7, 16, 15, 9), (0, 7, 7, 16, 9, 9)]
GATE_ITEM = [(0, 5, 7, 2, 16, 9), (14, 5, 7, 16, 16, 9), (2, 8, 7, 14, 15, 9)]
# What the world shows before the frame builder replaces it: a lone post, and a closed gate panel
BARRIER_BLOCK = [(6, 0, 6, 10, 16, 10)]
GATE_BLOCK = GATE_ITEM

FACES = ["down", "up", "north", "south", "west", "east"]

# As vanilla's fence and fence gate items
DISPLAY = {
    "gui": {"rotation": [30, 135, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
}


def boxes_model(boxes, display=None):
    model = {
        "parent": "minecraft:block/block",
        "textures": {"frame": FRAME, "particle": FRAME},
        "elements": [{"from": list(box[:3]), "to": list(box[3:]),
                      "faces": {face: {"texture": "#frame"} for face in FACES}} for box in boxes],
    }
    if display:
        model["display"] = display
    return model


def block(name, block_boxes, item_boxes):
    write(ASSETS / f"models/block/{name}.json", boxes_model(block_boxes))
    write(ASSETS / f"models/item/{name}.json", boxes_model(item_boxes, DISPLAY))
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    block_loot(name)
    AXE_MINEABLE.append(f"{MOD}:{name}")


def generate() -> None:
    block("carpenters_barrier", BARRIER_BLOCK, BARRIER_ITEM)
    block("carpenters_gate", GATE_BLOCK, GATE_ITEM)

    stick = {"tag": "c:rods/wooden"}
    frame = {"item": f"{MOD}:carpenters_block"}
    shaped_recipe("carpenters_barrier", "building", [" Y ", "XYX"], {"X": stick, "Y": frame}, 4)
    shaped_recipe("carpenters_gate", "redstone", ["XYX", "XYX"], {"X": stick, "Y": frame}, 1)

    # Barriers join these besides each other, gates, fence gates and solid faces
    write(DATA / f"{MOD}/tags/block/barrier_connects_to.json", {"values": ["#minecraft:fences", "#minecraft:walls"]})
    # Vanilla fences join barriers, leads tie to them, and mobs treat them as too tall to jump
    for tag in ("fences", "wooden_fences"):
        write(DATA / f"minecraft/tags/block/{tag}.json", {"values": [f"{MOD}:carpenters_barrier"]})

    LANG["block.carpentersblocks.carpenters_barrier"] = "Carpenter's Barrier"
    LANG["block.carpentersblocks.carpenters_gate"] = "Carpenter's Gate"
