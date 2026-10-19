"""The Carpenter's Collapsible Block. Its sloped shape comes from the block entity and is built in Java
(CollapsibleFrames), so the block model is the full cube it starts as: the item, and the particle texture."""

from .block import frame_model
from .common import ASSETS, AXE_MINEABLE, MOD, block_loot, shaped_recipe, write

NAME = "carpenters_collapsible_block"
SOLID = f"{MOD}:block/general/solid"


def generate() -> None:
    write(ASSETS / f"models/block/{NAME}.json", frame_model(([0, 0, 0], [16, 16, 16]), SOLID))
    write(ASSETS / f"models/item/{NAME}.json", {"parent": f"{MOD}:block/{NAME}"})
    write(ASSETS / f"blockstates/{NAME}.json", {"variants": {
        f"vertical_direction={direction}": {"model": f"{MOD}:block/{NAME}"} for direction in ("up", "down")}})
    block_loot(NAME)
    AXE_MINEABLE.append(f"{MOD}:{NAME}")

    shaped_recipe(NAME, "building", ["XXX", "XXX", "XXX"], {"X": {"item": f"{MOD}:carpenters_block"}}, 9)
