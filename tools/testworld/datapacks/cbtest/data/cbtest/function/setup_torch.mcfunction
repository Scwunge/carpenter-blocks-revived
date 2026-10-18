# Batch 6: the torch. Row z-1, in front of a stone wall at z-2: floor and wall torches, smoldering, unlit, lanterns.
execute at @s run fill ~-9 ~ ~-2 ~-1 ~2 ~-2 minecraft:stone
execute at @s run setblock ~-9 ~ ~-1 carpentersblocks:carpenters_torch[facing=up]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-8 ~1 ~-1 carpentersblocks:carpenters_torch[facing=south]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}]}
execute at @s run setblock ~-7 ~ ~-1 carpentersblocks:carpenters_torch[facing=up,flame=smoldering]{Covers:[{Slot:6b,State:{Name:"minecraft:cobblestone"}}]}
execute at @s run setblock ~-6 ~1 ~-1 carpentersblocks:carpenters_torch[facing=south,flame=unlit]
execute at @s run setblock ~-5 ~ ~-1 carpentersblocks:carpenters_torch[facing=up,type=lantern]{Covers:[{Slot:6b,State:{Name:"minecraft:iron_block"}}]}
execute at @s run setblock ~-4 ~1 ~-1 carpentersblocks:carpenters_torch[facing=south,type=lantern]{Covers:[{Slot:6b,State:{Name:"minecraft:dark_oak_planks"}}]}
give @s carpentersblocks:carpenters_torch 16
