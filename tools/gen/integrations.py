"""Strings for the optional integrations: Jade/WTHIT tooltip lines (TooltipLines) and their toggles."""

import re
from pathlib import Path

from .common import LANG, MOD


def generate() -> None:
    LANG.update({
        f"{MOD}.tooltip.cover": "Cover: %s",
        f"{MOD}.tooltip.side_cover": "Side cover: %s",
        f"{MOD}.tooltip.dye": "Dye: %s",
        f"{MOD}.tooltip.overlay": "Overlay: %s",
        f"{MOD}.tooltip.design": "Design: %s",
        f"{MOD}.tooltip.illuminated": "Illuminated",
        f"{MOD}.tooltip.rotation": "Turned %s°",
        # Jade's toggle names for each provider
        f"config.jade.plugin_{MOD}.covers": "Carpenter's Blocks covers",
        f"config.jade.plugin_{MOD}.tile": "Carpenter's Tile",
        # WTHIT's name for the plugin
        f"config.waila.plugin_{MOD}.plugin": "Carpenter's Blocks",
    })
    config_lang()


CONFIG_OPTIONS = Path(__file__).resolve().parents[2] / "common/src/main/java/com/carpentersblocks/config/ConfigOptions.java"
SECTION_NAMES = {"features": "Features", "blocks": "Blocks", "items": "Items", "recipe_quantities": "Recipe Quantities"}


def config_keys():
    """The option keys of ConfigOptions.java, in order, as (section constant, key). The per-block loop is expanded."""
    source = CONFIG_OPTIONS.read_text(encoding="utf-8")
    keys = []
    loop = re.search(r'for \(String block : List\.of\(([^)]*)\)\) \{(.*?)\n        \}', source, re.S)
    blocks = re.findall(r'"(\w+)"', loop.group(1))
    for section, key in re.findall(r'(?:bool|integer|decimal)\((\w+), "(\w+)"(?! \+)', source):
        keys.append((section, key))
    for section, prefix in re.findall(r'(?:bool|integer|decimal)\((\w+), "(\w+)" \+ block', loop.group(2)):
        keys.extend((section, prefix + block) for block in blocks)
    return keys


def config_name(key: str) -> str:
    """enableFlowerPot -> Carpenter's Flower Pot under Blocks; enableCovers -> Enable covers."""
    words = re.findall(r"[A-Z]?[a-z0-9]+|[A-Z]+(?![a-z])", key)
    for prefix in ("enable", "recipeQuantity"):
        rest = key[len(prefix):]
        if key.startswith(prefix) and rest in BLOCK_KEYS:
            return "Carpenter's " + " ".join(re.findall(r"[A-Z][a-z]*", rest))
    return " ".join([words[0].capitalize()] + [w.lower() for w in words[1:]])


BLOCK_KEYS = set()


def config_lang() -> None:
    """Names for the NeoForge config screen and the Fabric (Mod Menu) one, which use the same keys."""
    keys = config_keys()
    BLOCK_KEYS.update(key[len("enable"):] for section, key in keys if section == "BLOCKS")
    BLOCK_KEYS.update(("Hammer", "Chisel", "Tile"))
    LANG[f"{MOD}.configuration.title"] = "Carpenter's Blocks"
    LANG[f"{MOD}.configuration.restart"] = "Changes apply the next time the game starts."
    for section, name in SECTION_NAMES.items():
        LANG[f"{MOD}.configuration.{section}"] = name
    for _, key in keys:
        LANG[f"{MOD}.configuration.{key}"] = config_name(key)
