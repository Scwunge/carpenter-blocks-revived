# Batch 7: doors, hatches and garage doors on their own floor south of the b6 rows (z17 to z22).
execute at @s run fill ~-10 ~-1 ~17 ~10 ~-1 ~22 minecraft:smooth_stone
execute at @s run fill ~-10 ~ ~17 ~10 ~5 ~22 minecraft:air
execute at @s run setblock ~-9 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=glass_top]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-9 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=glass_top]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-7 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=glass_tall]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-7 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=glass_tall]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-5 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=panels]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-5 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=panels]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-3 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=screen_tall]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-3 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=screen_tall]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-1 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=french_glass]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-1 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=french_glass]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~1 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=hidden]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~1 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=hidden]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=true,type=panels]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=true,type=panels]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~5 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=left,open=false,type=french_glass]{Covers:[]}
execute at @s run setblock ~5 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=left,open=false,type=french_glass]{Covers:[]}
execute at @s run setblock ~6 ~0 ~18 carpentersblocks:carpenters_door[facing=south,half=lower,hinge=right,open=false,type=french_glass]{Covers:[]}
execute at @s run setblock ~6 ~1 ~18 carpentersblocks:carpenters_door[facing=south,half=upper,hinge=right,open=false,type=french_glass]{Covers:[]}
execute at @s run fill ~-9 ~ ~21 ~0 ~1 ~21 minecraft:stone
execute at @s run setblock ~-9 ~0 ~20 carpentersblocks:carpenters_hatch[facing=south,half=bottom,open=false,type=hidden]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-9 ~1 ~20 carpentersblocks:carpenters_hatch[facing=south,half=top,open=true,type=hidden]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-7 ~0 ~20 carpentersblocks:carpenters_hatch[facing=south,half=bottom,open=false,type=window]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-7 ~1 ~20 carpentersblocks:carpenters_hatch[facing=south,half=top,open=true,type=window]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-5 ~0 ~20 carpentersblocks:carpenters_hatch[facing=south,half=bottom,open=false,type=screen]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-5 ~1 ~20 carpentersblocks:carpenters_hatch[facing=south,half=top,open=true,type=screen]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-3 ~0 ~20 carpentersblocks:carpenters_hatch[facing=south,half=bottom,open=false,type=french_window]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-3 ~1 ~20 carpentersblocks:carpenters_hatch[facing=south,half=top,open=true,type=french_window]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-1 ~0 ~20 carpentersblocks:carpenters_hatch[facing=south,half=bottom,open=false,type=panel]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-1 ~1 ~20 carpentersblocks:carpenters_hatch[facing=south,half=top,open=true,type=panel]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run fill ~3 ~3 ~21 ~9 ~3 ~21 minecraft:stone
execute at @s run setblock ~3 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~3 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~4 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~4 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~4 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=default,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~6 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass_top,open=false,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~6 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass_top,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~6 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass_top,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~7 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass,open=false,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~7 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~7 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=glass,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~8 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=siding,open=false,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~8 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=siding,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~8 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=siding,open=false,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~9 ~2 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=hidden,open=true,host=true]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~9 ~1 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=hidden,open=true,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~9 ~0 ~21 carpentersblocks:carpenters_garage_door[facing=south,type=hidden,open=true,host=false]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
give @s carpentersblocks:carpenters_door 8
give @s carpentersblocks:carpenters_hatch 8
give @s carpentersblocks:carpenters_garage_door 16
give @s minecraft:lever 4
