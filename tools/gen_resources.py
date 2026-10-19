"""Generates the JSON assets and data. Run from the repository root: python -I -X utf8 tools/gen_resources.py

Each block family has its own module in tools/gen; add new ones to MODULES.
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from gen import barrier, bed, block, collapsible, coverable, door, flower_pot, ladder, lang, redstone, slope, stairs, structures, tile, tools, torch, integrations  # noqa: E402
from gen.common import ASSETS, AXE_MINEABLE, DATA, LANG, write  # noqa: E402

# lang goes last: it fills gaps in LANG from tools/lang/en_us.json and writes the other locales
MODULES = [coverable, block, slope, stairs, barrier, redstone, torch, ladder, door, bed, flower_pot, collapsible, tile, tools, structures, integrations, lang]


def main() -> None:
    for module in MODULES:
        module.generate()

    write(DATA / "minecraft/tags/block/mineable/axe.json", {"values": sorted(AXE_MINEABLE)})
    write(ASSETS / "lang/en_us.json", dict(sorted(LANG.items())))


if __name__ == "__main__":
    main()
