"""The Carpenter's Stairs: 28 shapes, each a frame model made of the original's boxes."""

from .common import ASSETS, AXE_MINEABLE, LANG, MOD, block_loot, shaped_recipe, write

# Shape name -> boxes (x1, y1, z1, x2, y2, z2) in pixels, matching StairsShape (checked by StairsModelTest)
SHAPES = {
    "side_se": [(0, 0, 0, 8, 16, 16), (8, 0, 0, 16, 16, 8)],
    "side_nw": [(8, 0, 0, 16, 16, 16), (0, 0, 8, 8, 16, 16)],
    "side_ne": [(0, 0, 0, 8, 16, 16), (8, 0, 8, 16, 16, 16)],
    "side_sw": [(8, 0, 0, 16, 16, 16), (0, 0, 0, 8, 16, 8)],
    "neg_n": [(0, 0, 8, 16, 16, 16), (0, 8, 0, 16, 16, 8)],
    "neg_s": [(0, 0, 0, 16, 16, 8), (0, 8, 8, 16, 16, 16)],
    "neg_w": [(8, 0, 0, 16, 16, 16), (0, 8, 0, 8, 16, 16)],
    "neg_e": [(0, 0, 0, 8, 16, 16), (8, 8, 0, 16, 16, 16)],
    "pos_n": [(0, 0, 8, 16, 16, 16), (0, 0, 0, 16, 8, 8)],
    "pos_s": [(0, 0, 0, 16, 16, 8), (0, 0, 8, 16, 8, 16)],
    "pos_w": [(8, 0, 0, 16, 16, 16), (0, 0, 0, 8, 8, 16)],
    "pos_e": [(0, 0, 0, 8, 16, 16), (8, 0, 0, 16, 8, 16)],
    "int_neg_se": [(0, 0, 0, 8, 16, 16), (8, 0, 0, 16, 16, 8), (8, 8, 8, 16, 16, 16)],
    "int_neg_nw": [(8, 0, 0, 16, 16, 16), (0, 0, 8, 8, 16, 16), (0, 8, 0, 8, 16, 8)],
    "int_neg_ne": [(0, 0, 0, 8, 16, 16), (8, 0, 8, 16, 16, 16), (8, 8, 0, 16, 16, 8)],
    "int_neg_sw": [(8, 0, 0, 16, 16, 16), (0, 0, 0, 8, 16, 8), (0, 8, 8, 8, 16, 16)],
    "int_pos_se": [(0, 0, 0, 8, 16, 16), (8, 0, 0, 16, 16, 8), (8, 0, 8, 16, 8, 16)],
    "int_pos_nw": [(8, 0, 0, 16, 16, 16), (0, 0, 8, 8, 16, 16), (0, 0, 0, 8, 8, 8)],
    "int_pos_ne": [(0, 0, 0, 8, 16, 16), (8, 0, 8, 16, 16, 16), (8, 0, 0, 16, 8, 8)],
    "int_pos_sw": [(8, 0, 0, 16, 16, 16), (0, 0, 0, 8, 16, 8), (0, 0, 8, 8, 8, 16)],
    "ext_neg_se": [(0, 0, 0, 8, 16, 8), (8, 8, 0, 16, 16, 16), (0, 8, 8, 8, 16, 16)],
    "ext_neg_nw": [(8, 0, 8, 16, 16, 16), (0, 8, 0, 8, 16, 16), (8, 8, 0, 16, 16, 8)],
    "ext_neg_ne": [(0, 0, 8, 8, 16, 16), (8, 8, 0, 16, 16, 16), (0, 8, 0, 8, 16, 8)],
    "ext_neg_sw": [(8, 0, 0, 16, 16, 8), (0, 8, 0, 8, 16, 16), (8, 8, 8, 16, 16, 16)],
    "ext_pos_se": [(0, 0, 0, 8, 16, 8), (8, 0, 0, 16, 8, 16), (0, 0, 8, 8, 8, 16)],
    "ext_pos_nw": [(8, 0, 8, 16, 16, 16), (0, 0, 0, 8, 8, 16), (8, 0, 0, 16, 8, 8)],
    "ext_pos_ne": [(0, 0, 8, 8, 16, 16), (8, 0, 0, 16, 8, 16), (0, 0, 0, 8, 8, 8)],
    "ext_pos_sw": [(8, 0, 0, 16, 16, 8), (0, 0, 0, 8, 8, 16), (8, 0, 8, 16, 8, 16)],
}

# Face -> (axis, whether it is the box's max side)
FACES = {
    "down": (1, False),
    "up": (1, True),
    "north": (2, False),
    "south": (2, True),
    "west": (0, False),
    "east": (0, True),
}

# The item shows upright stairs climbing east, as the original's inventory render and vanilla's stairs item
ITEM_SHAPE = "pos_w"
ITEM_DISPLAY = {
    "gui": {"rotation": [30, 135, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "head": {"rotation": [0, -90, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "thirdperson_lefthand": {"rotation": [75, -135, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
}


def face_rect(box, axis):
    """The face's extent on the two other axes."""
    return [(box[i], box[i + 3]) for i in range(3) if i != axis]


def hidden(box, face, others):
    """Whether another box lies flush against this face and covers all of it."""
    axis, is_max = FACES[face]
    plane = box[axis + 3] if is_max else box[axis]
    for other in others:
        touching = other[axis] if is_max else other[axis + 3]
        if touching != plane:
            continue
        if all(lo >= olo and hi <= ohi
               for (lo, hi), (olo, ohi) in zip(face_rect(box, axis), face_rect(other, axis))):
            return True
    return False


def stairs_model(boxes, texture=f"{MOD}:block/frame"):
    """Every box textured #frame; faces inside the stairs are left out, faces on the block's edge are culled."""
    elements = []
    for box in boxes:
        others = [other for other in boxes if other is not box]
        faces = {}
        for face, (axis, is_max) in FACES.items():
            if hidden(box, face, others):
                continue
            entry = {"texture": "#frame"}
            if box[axis + 3 if is_max else axis] == (16 if is_max else 0):
                entry["cullface"] = face
            faces[face] = entry
        # UVs are left out so they follow the element's position, keeping textures aligned between blocks
        elements.append({"from": list(box[:3]), "to": list(box[3:]), "faces": faces})
    return {
        "parent": "minecraft:block/block",
        "textures": {"frame": texture, "particle": texture},
        "elements": elements,
    }


def generate() -> None:
    variants = {}
    for name, boxes in SHAPES.items():
        model = f"carpenters_stairs_{name}"
        write(ASSETS / f"models/block/{model}.json", stairs_model(boxes))
        variants[f"shape={name}"] = {"model": f"{MOD}:block/{model}"}
    write(ASSETS / "blockstates/carpenters_stairs.json", {"variants": variants})
    write(ASSETS / "models/item/carpenters_stairs.json",
          {"parent": f"{MOD}:block/carpenters_stairs_{ITEM_SHAPE}", "display": ITEM_DISPLAY})

    block_loot("carpenters_stairs")

    # From the original: a staircase of Carpenter's Blocks makes 4 stairs
    shaped_recipe("carpenters_stairs", "building", ["  X", " XX", "XXX"],
                  {"X": {"item": f"{MOD}:carpenters_block"}}, 4)

    AXE_MINEABLE.append(f"{MOD}:carpenters_stairs")
    LANG["block.carpentersblocks.carpenters_stairs"] = "Carpenter's Stairs"
