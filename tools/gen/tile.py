"""The Carpenter's Tile. The tile is an entity drawn in Java (CarpentersTileRenderer), so only its flat item model and
recipe are generated here."""

from .common import ASSETS, MOD, shaped_recipe, write


def generate() -> None:
    name = "carpenters_tile"
    write(ASSETS / f"models/item/{name}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}})
    # Terracotta stands in for the original's hardened clay
    shaped_recipe(name, "decorations", ["XXX", "YYY"],
                  {"X": {"item": "minecraft:terracotta"}, "Y": {"item": f"{MOD}:carpenters_block"}}, 12)
