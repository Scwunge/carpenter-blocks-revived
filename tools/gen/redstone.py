"""The redstone family: Button, Lever, Pressure Plate and Daylight Sensor. Their frame geometry is built in Java
(RedstoneFrames) at render time, so the block models here only give the particle texture and a stand-in shape; the
item models follow the original's inventory renders."""

from .common import ASSETS, AXE_MINEABLE, DATA, LANG, MOD, block_loot, shaped_recipe, write

FRAME = f"{MOD}:block/frame"
FULL_FRAME = f"{MOD}:block/general/full_frame"
SOLID = f"{MOD}:block/general/solid"
GLASS = f"{MOD}:block/daylight_sensor/daylight_sensor_glass_top"
LAPIS = "minecraft:block/lapis_block"
REDSTONE = "minecraft:block/redstone_block"

FACES = ["down", "up", "north", "south", "west", "east"]

# Boxes in pixels with their texture, from the original's renderInventoryBlock
BUTTON_ITEM = [((5, 6, 6, 11, 10, 10), FRAME)]
PLATE_ITEM = [((0, 7, 0, 16, 9, 16), FULL_FRAME)]
SENSOR_ITEM = [
    ((1, 9, 1, 15, 10, 15), GLASS),
    ((2, 7, 2, 14, 9, 14), LAPIS),
    ((1, 7, 1, 2, 9, 15), REDSTONE),
    ((14, 7, 1, 15, 9, 15), REDSTONE),
    ((2, 7, 1, 14, 9, 2), REDSTONE),
    ((2, 7, 14, 14, 9, 15), REDSTONE),
    ((1, 6, 1, 15, 7, 15), FRAME),
    ((0, 6, 0, 1, 10, 16), FRAME),
    ((15, 6, 0, 16, 10, 16), FRAME),
    ((1, 6, 0, 15, 10, 1), FRAME),
    ((1, 6, 15, 15, 10, 16), FRAME),
]
# What the world shows before the frame builder replaces it, on a floor
BUTTON_BLOCK = [((5, 0, 6, 11, 2, 10), FRAME)]
LEVER_BLOCK = [((5, 0, 3.2, 11, 3, 12.8), SOLID)]
PLATE_BLOCK = [((1, 0, 1, 15, 1, 15), FULL_FRAME)]
SENSOR_BLOCK = [((0, 0, 0, 16, 4, 16), FRAME)]


def boxes_model(boxes, particle):
    textures = sorted({texture for _, texture in boxes})
    names = {texture: f"t{i}" for i, texture in enumerate(textures)}
    model = {
        "parent": "minecraft:block/block",
        "textures": {**{names[t]: t for t in textures}, "particle": particle},
        "elements": [{"from": list(box[:3]), "to": list(box[3:]),
                      "faces": {face: {"texture": f"#{names[texture]}"} for face in FACES}}
                     for box, texture in boxes],
    }
    return model


def block(name, block_boxes, particle, item_model):
    write(ASSETS / f"models/block/{name}.json", boxes_model(block_boxes, particle))
    write(ASSETS / f"models/item/{name}.json", item_model)
    write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    block_loot(name)
    AXE_MINEABLE.append(f"{MOD}:{name}")


def generate() -> None:
    block("carpenters_button", BUTTON_BLOCK, FRAME, boxes_model(BUTTON_ITEM, FRAME))
    # As the original, the lever shows as a flat icon in inventories
    block("carpenters_lever", LEVER_BLOCK, SOLID,
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:block/lever/lever"}})
    block("carpenters_pressure_plate", PLATE_BLOCK, FULL_FRAME, boxes_model(PLATE_ITEM, FULL_FRAME))
    block("carpenters_daylight_sensor", SENSOR_BLOCK, FRAME, boxes_model(SENSOR_ITEM, FRAME))

    frame = {"item": f"{MOD}:carpenters_block"}
    shaped_recipe("carpenters_button", "redstone", ["X"], {"X": frame}, 1)
    shaped_recipe("carpenters_lever", "redstone", ["X", "Y"], {"X": {"tag": "c:rods/wooden"}, "Y": frame}, 1)
    shaped_recipe("carpenters_pressure_plate", "redstone", ["XX"], {"X": frame}, 1)
    shaped_recipe("carpenters_daylight_sensor", "redstone", ["WWW", "XYX", "ZZZ"], {
        "W": {"tag": "c:glass_blocks/colorless"},
        "X": {"tag": "c:dusts/redstone"},
        "Y": {"tag": "c:gems/lapis"},
        "Z": frame,
    }, 1)

    LANG["block.carpentersblocks.carpenters_button"] = "Carpenter's Button"
    LANG["block.carpentersblocks.carpenters_lever"] = "Carpenter's Lever"
    LANG["block.carpentersblocks.carpenters_pressure_plate"] = "Carpenter's Pressure Plate"
    LANG["block.carpentersblocks.carpenters_daylight_sensor"] = "Carpenter's Daylight Sensor"
