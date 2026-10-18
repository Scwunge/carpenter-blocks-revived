# Batch 4: Carpenter's Barrier and Gate. Row z+0 east of the stairs: a fence run with a gate, then each style.
execute at @s run setblock ~1 ~ ~0 carpentersblocks:carpenters_barrier[type=vanilla,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~2 ~ ~0 carpentersblocks:carpenters_barrier[type=vanilla,west=true,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~ ~0 carpentersblocks:carpenters_barrier[type=vanilla,west=true,east=true,post=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~4 ~ ~0 carpentersblocks:carpenters_barrier[type=vanilla_x3,west=true,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~5 ~ ~0 carpentersblocks:carpenters_barrier[type=vanilla_x3,west=true,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~6 ~ ~0 carpentersblocks:carpenters_gate[type=vanilla,facing=west]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~7 ~ ~0 carpentersblocks:carpenters_barrier[type=picket,west=true,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:white_wool"}}]}
execute at @s run setblock ~8 ~ ~0 carpentersblocks:carpenters_barrier[type=shadowbox,west=true,east=true]{Covers:[{Slot:6b,State:{Name:"minecraft:birch_planks"}}]}
execute at @s run setblock ~9 ~ ~0 carpentersblocks:carpenters_barrier[type=wall,west=true]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~9 ~1 ~0 carpentersblocks:carpenters_barrier[type=wall]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~9 ~2 ~0 minecraft:torch
execute at @s run setblock ~6 ~ ~1 carpentersblocks:carpenters_gate[type=picket,facing=south,open=true]{Covers:[{Slot:6b,State:{Name:"minecraft:white_wool"}}]}
give @s carpentersblocks:carpenters_barrier 64
give @s carpentersblocks:carpenters_gate 16
