"""Writes the structure templates GameTests run in (data/carpentersblocks/structure/*.nbt)."""

import gzip
import struct

from .common import DATA, MOD

DATA_VERSION = 3955  # Minecraft 1.21.1

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def _name(name: str) -> bytes:
    raw = name.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def _payload(tag: int, value) -> bytes:
    if tag == TAG_INT:
        return struct.pack(">i", value)
    if tag == TAG_STRING:
        return _name(value)
    if tag == TAG_LIST:
        elem_tag, items = value
        return struct.pack(">bi", elem_tag if items else TAG_END, len(items)) + b"".join(
            _payload(elem_tag, item) for item in items)
    if tag == TAG_COMPOUND:
        return b"".join(struct.pack(">b", t) + _name(k) + _payload(t, v) for k, (t, v) in value.items()) + b"\x00"
    raise ValueError(tag)


def nbt_file(root: dict) -> bytes:
    """Gzipped NBT with an unnamed root compound, the format StructureTemplateManager reads."""
    return gzip.compress(struct.pack(">b", TAG_COMPOUND) + _name("") + _payload(TAG_COMPOUND, root), mtime=0)


def air_structure(sx: int, sy: int, sz: int) -> dict:
    blocks = [{"pos": (TAG_LIST, (TAG_INT, [x, y, z])), "state": (TAG_INT, 0)}
              for y in range(sy) for z in range(sz) for x in range(sx)]
    return {
        "DataVersion": (TAG_INT, DATA_VERSION),
        "size": (TAG_LIST, (TAG_INT, [sx, sy, sz])),
        "palette": (TAG_LIST, (TAG_COMPOUND, [{"Name": (TAG_STRING, "minecraft:air")}])),
        "blocks": (TAG_LIST, (TAG_COMPOUND, blocks)),
        "entities": (TAG_LIST, (TAG_COMPOUND, [])),
    }


def generate() -> None:
    out = DATA / f"{MOD}/structure/empty.nbt"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_bytes(nbt_file(air_structure(3, 3, 3)))
