"""The config option keys, read from ConfigOptions.java so the generator and the mod can't disagree."""

import re
from functools import cache
from pathlib import Path

CONFIG_OPTIONS = Path(__file__).resolve().parents[2] / "common/src/main/java/com/carpentersblocks/config/ConfigOptions.java"


@cache
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


def recipe_quantity_option(name: str):
    """The recipeQuantity* option for a recipe result such as carpenters_flower_pot, or None if it has none."""
    camel = "".join(word.capitalize() for word in name.removeprefix("carpenters_").split("_"))
    option = "recipeQuantity" + camel
    return option if any(key == option for _, key in config_keys()) else None
