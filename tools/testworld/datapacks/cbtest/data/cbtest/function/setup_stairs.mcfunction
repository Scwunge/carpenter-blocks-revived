# Batch 3: Carpenter's Stairs. Row z+3 shows each kind, row z+0 a covered stair run with both corners.
execute at @s run setblock ~-9 ~ ~3 carpentersblocks:carpenters_stairs[shape=pos_n]
execute at @s run setblock ~-7 ~ ~3 carpentersblocks:carpenters_stairs[shape=neg_n]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}]}
execute at @s run setblock ~-5 ~ ~3 carpentersblocks:carpenters_stairs[shape=side_se]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-3 ~ ~3 carpentersblocks:carpenters_stairs[shape=int_pos_nw]{Covers:[{Slot:6b,State:{Name:"minecraft:bricks"}}]}
execute at @s run setblock ~-1 ~ ~3 carpentersblocks:carpenters_stairs[shape=int_neg_se]{Covers:[{Slot:6b,State:{Name:"minecraft:glass"}}]}
execute at @s run setblock ~1 ~ ~3 carpentersblocks:carpenters_stairs[shape=ext_pos_se]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}]}
execute at @s run setblock ~3 ~ ~3 carpentersblocks:carpenters_stairs[shape=ext_neg_nw]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_log",Properties:{axis:"y"}}}]}
execute at @s run setblock ~5 ~ ~3 carpentersblocks:carpenters_stairs[shape=pos_w]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}},{Slot:1b,State:{Name:"minecraft:diamond_block"}}]}
execute at @s run setblock ~7 ~ ~3 carpentersblocks:carpenters_stairs[shape=pos_e,waterlogged=true]
execute at @s run setblock ~9 ~ ~3 carpentersblocks:carpenters_stairs[shape=pos_s]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}],Illuminator:{id:"minecraft:glowstone_dust",count:1}}
execute at @s run setblock ~-4 ~ ~0 carpentersblocks:carpenters_stairs[shape=int_pos_nw]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~-3 ~ ~0 carpentersblocks:carpenters_stairs[shape=pos_n]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~-2 ~ ~0 carpentersblocks:carpenters_stairs[shape=pos_n]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~-1 ~ ~0 carpentersblocks:carpenters_stairs[shape=ext_pos_nw]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~-4 ~ ~1 carpentersblocks:carpenters_stairs[shape=pos_w]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
execute at @s run setblock ~-1 ~ ~1 carpentersblocks:carpenters_stairs[shape=pos_w]{Covers:[{Slot:6b,State:{Name:"minecraft:stone_bricks"}}]}
give @s carpentersblocks:carpenters_stairs 64
