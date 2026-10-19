"""Strings for the optional integrations: Jade/WTHIT tooltip lines (TooltipLines) and their toggles."""

import re

from .common import LANG, MOD
from .config_options import config_keys


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
    # Item tag names, which recipe viewers show where a recipe takes any item of a tag (tag.item.<ns>.<path, dotted>)
    LANG.update({f"tag.item.{MOD}.{path}": name for path, name in ITEM_TAGS.items()})
    config_lang()


ITEM_TAGS = {
    "fertilizers": "Carpenter's Fertilizers",
    "illuminators": "Carpenter's Illuminators",
    "overlays.grass": "Grass Overlays",
    "overlays.hay": "Hay Overlays",
    "overlays.moss": "Moss Overlays",
    "overlays.mycelium": "Mycelium Overlays",
    "overlays.sculk": "Sculk Overlays",
    "overlays.snow": "Snow Overlays",
    "overlays.vine": "Vine Overlays",
    "overlays.web": "Web Overlays",
}

SECTION_NAMES = {"features": "Features", "blocks": "Blocks", "items": "Items", "recipe_quantities": "Recipe Quantities"}


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
