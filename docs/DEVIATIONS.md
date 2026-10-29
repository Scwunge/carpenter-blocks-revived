# Deviations from the original

Where the port behaves or looks differently from the 1.7.10 mod on purpose, by batch. Modern additions (waterlogging,
copper covers and so on) are noted in the code instead.

## All blocks

- `enableRoutableFluids` is read but does nothing. In the original it drew neighboring water and lava into the gaps of
  Carpenter's blocks; waterlogging replaces it.

- Side covers stand a pixel out from their face, as the original's. Where one reaches into the neighboring space,
  its thin edges take their light from the block itself, so they can look darker than the original.

## Batch 2: Slope

- The slope type lives in the block state and the item kind in the `carpentersblocks:slope_kind` data component, in
  place of tile entity data and item damage. Slope select (sneak + scroll) steps the data component.
- The rail slope fill keeps the base cover's overlays; the original suppressed them. The fill's side faces take
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

## Batch 9: Safe + Collapsible Block

### Safe

- Upgrades are matched by the common tags `c:ingots/gold`, `c:gems/diamond` and `c:gems/emerald`, replacing the
  original's ore dictionary names.
- The hammer's turning, locking and automation settings need the owner (or an operator, or singleplayer). The
  original let anyone who could open the safe change them, so a stranger could lock an unlocked safe.
- Comparators read how full a safe is, as a chest. NeoForge pipes reach the slots through an item handler
  capability; Fabric's transfer API finds them through the safe's sided container. Both use the automation setting
  and keep out of the front.
- Clients are sent how full the safe is, not what is in it, and the capacity lights update as soon as they change
  instead of once a second.
- Lit lights glow at full brightness through each loader's renderer (Fabric's emissive material, NeoForge's baked
  light) instead of the original's brightness override.
- The safe's front face counts as solid, as its other faces. The original's front wasn't solid for placing.

### Collapsible Block

- Whether the block stands or hangs is block state (`vertical_direction`), not the data's low bits. The corner
  depths keep the original's bit layout in the block data.
- A hanging block's flat top counts as a whole face, and its bottom only when full. The original always reported the
  bottom whole and the top only when full, whichever way the block faced.
- When placed, it takes corner heights only from collapsible blocks facing the same way. The original read any
  Carpenter's block's data as corner depths.
- Clicks don't send the hit position to the server, so the hammer finds the corner by tracing the player's view, as
  the original's event handler did for left-clicks.
- The outline is at least one pixel tall, so a flat block can still be aimed at. The original's was flat and was hit
  through its zero-height collision boxes.
- Neighbors never cull their faces against it, and it lets light through. Vanilla caches face culling per block
  state, and the depths aren't in the state, so culling by them could leave holes.

## Batch 10: Tile

- A tile copies the look only of neighboring tiles on the same kind of face (floor, ceiling or the same wall
  direction). The original could also copy a tile on a neighboring face at a corner.
