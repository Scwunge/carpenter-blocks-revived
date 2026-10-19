"""The Carpenter's Flower Pot. Its pot, soil and plant are built in Java (FlowerPotFrames) at render time, so the block
model here only gives the particle texture and a stand-in shape; as in the original, the item is a flat pot icon.
Tags decide what goes in it (see PotPlants)."""

from .common import ASSETS, AXE_MINEABLE, DATA, MOD, block_loot, shaped_recipe, write
from .redstone import boxes_model

SOLID = f"{MOD}:block/general/solid"
POT = [(6, 0, 6, 10, 1, 10), (6, 0, 5, 10, 6, 6), (6, 0, 10, 10, 6, 11), (5, 0, 5, 6, 6, 11), (10, 0, 5, 11, 6, 11)]

# Plants beyond bushes (any mod's flowers, saplings, mushrooms, crops), cactus, sugar cane and leaves
PLANTS = ["minecraft:vine", "minecraft:bamboo"]
# What the original took as soil: grass, dirt and sand, and gravel by its material
SOILS = ["#minecraft:dirt", "#minecraft:sand", "#minecraft:nylium", "minecraft:gravel", "minecraft:soul_sand",
         "minecraft:soul_soil", "minecraft:farmland", "minecraft:clay"]
# The original's TRUE_SCALE plants, and the fungi that took their place
FULL_SIZE = ["minecraft:dandelion", "minecraft:azure_bluet", "minecraft:brown_mushroom", "minecraft:red_mushroom",
             "minecraft:crimson_fungus", "minecraft:warped_fungus"]


def generate() -> None:
    name = "carpenters_flower_pot"
    write(ASSETS / f"models/block/{name}.json", boxes_model([(box, SOLID) for box in POT], SOLID))
    write(ASSETS / f"models/item/{name}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:block/flower_pot/flower_pot"}})
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    block_loot(name)
    AXE_MINEABLE.append(f"{MOD}:{name}")

    shaped_recipe(name, "misc", ["X X", " X "], {"X": {"item": f"{MOD}:carpenters_block"}}, 1)

    write(DATA / f"{MOD}/tags/block/flower_pot_plants.json", {"values": PLANTS})
    write(DATA / f"{MOD}/tags/block/flower_pot_soils.json", {"values": SOILS})
    write(DATA / f"{MOD}/tags/block/flower_pot_full_size.json", {"values": FULL_SIZE})
    write(DATA / f"{MOD}/tags/item/fertilizers.json", {"values": ["minecraft:bone_meal"]})
