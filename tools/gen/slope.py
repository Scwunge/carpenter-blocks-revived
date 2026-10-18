"""The Carpenter's Slope. Its geometry is built in code (SlopeGeometry / SlopeFrames); the JSON models only supply the
particle texture and the item's display transforms."""

from .common import AXE_MINEABLE, ASSETS, DATA, MOD, shaped_recipe, write

KINDS = ["wedge", "oblique_interior", "oblique_exterior", "prism", "prism_wedge"]

# Slope IDs (Slope.java order) of each item kind, for the loot table; the drop follows the slope's current type
SLOPES = {
    "wedge": [
        "wedge_se", "wedge_nw", "wedge_ne", "wedge_sw",
        "wedge_neg_n", "wedge_neg_s", "wedge_neg_w", "wedge_neg_e",
        "wedge_pos_n", "wedge_pos_s", "wedge_pos_w", "wedge_pos_e",
    ] + [f"wedge_{kind}_{sign}_{corner}" for kind in ("int", "ext") for sign in ("neg", "pos")
         for corner in ("se", "nw", "ne", "sw")],
    "oblique_interior": [f"obl_int_{sign}_{corner}" for sign in ("neg", "pos") for corner in ("se", "nw", "ne", "sw")],
    "oblique_exterior": [f"obl_ext_{sign}_{corner}" for sign in ("neg", "pos") for corner in ("se", "nw", "ne", "sw")],
    "prism": ["prism_neg", "prism_pos", "prism_1p_pos_n", "prism_1p_pos_s", "prism_1p_pos_w", "prism_1p_pos_e",
              "prism_2p_pos_ns", "prism_2p_pos_we", "prism_2p_pos_se", "prism_2p_pos_nw", "prism_2p_pos_ne",
              "prism_2p_pos_sw", "prism_3p_pos_nwe", "prism_3p_pos_swe", "prism_3p_pos_nsw", "prism_3p_pos_nse",
              "prism_4p_pos"],
    "prism_wedge": ["prism_wedge_pos_n", "prism_wedge_pos_s", "prism_wedge_pos_w", "prism_wedge_pos_e"],
}

# Covers that show their top texture on upward slopes (plus anything extending SnowyDirtBlock), like the original's
# grass material
SLOPE_TOP_COVERS = ["minecraft:grass_block", "minecraft:mycelium", "minecraft:podzol", "minecraft:crimson_nylium",
                    "minecraft:warped_nylium", "minecraft:dirt_path"]

ITEM_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
}


def loot() -> dict:
    entries = []
    for kind, slopes in SLOPES.items():
        entries.append({
            "type": "minecraft:item",
            "name": f"{MOD}:carpenters_slope",
            "conditions": [{
                "condition": "minecraft:any_of",
                "terms": [{"condition": "minecraft:block_state_property", "block": f"{MOD}:carpenters_slope",
                           "properties": {"slope": slope}} for slope in slopes],
            }],
            "functions": [{"function": "minecraft:set_components", "components": {f"{MOD}:slope_kind": kind}}],
        })
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:alternatives", "children": entries}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": f"{MOD}:blocks/carpenters_slope",
    }


def generate() -> None:
    assert sum(len(s) for s in SLOPES.values()) == 65

    texture = f"{MOD}:block/general/full_frame"
    write(ASSETS / "models/block/carpenters_slope.json", {"textures": {"particle": texture}})
    write(ASSETS / "blockstates/carpenters_slope.json", {"variants": {"": {"model": f"{MOD}:block/carpenters_slope"}}})
    write(ASSETS / "models/item/carpenters_slope.json",
          {"parent": "minecraft:block/block", "textures": {"particle": texture}, "display": ITEM_DISPLAY})

    write(DATA / f"{MOD}/loot_table/blocks/carpenters_slope.json", loot())
    write(DATA / f"{MOD}/tags/block/slope_top_covers.json", {"values": SLOPE_TOP_COVERS})

    shaped_recipe("carpenters_slope", "building", ["  X", " XY", "XYY"],
                  {"X": {"tag": "c:rods/wooden"}, "Y": {"item": f"{MOD}:carpenters_block"}}, 6)

    AXE_MINEABLE.append(f"{MOD}:carpenters_slope")
