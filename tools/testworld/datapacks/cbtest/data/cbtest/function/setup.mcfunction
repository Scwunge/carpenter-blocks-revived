tag @s add cbtest
gamemode creative @s
time set noon
weather clear
gamerule doDaylightCycle false
gamerule doWeatherCycle false
tp @s ~ 160 ~ 0 10
execute at @s run fill ~-10 ~-1 ~-2 ~10 ~-1 ~12 minecraft:smooth_stone
execute at @s run fill ~-10 ~ ~-2 ~10 ~5 ~12 minecraft:air
execute at @s run setblock ~-8 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[]}
execute at @s run setblock ~-6 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:grass_block",Properties:{snowy:"false"}}}]}
execute at @s run setblock ~-4 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_log",Properties:{axis:"x"}}}]}
execute at @s run setblock ~-2 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:glass"}}]}
execute at @s run setblock ~0 ~ ~9 carpentersblocks:carpenters_block[shape=down]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"}}]}
execute at @s run setblock ~2 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:red_stained_glass"}}]}
execute at @s run setblock ~4 ~ ~9 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}},{Slot:2b,State:{Name:"minecraft:diamond_block"}}]}
execute at @s run setblock ~6 ~ ~9 carpentersblocks:carpenters_block[shape=up]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_leaves",Properties:{distance:"7",persistent:"true",waterlogged:"false"}}}],Illuminated:1b}
execute at @s run setblock ~8 ~ ~9 carpentersblocks:carpenters_block[shape=west]{Covers:[{Slot:6b,State:{Name:"minecraft:bricks"}}]}
execute at @s run setblock ~ ~1 ~9 carpentersblocks:carpenters_block
give @s carpentersblocks:carpenters_block 64
give @s carpentersblocks:carpenters_hammer
give @s minecraft:grass_block 64
give @s minecraft:glowstone_dust 16
