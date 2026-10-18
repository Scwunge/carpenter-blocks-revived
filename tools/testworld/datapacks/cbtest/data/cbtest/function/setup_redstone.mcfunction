# Batch 5: the redstone family. Row z+11, in front of a stone wall at z+12: wall and floor mounts of each block.
execute at @s run fill ~-8 ~ ~12 ~6 ~1 ~12 minecraft:stone
execute at @s run setblock ~-8 ~ ~11 carpentersblocks:carpenters_button[facing=north]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-6 ~ ~11 carpentersblocks:carpenters_lever[facing=north,axis=z,powered=true]{Covers:[{Slot:6b,State:{Name:"minecraft:cobblestone"}}]}
execute at @s run setblock ~-7 ~ ~12 minecraft:redstone_lamp
execute at @s run setblock ~-4 ~ ~11 carpentersblocks:carpenters_lever[facing=up,axis=x]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~-2 ~ ~11 carpentersblocks:carpenters_pressure_plate[facing=up,trigger=all]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}]}
execute at @s run setblock ~0 ~ ~11 carpentersblocks:carpenters_daylight_sensor[facing=up,sensitivity=dynamic]{Covers:[{Slot:6b,State:{Name:"minecraft:birch_planks"}}]}
execute at @s run setblock ~2 ~1 ~11 carpentersblocks:carpenters_daylight_sensor[facing=north]{Covers:[{Slot:6b,State:{Name:"minecraft:dark_oak_planks"}}]}
execute at @s run setblock ~4 ~ ~11 carpentersblocks:carpenters_button[facing=north,inverted=true]{Covers:[{Slot:6b,State:{Name:"minecraft:quartz_block"}}]}
execute at @s run setblock ~5 ~ ~12 minecraft:redstone_lamp
execute at @s run setblock ~6 ~1 ~11 carpentersblocks:carpenters_pressure_plate[facing=north,trigger=all]{Covers:[{Slot:6b,State:{Name:"minecraft:mossy_cobblestone"}}]}
give @s carpentersblocks:carpenters_button 16
give @s carpentersblocks:carpenters_lever 16
give @s carpentersblocks:carpenters_pressure_plate 16
give @s carpentersblocks:carpenters_daylight_sensor 16
