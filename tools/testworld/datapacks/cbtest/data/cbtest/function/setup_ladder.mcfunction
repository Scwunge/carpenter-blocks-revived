# Batch 6: the ladder. Row z-1 on the stone wall at z-2: a joined row of three, a rail and a pole, and two freestanding ladders.
execute at @s run fill ~1 ~ ~-2 ~9 ~2 ~-2 minecraft:stone
execute at @s run setblock ~1 ~ ~-1 carpentersblocks:carpenters_ladder[facing=south]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~2 ~ ~-1 carpentersblocks:carpenters_ladder[facing=south]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~ ~-1 carpentersblocks:carpenters_ladder[facing=south]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~5 ~ ~-1 carpentersblocks:carpenters_ladder[facing=south,type=rail]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~5 ~1 ~-1 carpentersblocks:carpenters_ladder[facing=south,type=rail]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~7 ~ ~-1 carpentersblocks:carpenters_ladder[facing=south,type=pole]{Covers:[{Slot:6b,State:{Name:"minecraft:birch_planks"}}]}
execute at @s run setblock ~9 ~ ~0 carpentersblocks:carpenters_ladder[facing=up]{Covers:[{Slot:6b,State:{Name:"minecraft:jungle_planks"}}]}
execute at @s run setblock ~9 ~1 ~0 carpentersblocks:carpenters_ladder[facing=up]{Covers:[{Slot:6b,State:{Name:"minecraft:jungle_planks"}}]}
execute at @s run setblock ~10 ~ ~0 carpentersblocks:carpenters_ladder[facing=down,type=rail]
give @s carpentersblocks:carpenters_ladder 16
