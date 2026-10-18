"""The Carpenter's Hammer and Chisel."""

from .common import LANG, MOD, handheld_item, shaped_recipe


def generate() -> None:
    handheld_item("carpenters_hammer", "carpenters_hammer")
    handheld_item("carpenters_chisel", "carpenters_chisel")

    # Recipes from the original: iron and a Carpenter's Block
    iron_and_block = {"X": {"item": "minecraft:iron_ingot"}, "Y": {"item": f"{MOD}:carpenters_block"}}
    shaped_recipe("carpenters_hammer", "equipment", ["XX ", " YX", " Y "], iron_and_block, 1)
    shaped_recipe("carpenters_chisel", "equipment", ["X", "Y"], iron_and_block, 1)

    LANG["item.carpentersblocks.carpenters_hammer"] = "Carpenter's Hammer"
    LANG["item.carpentersblocks.carpenters_chisel"] = "Carpenter's Chisel"
