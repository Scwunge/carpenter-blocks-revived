"""The Carpenter's Ladder. Its frame geometry is built in Java (LadderFrames) at render time, so the block model here
only gives the particle texture and a stand-in shape; the item model follows the original's inventory render."""

from .common import ASSETS, AXE_MINEABLE, DATA, LANG, MOD, block_loot, shaped_recipe, write
from .redstone import boxes_model

FRAME = f"{MOD}:block/frame"

ITEM = [((0, 0, 6, 2, 16, 10), FRAME), ((14, 0, 6, 16, 16, 10), FRAME)] + [
    ((2, y, 7, 14, y + 1, 9), FRAME) for y in (2, 6, 10, 14)]
BLOCK = [((0, 0, 0, 2, 16, 3), FRAME), ((14, 0, 0, 16, 16, 3), FRAME)] + [
    ((2, y, 0, 14, y + 1, 3), FRAME) for y in (2, 6, 10, 14)]


def generate() -> None:
    name = "carpenters_ladder"
    write(ASSETS / f"models/block/{name}.json", boxes_model(BLOCK, FRAME))
    write(ASSETS / f"models/item/{name}.json", boxes_model(ITEM, FRAME))
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    block_loot(name)
    AXE_MINEABLE.append(f"{MOD}:{name}")

    frame = {"item": f"{MOD}:carpenters_block"}
    shaped_recipe(name, "building", ["X X", "XXX", "X X"], {"X": frame}, 4)

    # Climbable the way vanilla and modded ladders are
    write(DATA / "minecraft/tags/block/climbable.json", {"values": [f"{MOD}:{name}"]})

    LANG["block.carpentersblocks.carpenters_ladder"] = "Carpenter's Ladder"
