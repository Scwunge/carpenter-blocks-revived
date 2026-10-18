"""The Carpenter's Block: a full block that the hammer turns into slabs."""

from .common import ASSETS, AXE_MINEABLE, LANG, MOD, block_loot, shaped_recipe, write

# Shape name -> element bounds (from, to), matching FrameShape
SHAPES = {
    "full": ([0, 0, 0], [16, 16, 16]),
    "down": ([0, 0, 0], [16, 8, 16]),
    "up": ([0, 8, 0], [16, 16, 16]),
    "north": ([0, 0, 0], [16, 16, 8]),
    "south": ([0, 0, 8], [16, 16, 16]),
    "west": ([0, 0, 0], [8, 16, 16]),
    "east": ([8, 0, 0], [16, 16, 16]),
}

# Face -> (axis index, boundary value, which corner) where that face touches the block edge
FACE_BOUNDARY = {
    "down": (1, 0, "from"),
    "up": (1, 16, "to"),
    "north": (2, 0, "from"),
    "south": (2, 16, "to"),
    "west": (0, 0, "from"),
    "east": (0, 16, "to"),
}


def frame_model(bounds, texture=f"{MOD}:block/frame"):
    """A single box with every face textured #frame, culled only where it touches the block's edge."""
    lo, hi = bounds
    faces = {}
    for face, (axis, value, end) in FACE_BOUNDARY.items():
        entry = {"texture": "#frame"}
        coord = lo[axis] if end == "from" else hi[axis]
        if coord == value:
            entry["cullface"] = face
        faces[face] = entry
    # UVs are left out so they follow the element's position, keeping slab textures aligned with full blocks
    return {
        "parent": "minecraft:block/block",
        "textures": {"frame": texture, "particle": texture},
        "elements": [{"from": lo, "to": hi, "faces": faces}],
    }


def generate() -> None:
    variants = {}
    for name, bounds in SHAPES.items():
        model_name = "carpenters_block" if name == "full" else f"carpenters_block_{name}"
        write(ASSETS / f"models/block/{model_name}.json", frame_model(bounds))
        variants[f"shape={name}"] = {"model": f"{MOD}:block/{model_name}"}
    write(ASSETS / "blockstates/carpenters_block.json", {"variants": variants})
    write(ASSETS / "models/item/carpenters_block.json", {"parent": f"{MOD}:block/carpenters_block"})

    block_loot("carpenters_block")

    # From the original: 8 sticks around planks make 5 blocks
    shaped_recipe("carpenters_block", "building", ["XXX", "XYX", "XXX"],
                  {"X": {"item": "minecraft:stick"}, "Y": {"tag": "minecraft:planks"}}, 5)

    AXE_MINEABLE.append(f"{MOD}:carpenters_block")
    LANG["block.carpentersblocks.carpenters_block"] = "Carpenter's Block"
