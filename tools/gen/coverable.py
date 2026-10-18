"""Tags and strings shared by every coverable block: attribute items and cover rules."""

from .common import DATA, LANG, MOD, write

# Items that apply each overlay, from the original's default "Overlay Definitions"
OVERLAYS = {
    "grass": ["minecraft:wheat_seeds"],
    "snow": ["minecraft:snowball"],
    "web": ["minecraft:string"],
    "vine": ["minecraft:vine"],
    "hay": ["minecraft:wheat"],
    "mycelium": ["minecraft:brown_mushroom", "minecraft:red_mushroom"],
    # Modern additions
    "moss": ["minecraft:moss_carpet"],
    "sculk": ["minecraft:sculk_vein"],
}


def generate() -> None:
    for overlay, items in OVERLAYS.items():
        write(DATA / f"{MOD}/tags/item/overlays/{overlay}.json", {"values": items})

    write(DATA / f"{MOD}/tags/item/illuminators.json", {"values": [
        "minecraft:glowstone_dust",
        {"id": "#c:dusts/glowstone", "required": False},
    ]})

    # Packs fill these; empty by default
    write(DATA / f"{MOD}/tags/block/cover_denylist.json", {"values": []})
    write(DATA / f"{MOD}/tags/block/cover_allowlist.json", {"values": []})

    LANG["itemGroup.carpentersblocks"] = "Carpenter's Blocks"
