"""Recipe-book unlock advancements, one per generated recipe, in vanilla's shape: the recipe unlocks once the player
has it or picks up its key ingredient. Reads the recipes the other modules wrote, so it runs after them."""

import json

from .common import DATA, MOD, write

RECIPES = DATA / f"{MOD}/recipe"
ADVANCEMENTS = DATA / f"{MOD}/advancement/recipes"
FRAME = f"{MOD}:carpenters_block"

# Crafting book category -> vanilla's advancement folder for it (RecipeTests checks the same)
FOLDERS = {"building": "building_blocks", "equipment": "tools", "redstone": "redstone", "misc": "misc"}


def key_ingredient(recipe: dict) -> dict:
    """The Carpenter's Block when the recipe uses one; otherwise the first tag, as the Carpenter's Block's planks."""
    ingredients = list(recipe["key"].values())
    for ingredient in ingredients:
        if ingredient.get("item") == FRAME:
            return ingredient
    tags = [ingredient for ingredient in ingredients if "tag" in ingredient]
    return (tags or ingredients)[0]


def advancement(recipe_id: str, ingredient: dict) -> dict:
    name = ingredient.get("item") or ingredient["tag"]
    criterion = "has_" + name.split(":")[-1].replace("/", "_")
    items = ingredient["item"] if "item" in ingredient else "#" + ingredient["tag"]
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": recipe_id}},
            criterion: {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": items}]}},
        },
        "requirements": [["has_the_recipe", criterion]],
        "rewards": {"recipes": [recipe_id]},
    }


def generate() -> None:
    for path in sorted(RECIPES.glob("*.json")):
        recipe = json.loads(path.read_text(encoding="utf-8"))
        recipe_id = f"{MOD}:{path.stem}"
        write(ADVANCEMENTS / FOLDERS[recipe["category"]] / f"{path.stem}.json",
              advancement(recipe_id, key_ingredient(recipe)))
