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
execute at @s run setblock ~6 ~ ~9 carpentersblocks:carpenters_block[shape=up]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_leaves",Properties:{distance:"7",persistent:"true",waterlogged:"false"}}}],Illuminator:{id:"minecraft:glowstone_dust",count:1}}
execute at @s run setblock ~8 ~ ~9 carpentersblocks:carpenters_block[shape=west]{Covers:[{Slot:6b,State:{Name:"minecraft:bricks"}}]}
execute at @s run setblock ~ ~1 ~9 carpentersblocks:carpenters_block
execute at @s run setblock ~-9 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"},Dye:"red"}]}
execute at @s run setblock ~-7 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"},Overlay:"grass",OverlayItem:{id:"minecraft:wheat_seeds",count:1}}]}
execute at @s run setblock ~-5 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"},Overlay:"snow",OverlayItem:{id:"minecraft:snowball",count:1}}]}
execute at @s run setblock ~-3 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:cobblestone"},Overlay:"vine",OverlayItem:{id:"minecraft:vine",count:1}}]}
execute at @s run setblock ~-1 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"},ChiselDesign:"creeper_face"}]}
execute at @s run setblock ~1 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:white_wool"},Dye:"blue",ChiselDesign:"brick"}]}
execute at @s run setblock ~3 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,Dye:"lime"}]}
execute at @s run setblock ~5 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:dirt"},Overlay:"mycelium",OverlayItem:{id:"minecraft:red_mushroom",count:1}}]}
execute at @s run setblock ~7 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"},Overlay:"hay",OverlayItem:{id:"minecraft:wheat",count:1}},{Slot:2b,State:{Name:"minecraft:gold_block"},Overlay:"web",OverlayItem:{id:"minecraft:string",count:1}}]}
execute at @s run setblock ~9 ~ ~6 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:redstone_block"}}]}
execute at @s run setblock ~9 ~ ~5 minecraft:redstone_lamp
execute at @s run setblock ~-7 ~ ~8 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:stone"},Overlay:"moss",OverlayItem:{id:"minecraft:moss_carpet",count:1}}]}
execute at @s run setblock ~-5 ~ ~8 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:cobblestone"},Overlay:"sculk",OverlayItem:{id:"minecraft:sculk_vein",count:1}}]}
execute at @s run setblock ~-3 ~ ~8 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_log",Properties:{axis:"y"}}}]}
execute at @s run setblock ~-1 ~ ~8 carpentersblocks:carpenters_block[shape=full]{Covers:[{Slot:6b,State:{Name:"minecraft:exposed_copper"}}]}
execute at @s run setblock ~1 ~ ~8 carpentersblocks:carpenters_block[shape=down]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
function cbtest:setup_slope
function cbtest:setup_stairs
function cbtest:setup_barrier
give @s carpentersblocks:carpenters_block 64
give @s carpentersblocks:carpenters_hammer
give @s minecraft:grass_block 64
give @s minecraft:glowstone_dust 16
give @s carpentersblocks:carpenters_chisel
give @s minecraft:red_dye 16
give @s minecraft:wheat_seeds 16
give @s minecraft:snowball 16
give @s minecraft:stone 64
give @s minecraft:iron_axe
give @s minecraft:honeycomb 16
