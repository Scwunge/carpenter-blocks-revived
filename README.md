# Carpenter's Blocks (1.21.1 port)

A port of [Carpenter's Blocks](https://github.com/GTNewHorizons/CarpentersBlocks) to Minecraft 1.21.1, built for
**Fabric** and **NeoForge** from one codebase (Architectury Loom, no runtime library needed).

Original mod by Mineshopper; the 1.7.10 code this port follows is maintained by GTNewHorizons. Licensed under the
GNU LGPL 2.1, like the original (see `LICENSE`).

## What works so far

- **Carpenter's Block**: right-click it with almost any full block to cover it. The first block covers the whole
  thing; more blocks go on the face you click, so each side can look different.
- **Carpenter's Hammer**: right-click to turn a block into a slab against the clicked side and back. Sneak +
  right-click to take off the last thing applied to that face.
- **Glowstone dust** makes a block give off full light. Covers that glow (glowstone, sea lantern) also light it.
- Grass, leaves and other tinted blocks keep their biome colors; glass and stained glass render see-through.

Not ported yet: slopes, stairs, doors, hatches, and the other block types; dyes, overlays and chisel designs.

## Layout

- `common/`: all game logic and the shared model code (`client/CoverQuads.java` re-textures the frame model with
  each cover's textures)
- `fabric/`, `neoforge/`: registration and each loader's model wrapper
- `tools/gen_resources.py`: generates block states, models, recipes and loot tables

## Building and running

Needs JDK 21.

```bash
./gradlew build
```

Jars land in `fabric/build/libs` and `neoforge/build/libs`. Run the game with `./gradlew :fabric:runClient` or
`./gradlew :neoforge:runClient`. Add `--args="--quickPlaySingleplayer cbtest"` to load the test world once it has been
copied into `<loader>/run/saves/cbtest` (the datapack in `tools/testworld` builds a scene of covered blocks).
