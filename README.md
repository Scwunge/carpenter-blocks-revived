# Carpenter's Blocks (1.21.1 port)

A port of [Carpenter's Blocks](https://github.com/GTNewHorizons/CarpentersBlocks) to Minecraft 1.21.1, built for
**Fabric** and **NeoForge** from one codebase (Architectury Loom, no runtime library needed).

Original mod by Mineshopper; the 1.7.10 code this port follows is maintained by GTNewHorizons. Licensed under the
GNU LGPL 2.1, like the original (see `LICENSE`).

## Contents

Every block, item, attribute and config option of the original:

- **Blocks**: Carpenter's Block (and slabs), Slope (65 shapes), Stairs, Barrier and Gate, Button, Lever, Pressure
  Plate, Daylight Sensor, Torch, Ladder, Door, Hatch, Garage Door, Bed, Flower Pot, Safe, Collapsible Block.
- **Items**: Hammer, Chisel, Tile.
- **Attributes on every block**: a cover from almost any full block, modded ones included, and a cover per side.
  Each side can also take a dye, an overlay (grass, snow, web, vine, hay, mycelium, plus moss and sculk), a chisel
  design and a glowstone illuminator. Every block also has an owner. Covers set the block's hardness, blast
  resistance, flammability, sounds, light and plant support.
- **Tools**: the hammer changes a block's shape or style (left- and right-click) and takes attributes off
  (sneak + left-click). The chisel cycles designs.

Modern additions:

- Waterlogging on every partial block.
- Axe stripping, waxing and aging on wood and copper covers.
- Wind charges swing the doors, hatches, gates, buttons and levers.
- Cover allow and deny lists as tags.
- Plants for the flower pot by tag.
- Item data components in place of item damage.
- The Safe has owners by UUID, comparator output, and hopper and pipe rules.
- Config as TOML on NeoForge and JSON on Fabric, with a screen in Mod Menu and NeoForge's mod list.
- Optional support for JEI, EMI, REI, Jade, WTHIT and Mod Menu.

Where the port deliberately behaves differently from the original, the difference is listed in
[docs/DEVIATIONS.md](docs/DEVIATIONS.md).

## Layout

- `common/`: all game logic, the code-built frame meshes (`client/frame`), and the shared model code
  (`client/CoverQuads.java` re-textures frames with each cover's textures).
- `fabric/`, `neoforge/`: registration, events, model wrappers and the few mixins each loader needs.
- `tools/gen_resources.py`: generates block states, models, recipes, loot tables, tags and the 16 translations. Run
  it with `python -I -X utf8 tools/gen_resources.py`.
- `tools/testworld`: a datapack that builds a scene of every block for checking by eye (`/function cbtest:setup`).
- `docs/`: the port plan, status and deviations.

## Building and testing

Needs JDK 21.

```bash
./gradlew build
```

Jars land in `fabric/build/libs` and `neoforge/build/libs`.

```bash
bash tools/check.sh
```

`tools/check.sh` builds and runs the unit tests, then runs every GameTest headless on both loaders.

To play in a dev client, use `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`. To load the test world,
first copy it to `<loader>/run/saves/cbtest`, then add `--args="--quickPlaySingleplayer cbtest"`.
