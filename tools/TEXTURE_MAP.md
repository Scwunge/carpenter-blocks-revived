# Texture map

Every texture of the original mod (`assets/carpentersblocks/textures/` in 1.7.10) and where it lives in the port
(`common/src/main/resources/assets/carpentersblocks/textures/`). Model and code references use the new path
without `.png`, e.g. `carpentersblocks:block/torch/torch_head_lit`.

Rules: `blocks/` -> `block/`, `items/` -> `item/` with the item's registry name, folder names in snake_case
(`daylightsensor` -> `daylight_sensor`, `flowerpot` -> `flower_pot`, `garagedoor` -> `garage_door`), file names kept.

| Original | Port | Note |
|---|---|---|
| `blocks/bed/bed_pillow.png` | `block/bed/bed_pillow.png` |  |
| `blocks/daylightsensor/daylight_sensor_glass_top.png` | `block/daylight_sensor/daylight_sensor_glass_top.png` |  |
| `blocks/designs/bed/carpenters_hammer.png` | `block/designs/bed/carpenters_hammer.png` |  |
| `blocks/designs/bed/carpenters_quilt_green.png` | `block/designs/bed/carpenters_quilt_green.png` |  |
| `blocks/designs/bed/carpenters_quilt_multicolor.png` | `block/designs/bed/carpenters_quilt_multicolor.png` |  |
| `blocks/designs/bed/carpenters_quilt_orange.png` | `block/designs/bed/carpenters_quilt_orange.png` |  |
| `blocks/designs/bed/carpenters_rainbow.png` | `block/designs/bed/carpenters_rainbow.png` |  |
| `blocks/designs/bed/carpenters_snowflake.png` | `block/designs/bed/carpenters_snowflake.png` |  |
| `blocks/designs/bed/template/template_16x.png` | `block/designs/bed/template/template_16x.png` |  |
| `blocks/designs/bed/template/template_64x.png` | `block/designs/bed/template/template_64x.png` |  |
| `blocks/designs/chisel/brick.png` | `block/designs/chisel/brick.png` | already present |
| `blocks/designs/chisel/brick_slab.png` | `block/designs/chisel/brick_slab.png` | already present |
| `blocks/designs/chisel/brick_slab_staggered.png` | `block/designs/chisel/brick_slab_staggered.png` | already present |
| `blocks/designs/chisel/brick_slab_staggered_vertical.png` | `block/designs/chisel/brick_slab_staggered_vertical.png` | already present |
| `blocks/designs/chisel/brick_slab_vertical.png` | `block/designs/chisel/brick_slab_vertical.png` | already present |
| `blocks/designs/chisel/chiseled.png` | `block/designs/chisel/chiseled.png` | already present |
| `blocks/designs/chisel/column.png` | `block/designs/chisel/column.png` | already present |
| `blocks/designs/chisel/column_top.png` | `block/designs/chisel/column_top.png` | already present |
| `blocks/designs/chisel/creeper_face.png` | `block/designs/chisel/creeper_face.png` | already present |
| `blocks/designs/flowerpot/black.png` | `block/designs/flower_pot/black.png` |  |
| `blocks/designs/flowerpot/blue.png` | `block/designs/flower_pot/blue.png` |  |
| `blocks/designs/flowerpot/brown.png` | `block/designs/flower_pot/brown.png` |  |
| `blocks/designs/flowerpot/cyan.png` | `block/designs/flower_pot/cyan.png` |  |
| `blocks/designs/flowerpot/gray.png` | `block/designs/flower_pot/gray.png` |  |
| `blocks/designs/flowerpot/green.png` | `block/designs/flower_pot/green.png` |  |
| `blocks/designs/flowerpot/light_blue.png` | `block/designs/flower_pot/light_blue.png` |  |
| `blocks/designs/flowerpot/light_gray.png` | `block/designs/flower_pot/light_gray.png` |  |
| `blocks/designs/flowerpot/lime.png` | `block/designs/flower_pot/lime.png` |  |
| `blocks/designs/flowerpot/magenta.png` | `block/designs/flower_pot/magenta.png` |  |
| `blocks/designs/flowerpot/orange.png` | `block/designs/flower_pot/orange.png` |  |
| `blocks/designs/flowerpot/original.png` | `block/designs/flower_pot/original.png` |  |
| `blocks/designs/flowerpot/pink.png` | `block/designs/flower_pot/pink.png` |  |
| `blocks/designs/flowerpot/purple.png` | `block/designs/flower_pot/purple.png` |  |
| `blocks/designs/flowerpot/rainbow.png` | `block/designs/flower_pot/rainbow.png` |  |
| `blocks/designs/flowerpot/red.png` | `block/designs/flower_pot/red.png` |  |
| `blocks/designs/flowerpot/white.png` | `block/designs/flower_pot/white.png` |  |
| `blocks/designs/flowerpot/yellow.png` | `block/designs/flower_pot/yellow.png` |  |
| `blocks/designs/tile/stock_chain.png` | `block/designs/tile/stock_chain.png` |  |
| `blocks/designs/tile/stock_chainlink.png` | `block/designs/tile/stock_chainlink.png` |  |
| `blocks/designs/tile/stock_dandelion.png` | `block/designs/tile/stock_dandelion.png` |  |
| `blocks/designs/tile/stock_decorative.png` | `block/designs/tile/stock_decorative.png` |  |
| `blocks/designs/tile/stock_decorative2.png` | `block/designs/tile/stock_decorative2.png` |  |
| `blocks/designs/tile/stock_diagonal_striped.png` | `block/designs/tile/stock_diagonal_striped.png` |  |
| `blocks/designs/tile/stock_dots.png` | `block/designs/tile/stock_dots.png` |  |
| `blocks/designs/tile/stock_grid.png` | `block/designs/tile/stock_grid.png` |  |
| `blocks/designs/tile/stock_leaves.png` | `block/designs/tile/stock_leaves.png` |  |
| `blocks/designs/tile/stock_offset_squares.png` | `block/designs/tile/stock_offset_squares.png` |  |
| `blocks/designs/tile/stock_quilt.png` | `block/designs/tile/stock_quilt.png` |  |
| `blocks/designs/tile/stock_shifted.png` | `block/designs/tile/stock_shifted.png` |  |
| `blocks/designs/tile/stock_striped.png` | `block/designs/tile/stock_striped.png` |  |
| `blocks/door/door_french_glass_bottom.png` | `block/door/door_french_glass_bottom.png` |  |
| `blocks/door/door_french_glass_top.png` | `block/door/door_french_glass_top.png` |  |
| `blocks/door/door_glass_tall_bottom.png` | `block/door/door_glass_tall_bottom.png` |  |
| `blocks/door/door_glass_tall_top.png` | `block/door/door_glass_tall_top.png` |  |
| `blocks/door/door_glass_top.png` | `block/door/door_glass_top.png` |  |
| `blocks/door/door_screen_tall.png` | `block/door/door_screen_tall.png` |  |
| `blocks/flowerpot/flower_pot.png` | `block/flower_pot/flower_pot.png` |  |
| `blocks/flowerpot/flower_pot_glass.png` | `block/flower_pot/flower_pot_glass.png` |  |
| `blocks/garagedoor/glass.png` | `block/garage_door/glass.png` |  |
| `blocks/garagedoor/glass_top.png` | `block/garage_door/glass_top.png` |  |
| `blocks/general/blank.png` | `block/general/blank.png` |  |
| `blocks/general/full_frame.png` | `block/general/full_frame.png` |  |
| `blocks/general/quartered_frame.png` | `block/frame.png` | already present |
| `blocks/general/solid.png` | `block/general/solid.png` |  |
| `blocks/hatch/hatch_french_glass.png` | `block/hatch/hatch_french_glass.png` |  |
| `blocks/hatch/hatch_glass.png` | `block/hatch/hatch_glass.png` |  |
| `blocks/hatch/hatch_screen.png` | `block/hatch/hatch_screen.png` |  |
| `blocks/lever/lever.png` | `block/lever/lever.png` |  |
| `blocks/overlay/overlay_fast_grass_side.png` | `block/overlay/overlay_fast_grass_side.png` |  |
| `blocks/overlay/overlay_hay_side.png` | `block/overlay/overlay_hay_side.png` | already present |
| `blocks/overlay/overlay_mycelium_side.png` | `block/overlay/overlay_mycelium_side.png` | already present |
| `blocks/overlay/overlay_snow_side.png` | `block/overlay/overlay_snow_side.png` | already present |
| `blocks/safe/safe_light.png` | `block/safe/safe_light.png` |  |
| `blocks/slope/oblique_neg.png` | `block/slope/oblique_neg.png` |  |
| `blocks/slope/oblique_pos.png` | `block/slope/oblique_pos.png` |  |
| `blocks/tile/blank.png` | `block/tile/blank.png` |  |
| `blocks/torch/lantern_glass.png` | `block/torch/lantern_glass.png` |  |
| `blocks/torch/torch.png` | `block/torch/torch.png` |  |
| `blocks/torch/torch_head_lit.png` | `block/torch/torch_head_lit.png` |  |
| `blocks/torch/torch_head_smoldering.png` | `block/torch/torch_head_smoldering.png` |  |
| `blocks/torch/torch_head_unlit.png` | `block/torch/torch_head_unlit.png` |  |
| `items/bed.png` | `item/carpenters_bed.png` |  |
| `items/chisel.png` | `item/carpenters_chisel.png` | already present |
| `items/door.png` | `item/carpenters_door.png` |  |
| `items/hammer.png` | `item/carpenters_hammer.png` | already present |
| `items/tile.png` | `item/carpenters_tile.png` |  |
