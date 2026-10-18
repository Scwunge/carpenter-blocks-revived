"""Translations: tools/lang/<locale>.json -> assets/carpentersblocks/lang/<locale>.json.

tools/lang holds every string of the original mod under modern keys, for all blocks and items whether or not they
are registered yet. It was produced once from the original's .lang files with

    python -I -X utf8 tools/gen/lang.py --import ref/CarpentersBlocks/src/main/resources/assets/carpentersblocks/lang

and is edited by hand from then on. Family modules put their own en_us strings into common.LANG; anything they don't
set comes from tools/lang/en_us.json, and the other 15 locales are written straight from tools/lang.
"""

import json
import re
import sys
from pathlib import Path

MOD = "carpentersblocks"
LANG_SOURCE = Path(__file__).resolve().parent.parent / "lang"

# Original block names (tile.blockCarpentersX.name) -> modern block IDs
BLOCKS = {
    "Barrier": "carpenters_barrier",
    "Bed": "carpenters_bed",
    "Block": "carpenters_block",
    "Button": "carpenters_button",
    "CollapsibleBlock": "carpenters_collapsible_block",
    "DaylightSensor": "carpenters_daylight_sensor",
    "Door": "carpenters_door",
    "FlowerPot": "carpenters_flower_pot",
    "GarageDoor": "carpenters_garage_door",
    "Gate": "carpenters_gate",
    "Hatch": "carpenters_hatch",
    "Ladder": "carpenters_ladder",
    "Lever": "carpenters_lever",
    "PressurePlate": "carpenters_pressure_plate",
    "Safe": "carpenters_safe",
    "Slope": "carpenters_slope",
    "Stairs": "carpenters_stairs",
    "Torch": "carpenters_torch",
}

# Original items (item.itemCarpentersX.name) -> modern item IDs. Door and bed were separate items in 1.7.10; on
# 1.21.1 they are the blocks' own items, so their names also fill in the block keys above.
ITEMS = {
    "Hammer": "carpenters_hammer",
    "Chisel": "carpenters_chisel",
    "Door": "carpenters_door",
    "Bed": "carpenters_bed",
    "Tile": "carpenters_tile",
}

# Slope subtype names (tile.blockCarpentersSlope.<type>.name), keyed under the slope block
SLOPE_TYPES = {
    "wedge": "wedge",
    "obliqueInterior": "oblique_interior",
    "obliqueExterior": "oblique_exterior",
    "prism": "prism",
    "prismWedge": "prism_wedge",
}

FIXED = {
    "itemGroup.carpentersBlocks.name": "itemGroup.carpentersblocks",
    "gui.tile.carpentersSafe.title": f"container.{MOD}.carpenters_safe",
    "entity.CarpentersBlocks.CarpentersTile.name": f"entity.{MOD}.carpenters_tile",
}


def modern_key(old: str) -> str:
    if old in FIXED:
        return FIXED[old]
    if m := re.fullmatch(r"tile\.blockCarpenters(\w+?)\.name", old):
        return f"block.{MOD}.{BLOCKS[m.group(1)]}"
    if m := re.fullmatch(r"tile\.blockCarpentersSlope\.(\w+)\.name", old):
        return f"block.{MOD}.carpenters_slope.{SLOPE_TYPES[m.group(1)]}"
    if m := re.fullmatch(r"item\.itemCarpenters(\w+)\.name", old):
        return f"item.{MOD}.{ITEMS[m.group(1)]}"
    if m := re.fullmatch(r"message\.(\w+)\.name", old):
        return f"message.{MOD}.{m.group(1)}"
    raise KeyError(f"No modern key for {old}")


def parse_lang(text: str) -> dict:
    entries = {}
    for line in text.lstrip("﻿").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        entries[key.strip()] = value.strip()
    return entries


def convert(old: dict) -> dict:
    new = {}
    for key, value in old.items():
        new[modern_key(key)] = value
        # Door and bed items share their block's ID; vanilla's BlockItem shows the block key
        if m := re.fullmatch(r"item\.itemCarpenters(Door|Bed)\.name", key):
            new.setdefault(f"block.{MOD}.{ITEMS[m.group(1)]}", value)
    return dict(sorted(new.items()))


def import_ref(lang_dir: Path) -> None:
    LANG_SOURCE.mkdir(parents=True, exist_ok=True)
    for path in sorted(lang_dir.glob("*.lang")):
        locale = path.stem.lower()
        entries = convert(parse_lang(path.read_text(encoding="utf-8")))
        (LANG_SOURCE / f"{locale}.json").write_text(
            json.dumps(entries, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        print(f"{path.name} -> tools/lang/{locale}.json ({len(entries)} strings)")


def generate() -> None:
    """en_us fills gaps in the shared LANG (family modules' own strings win); other locales are written here."""
    from .common import ASSETS, LANG

    for source in sorted(LANG_SOURCE.glob("*.json")):
        strings = json.loads(source.read_text(encoding="utf-8"))
        if source.stem == "en_us":
            for key, value in strings.items():
                LANG.setdefault(key, value)
            continue
        out = ASSETS / "lang" / source.name
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(dict(sorted(strings.items())), indent=2, ensure_ascii=False) + "\n",
                       encoding="utf-8", newline="\n")


if __name__ == "__main__":
    if len(sys.argv) == 3 and sys.argv[1] == "--import":
        import_ref(Path(sys.argv[2]))
    else:
        sys.exit(__doc__)
