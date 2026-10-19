package com.carpentersblocks.client.frame;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.block.flowerpot.CarpentersFlowerPotBlock;
import com.carpentersblocks.block.flowerpot.FlowerPotBlockEntity;
import com.carpentersblocks.block.flowerpot.PotGeometry;
import com.carpentersblocks.block.flowerpot.PotPlants;
import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.client.SlopeSelectClient;
import com.carpentersblocks.network.EnrichPlantPayload;

/**
 * Frame mesh of the Carpenter's Flower Pot, from {@link PotGeometry}. The pot takes the cover, or shows its design in
 * the cover's place; the soil and the plant are drawn from their own block models, tinted through {@link CoverTint},
 * as the original's {@code BlockHandlerCarpentersFlowerPot}.
 */
public final class FlowerPotFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");

    /** Tint indexes of the soil's and plant's quads, before {@link CoverTint#custom}: the model's tint index plus this. */
    private static final int SOIL_TINT = 0x00, PLANT_TINT = 0x40, TINT_INDEX_MASK = 0x3F;
    /** The richer green of a fertilized plant, as the original's 0.45, 0.80, 0.30. */
    private static final int FERTILIZED_COLOR = 0x73CC4D;
    /** Plants drawn at three quarters of their size, as the original's {@code REDUCED_SCALE}. */
    private static final float REDUCED_SCALE = 0.75F;

    private static final long SEED = 42L;

    private FlowerPotFrames() {}

    /** What the pot looks like beyond its state. */
    private record Look(String design, @Nullable BlockState soil, @Nullable BlockState plant, int angle) {}

    public static final FrameBuilder POT = new FrameBuilder() {
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            if (!(level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot)) {
                return null;
            }
            String design = DesignList.FLOWER_POT.isValid(pot.getDesign()) ? pot.getDesign() : "";
            return new Look(design, pot.hasSoil() ? PotPlants.soilState(pot.getSoil()) : null,
                    PotPlants.plantState(pot.getPlant()), pot.getAngle());
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            Look look = key instanceof Look l ? l : new Look("", null, null, 0);
            for (Box box : PotGeometry.POT) {
                if (look.design().isEmpty()) {
                    mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16,
                            (float) box.x2() / 16, (float) box.y2() / 16, (float) box.z2() / 16, SOLID);
                } else {
                    FixedQuads.box(mesh, box, DesignList.FLOWER_POT.texture(look.design()), FixedQuads.WHITE);
                }
            }
            if (look.soil() != null) {
                addBlockBoxes(mesh, look.soil(), List.of(PotGeometry.SOIL), SOIL_TINT);
            }
            if (look.plant() != null) {
                addPlant(mesh, look.plant(), look.angle());
            }
        }
    };

    private static BakedModel model(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    /** Boxes drawn with a block's face sprites and tints, as the original drew soil and stacked plants. */
    private static void addBlockBoxes(FrameMesh mesh, BlockState block, List<Box> boxes, int tintBase) {
        BakedModel model = model(block);
        RandomSource random = RandomSource.create();
        for (Direction face : Direction.values()) {
            random.setSeed(SEED);
            List<BakedQuad> quads = model.getQuads(block, face, random);
            for (Box box : boxes) {
                for (BakedQuad quad : quads) {
                    BakedQuad drawn = FixedQuads.face(box, face, quad.getSprite(), FixedQuads.WHITE);
                    mesh.fixed(quad.isTinted()
                            ? FixedQuads.withTint(drawn, CoverTint.custom(tintBase | quad.getTintIndex() & TINT_INDEX_MASK))
                            : drawn);
                }
            }
        }
    }

    private static void addPlant(FrameMesh mesh, BlockState plant, int angle) {
        switch (PotPlants.profile(plant)) {
            case STACKED -> addBlockBoxes(mesh, plant, PotGeometry.STACKED, PLANT_TINT);
            case FULL -> addModel(mesh, plant, 1.0F, 0, angle);
            case REDUCED -> addModel(mesh, plant, REDUCED_SCALE, 0, angle);
            case DOUBLE -> {
                addModel(mesh, plant, REDUCED_SCALE, 0, angle);
                if (plant.hasProperty(DoublePlantBlock.HALF)) {
                    addModel(mesh, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), REDUCED_SCALE,
                            REDUCED_SCALE, angle);
                }
            }
        }
    }

    /**
     * A plant's own model, shrunk to {@code scale} about the middle of the floor, raised onto the soil (and by
     * {@code lift}, block units), and turned by the angle it was planted at.
     */
    private static void addModel(FrameMesh mesh, BlockState plant, float scale, float lift, int angle) {
        BakedModel model = model(plant);
        RandomSource random = RandomSource.create();
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction face : Direction.values()) {
            random.setSeed(SEED);
            quads.addAll(model.getQuads(plant, face, random));
        }
        random.setSeed(SEED);
        quads.addAll(model.getQuads(plant, null, random));

        float radians = PotGeometry.radians(angle);
        float cos = (float) Math.cos(radians), sin = (float) Math.sin(radians);
        float base = (float) PotGeometry.PLANT_BASE / 16 + lift;
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone();
            int stride = vertices.length / 4;
            for (int i = 0; i < 4; ++i) {
                int o = i * stride;
                float x = (Float.intBitsToFloat(vertices[o]) - 0.5F) * scale;
                float y = Float.intBitsToFloat(vertices[o + 1]) * scale + base;
                float z = (Float.intBitsToFloat(vertices[o + 2]) - 0.5F) * scale;
                vertices[o] = Float.floatToRawIntBits(x * cos - z * sin + 0.5F);
                vertices[o + 1] = Float.floatToRawIntBits(y);
                vertices[o + 2] = Float.floatToRawIntBits(x * sin + z * cos + 0.5F);
                // Turn the normal too, where the model has one
                int normal = vertices[o + 7];
                if (normal != 0) {
                    float nx = (byte) (normal & 0xFF) / 127.0F, nz = (byte) ((normal >> 16) & 0xFF) / 127.0F;
                    int rx = Math.round((nx * cos - nz * sin) * 127), rz = Math.round((nx * sin + nz * cos) * 127);
                    vertices[o + 7] = (normal & 0xFF00FF00) | (rx & 0xFF) | ((rz & 0xFF) << 16);
                }
            }
            Direction direction = quad.getDirection();
            if (direction.getAxis() != Direction.Axis.Y) {
                float dx = direction.getStepX(), dz = direction.getStepZ();
                direction = Direction.getNearest(dx * cos - dz * sin, 0, dx * sin + dz * cos);
            }
            int tint = quad.isTinted() ? CoverTint.custom(PLANT_TINT | quad.getTintIndex() & TINT_INDEX_MASK) : -1;
            mesh.fixed(new BakedQuad(vertices, tint, direction, quad.getSprite(), quad.isShade()));
        }
    }

    /** Client hooks: the tint of the soil and plant, and asking the server to fertilize a tintable plant. */
    public static void registerClient(CarpentersFlowerPotBlock block) {
        CoverTint.registerCustom(block, (state, level, pos, tint) -> {
            if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot)) {
                return -1;
            }
            boolean plant = (tint & PLANT_TINT) != 0;
            BlockState inside = plant ? PotPlants.plantState(pot.getPlant()) : PotPlants.soilState(pot.getSoil());
            if (inside == null) {
                return -1;
            }
            if (plant && pot.isFertilized()) {
                return FERTILIZED_COLOR;
            }
            return Minecraft.getInstance().getBlockColors().getColor(inside, level, pos, tint & TINT_INDEX_MASK);
        });
        CarpentersFlowerPotBlock.requestEnrich = (level, pos) -> {
            if (!(level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot)) {
                return false;
            }
            BlockState plant = PotPlants.plantState(pot.getPlant());
            if (plant == null || Minecraft.getInstance().getBlockColors().getColor(plant, level, pos, 0) == -1) {
                return false;
            }
            SlopeSelectClient.sender.accept(new EnrichPlantPayload(pos));
            return true;
        };
    }
}
