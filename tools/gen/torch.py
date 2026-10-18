"""The Carpenter's Torch. Its frame geometry is built in Java (TorchFrames) at render time, so the block model here
only gives the particle texture and a stand-in shape; as in the original, the item is a flat torch icon."""

from .common import ASSETS, AXE_MINEABLE, LANG, MOD, block_loot, shaped_recipe, write
from .redstone import boxes_model

SOLID = f"{MOD}:block/general/solid"


def generate() -> None:
    name = "carpenters_torch"
    write(ASSETS / f"models/block/{name}.json", boxes_model([((7, 0, 7, 9, 10, 9), SOLID)], SOLID))
    write(ASSETS / f"models/item/{name}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:block/torch/torch"}})
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    block_loot(name)
    AXE_MINEABLE.append(f"{MOD}:{name}")

    # Coal or charcoal over a frame, as the original's two recipes
    shaped_recipe(name, "misc", ["X", "Y"], {"X": {"tag": "minecraft:coals"}, "Y": {"item": f"{MOD}:carpenters_block"}},
                  8)

    LANG["block.carpentersblocks.carpenters_torch"] = "Carpenter's Torch"
