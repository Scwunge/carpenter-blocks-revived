# Deviations from the original

Where the port behaves or looks differently from the 1.7.10 mod on purpose, by batch. Modern additions (waterlogging,
copper covers and so on) are noted in the code instead.

## Batch 2: Slope

- The slope type lives in the block state and the item kind in the `carpentersblocks:slope_kind` data component, in
  place of tile entity data and item damage. Slope select (sneak + scroll) steps the data component.
- The rail slope fill re-textures each face with the cover for that side, as the block itself does, and keeps
  overlays. The original drew the whole fill with the base cover and suppressed overlays. The fill's side faces take
  their light from the neighboring block, so they can look darker than the original.

## Batch 6: Torch + Ladder

- The default ladder on a wall keeps all its parts 3 pixels deep on every wall. The original drew its rungs 2 deep on
  north and west walls only.

## Batch 7: Door, Hatch, Garage Door

- Redstone opens and closes the door, hatch and garage door only when the signal changes, as vanilla's doors (a
  `powered` state). The original toggled on any neighbor update while powered, so it could flip unexpectedly.
- Villagers and other mobs don't open Carpenter's Doors, and the blocks aren't in vanilla's door and trapdoor tags:
  mob AI casts anything in those tags to vanilla's door class. Mobs path through an open door, hatch or garage door.
- One door drops from the lower half and one garage door from the top (host) piece, by loot table conditions, so
  explosions and modded breakers drop the right count.
- With `enableAlphaPanes`, glass and screen panes draw translucent; otherwise they draw cut out, as the original.
- Garage doors still join any garage door beside them whose facing shares the original's facing bits, so some
  doors at right angles join as they did in the original.
- On Fabric, an open hidden hatch at the top of its space is climbable through a mixin on `LivingEntity.onClimbable`
  that only adds to vanilla's check; NeoForge asks the block's `isLadder`.

## Batch 8: Bed + Flower Pot

### Bed

- The bed is a block item placed like a vanilla bed (foot where you click, head beyond it in the direction you look),
  so it can go on any face whose block has a solid top under both halves. The original's bed item placed only from
  the top face.
- A dye applied while sneaking colors the blanket on both halves instead of the frame, so the frame can't be dyed.
  The original colored the blanket from the foot half's dye and the frame from the head half's.
- Sleeping, respawning and occupancy use vanilla's bed properties. NeoForge asks the block through its bed hooks;
  Fabric uses its sleep events plus one mixin that lets the respawn check accept the Carpenter's Bed.

### Flower Pot

- Plants are chosen by class and tag instead of Forge's plant interfaces and a list of names: any bush (so any mod's
  flowers, saplings, mushrooms and crops), cactus, sugar cane and leaves, plus `carpentersblocks:flower_pot_plants`.
  Soil is `carpentersblocks:flower_pot_soils`, full-size plants `carpentersblocks:flower_pot_full_size`, and
  fertilizer `carpentersblocks:fertilizers` (bone meal).
- Plants are drawn from their own block models, scaled, rather than as crossed squares. The original's thin plant
  profile is gone (thin plants draw at three quarters like the rest), and the whole plant turns with the angle it was
  planted at, not only the sunflower head.
- A pot covered in glass shows the glass, not the original's special `flower_pot_glass` texture.
- Sneak left-click with the hammer only takes the soil out once the plant is gone; the original could leave a plant
  floating.

## Batch 10: Tile

- A tile copies the look only of neighboring tiles on the same kind of face (floor, ceiling or the same wall
  direction). The original could also copy a tile on a neighboring face at a corner.
