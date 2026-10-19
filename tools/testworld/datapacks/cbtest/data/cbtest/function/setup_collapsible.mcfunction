# Batch 9: the collapsible block, on its own floor north of the b8 rows (z-14 to z-10).
# Row z-12: a grass hillside falling west to east, a ridge and a dip, a flat sheet and bare blocks.
# Row z-10: hanging blocks under a stone ceiling.
execute at @s run fill ~-10 ~-1 ~-14 ~10 ~-1 ~-10 minecraft:smooth_stone
execute at @s run fill ~-10 ~ ~-14 ~10 ~5 ~-10 minecraft:air
execute at @s run setblock ~-9 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}],Data:4329600}
execute at @s run setblock ~-8 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}],Data:4327488}
execute at @s run setblock ~-7 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}],Data:2162688}
execute at @s run setblock ~-5 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}],Data:4194304}
execute at @s run setblock ~-3 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}],Data:4261888}
execute at @s run setblock ~-1 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}],Data:1149984}
execute at @s run setblock ~1 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}],Data:0}
execute at @s run setblock ~3 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[],Data:3247200}
execute at @s run setblock ~5 ~ ~-12 carpentersblocks:carpenters_collapsible_block[vertical_direction=up]{Covers:[],Data:4194304}
execute at @s run fill ~-9 ~3 ~-10 ~5 ~3 ~-10 minecraft:stone
execute at @s run setblock ~-9 ~2 ~-10 carpentersblocks:carpenters_collapsible_block[vertical_direction=down]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}],Data:4327488}
execute at @s run setblock ~-7 ~2 ~-10 carpentersblocks:carpenters_collapsible_block[vertical_direction=down]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}],Data:4194304}
execute at @s run setblock ~-5 ~2 ~-10 carpentersblocks:carpenters_collapsible_block[vertical_direction=down]{Covers:[],Data:1149984}
execute at @s run setblock ~-3 ~2 ~-10 carpentersblocks:carpenters_collapsible_block[vertical_direction=down]{Covers:[],Data:3247200}
give @s carpentersblocks:carpenters_collapsible_block 32
