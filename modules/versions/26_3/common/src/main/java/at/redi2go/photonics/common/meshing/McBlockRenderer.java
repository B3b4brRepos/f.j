package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.meshing.impl.BlockBuilderSubmitCollector;
import at.redi2go.photonics.common.meshing.impl.BlockSetBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class McBlockRenderer {
    private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;
    private static final Set<Fluid> WHITELISTED_FLUIDS = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);

    private final RandomSource randomSource = RandomSource.create();
    private final PoseStack poseStack = new PoseStack();

    // 26.3: no AO, but cull faces hidden by neighbours (same behaviour as tesselateWithoutAO before)
    private final ModelBlockRenderer modelRenderer = new ModelBlockRenderer(
            false, true, Minecraft.getInstance().getBlockColors()
    );
    private FluidStateModelSet lastFluidModels;
    private FluidRenderer fluidRenderer;

    private final BlockBuilderSubmitCollector submitCollector = new BlockBuilderSubmitCollector();
    private final CameraRenderState cameraRenderState = createCameraRenderState();

    private final SimpleMeshState.HashStorage hashStorage = new SimpleMeshState.HashStorage();

    private static CameraRenderState createCameraRenderState() {
        var state = new CameraRenderState();
        state.pos = Vec3.ZERO;
        state.blockPos = BlockPos.ZERO;
        return state;
    }

    private FluidRenderer fluidRenderer() {
        var models = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
        if (models != lastFluidModels || fluidRenderer == null) {
            lastFluidModels = models;
            fluidRenderer = new FluidRenderer(models);
        }
        return fluidRenderer;
    }

    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter
    ) {
        if (blockState.getBlock() == Blocks.END_GATEWAY)
            return EmptyMeshState.INSTANCE;

        int blockId = IrisUtil.getBlockId(blockState);
        FluidState fluidState = blockState.getFluidState();

        List<BlockStateModelPart> parts;
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            parts = new ArrayList<>();

            randomSource.setSeed(blockState.getSeed(blockPos));
            Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                    .get(blockState)
                    .collectParts(randomSource, parts);
        } else parts = List.of();

        if (blockState.hasBlockEntity()) return new DynamicMeshState(blockId, fluidState, parts);
        if (WHITELISTED_FLUIDS.contains(fluidState.getType())) return new DynamicMeshState(blockId, fluidState, parts);

        if (parts.isEmpty())
            return EmptyMeshState.INSTANCE;

        var meshState = new SimpleMeshState(blockState.getBlock(), blockId, parts);
        meshState.computeHash(
                hashStorage,
                blockState,
                blockPos,
                blockAndTintGetter
        );

        return meshState;
    }

    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        if (meshState == EmptyMeshState.INSTANCE) return;

        builder.useBlockId(meshState.blockId());

        FluidState fluidState = meshState.fluidState();
        if (!fluidState.isEmpty()) {
            submitFluid(pos, blockAndTintGetter, builder, blockState, fluidState);
        }

        if (blockState.hasBlockEntity()) {
            submitBlockEntity(pos, blockState, blockAndTintGetter, builder);
        }

        if (!meshState.blockModel().isEmpty()) {
            submitBlock(pos, blockState, blockAndTintGetter, builder);
        }
    }

    private void submitFluid(
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder,
            BlockState blockState,
            FluidState fluidState
    ) {
        if (!WHITELISTED_FLUIDS.contains(fluidState.getType())) return;

        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(
                -(blockPos.getX() & 15),
                -(blockPos.getY() & 15),
                -(blockPos.getZ() & 15)
        );

        fluidRenderer().tesselate(
                blockAndTintGetter,
                blockPos,
                layer -> (VertexConsumer) builder,
                blockState,
                fluidState
        );
    }

    private void submitBlock(
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(0f, 0f, 0f);

        VertexConsumer consumer = (VertexConsumer) builder;
        BlockQuadOutput output = consumer::putBlockBakedQuad;

        modelRenderer.tesselateBlock(
                output,
                0f, 0f, 0f,
                blockAndTintGetter,
                pos,
                blockState,
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState),
                blockState.getSeed(pos)
        );
    }

    private static final Set<Block> FULL_BLOCK_ENTITY_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.PLAYER_HEAD)
            .addBlock(Blocks.PLAYER_WALL_HEAD)
            .build();

    private static final Set<Block> LEVEL_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.CHEST)
            .build();

    private BlockEntity copyBlockEntity(BlockState blockState) {
        EntityBlock entityBlock = (EntityBlock) blockState.getBlock();
        return entityBlock.newBlockEntity(new BlockPos(0, 0, 0), blockState);
    }

    private BlockEntity fetchBlockEntity(BlockPos blockPos, BlockState blockState) {
        try {
            var level = Minecraft.getInstance().level;
            if (level == null || !level.isInValidBounds(blockPos)) return copyBlockEntity(blockState);

            var chunk = level.getChunkAt(blockPos);
            return chunk.getBlockEntity(blockPos);
        } catch (Exception e) {
            return copyBlockEntity(blockState);
        }
    }

    private void submitBlockEntity(
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        builder.useOffset(0f, 0f, 0f);
        submitCollector.setBlockBuilder(builder);

        try {
            BlockEntity entity = FULL_BLOCK_ENTITY_REQUIRED_FOR.contains(blockState.getBlock()) ?
                    fetchBlockEntity(blockPos, blockState) :
                    copyBlockEntity(blockState);

            if (entity == null) return;

            if (LEVEL_REQUIRED_FOR.contains(blockState.getBlock()))
                entity.setLevel((Level) blockAndTintGetter);

            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                    Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);

            if (renderer == null) return;

            var renderState = renderer.createRenderState();
            renderer.extractRenderState(entity, renderState, 0.8f, cameraRenderState.pos, null);

            poseStack.pushPose();
            renderer.submit(renderState, poseStack, submitCollector, cameraRenderState);
            poseStack.popPose();
        } finally {
            submitCollector.setBlockBuilder(null);
        }
    }
}
