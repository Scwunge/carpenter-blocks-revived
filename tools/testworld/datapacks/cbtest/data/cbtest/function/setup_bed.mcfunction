# Batch 8: the bed and the flower pot, on their own floor north of the b6 rows (z-8 to z-3).
execute at @s run fill ~-10 ~-1 ~-8 ~10 ~-1 ~-3 minecraft:smooth_stone
execute at @s run fill ~-10 ~ ~-8 ~10 ~5 ~-3 minecraft:air
execute at @s run setblock ~-9 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[]}
execute at @s run setblock ~-9 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[]}
execute at @s run setblock ~-7 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-7 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}]}
execute at @s run setblock ~-5 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Design:"carpenters_rainbow"}
execute at @s run setblock ~-5 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Design:"carpenters_rainbow"}
execute at @s run setblock ~-3 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}],Data:15}
execute at @s run setblock ~-3 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[{Slot:6b,State:{Name:"minecraft:spruce_planks"}}],Data:15}
execute at @s run setblock ~-1 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Data:12}
execute at @s run setblock ~-1 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Data:12}
execute at @s run setblock ~0 ~ ~-5 carpentersblocks:carpenters_bed[facing=south,part=foot]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Data:12}
execute at @s run setblock ~0 ~ ~-4 carpentersblocks:carpenters_bed[facing=south,part=head]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Data:12}
execute at @s run setblock ~-9 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=empty]{Covers:[]}
execute at @s run setblock ~-8 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=empty]{Covers:[{Slot:6b,State:{Name:"minecraft:oak_planks"}}],Soil:{id:"minecraft:dirt",count:1}}
execute at @s run setblock ~-7 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=plant]{Covers:[],Soil:{id:"minecraft:dirt",count:1},Plant:{id:"minecraft:poppy",count:1},Data:8}
execute at @s run setblock ~-6 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=plant]{Covers:[],Soil:{id:"minecraft:grass_block",count:1},Plant:{id:"minecraft:fern",count:1}}
execute at @s run setblock ~-5 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=plant]{Covers:[],Soil:{id:"minecraft:grass_block",count:1},Plant:{id:"minecraft:fern",count:1},Fertilizer:{id:"minecraft:bone_meal",count:1}}
execute at @s run setblock ~-4 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=solid]{Covers:[],Soil:{id:"minecraft:sand",count:1},Plant:{id:"minecraft:cactus",count:1}}
execute at @s run setblock ~-3 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=solid]{Covers:[],Soil:{id:"minecraft:dirt",count:1},Plant:{id:"minecraft:oak_leaves",count:1}}
execute at @s run setblock ~-2 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=plant]{Covers:[],Soil:{id:"minecraft:dirt",count:1},Plant:{id:"minecraft:sunflower",count:1},Data:4}
execute at @s run setblock ~-1 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=plant]{Covers:[],Design:"rainbow",Soil:{id:"minecraft:dirt",count:1},Plant:{id:"minecraft:dandelion",count:1}}
execute at @s run setblock ~0 ~ ~-7 carpentersblocks:carpenters_flower_pot[contents=empty]{Covers:[{Slot:6b,State:{Name:"minecraft:glass"}}]}
give @s carpentersblocks:carpenters_bed 4
give @s carpentersblocks:carpenters_flower_pot 8
give @s minecraft:bone_meal 16
give @s minecraft:poppy 8
