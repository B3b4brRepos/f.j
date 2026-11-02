package at.redi2go.photonics.common.meshing.impl;

import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.Model.Simple;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives.Group;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/**
 * 26.3 block entity renderers submit nodes to a collector that the feature renderers later draw.
 * We implement the collector ourselves and write geometry that matters for the voxel scene straight
 * into the {@link BlockBuilder}. Everything that is not static block geometry is ignored.
 */
public class BlockBuilderSubmitCollector implements SubmitNodeCollector {
    private BlockBuilder blockBuilder;

    public void setBlockBuilder(BlockBuilder blockBuilder) {
        this.blockBuilder = blockBuilder;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

    @Override
    public <S> void submitModel(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            @Nullable UvMapping uvMapping,
            int outlineColor
    ) {
        VertexConsumer consumer = BlockBuilderRenderTypes.consumerFor(blockBuilder, renderType);
        if (consumer == EmptyVertexConsumer.INSTANCE) return;

        model.setupAnim(state);
        model.renderToBuffer(poseStack, consumer, lightCoords, overlayCoords, tintedColor);
    }

    @Override
    public void submitBlockModel(
            PoseStack poseStack,
            RenderType renderType,
            List<BlockStateModelPart> parts,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int outlineColor
    ) {
        VertexConsumer consumer = BlockBuilderRenderTypes.consumerFor(blockBuilder, renderType);
        if (consumer == EmptyVertexConsumer.INSTANCE) return;

        QuadInstance instance = new QuadInstance();
        instance.setLightCoords(lightCoords);
        instance.setOverlayCoords(overlayCoords);

        for (BlockStateModelPart part : parts) {
            for (Direction direction : DIRECTIONS) {
                putQuads(poseStack, consumer, part.getQuads(direction), tintLayers, instance);
            }
            putQuads(poseStack, consumer, part.getQuads(null), tintLayers, instance);
        }
    }

    private static final Direction[] DIRECTIONS = Direction.values();

    private static void putQuads(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<BakedQuad> quads,
            int[] tintLayers,
            QuadInstance instance
    ) {
        for (BakedQuad quad : quads) {
            int tintIndex = quad.materialInfo().tintIndex();
            instance.setColor(tintIndex >= 0 && tintIndex < tintLayers.length ? tintLayers[tintIndex] : -1);
            consumer.putBakedQuad(poseStack.last(), quad, instance);
        }
    }

    @Override
    public void submitCustomGeometry(
            PoseStack poseStack,
            RenderType renderType,
            CustomGeometryRenderer customGeometryRenderer
    ) {
        VertexConsumer consumer = BlockBuilderRenderTypes.consumerFor(blockBuilder, renderType);
        if (consumer == EmptyVertexConsumer.INSTANCE) return;

        customGeometryRenderer.render(poseStack.last(), consumer);
    }

    // Not part of static block geometry:
    @Override public void submitShadow(PoseStack poseStack, float radius, List<ShadowPiece> pieces) {}
    @Override public void submitNameTag(PoseStack poseStack, @Nullable Vec3 attachment, int offset, Component name, boolean seeThrough, int lightCoords, CameraRenderState camera) {}
    @Override public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor) {}
    @Override public void submitTextBackground(PoseStack poseStack, float x0, float y0, float x1, float y1, int color, DisplayMode displayMode, int lightCoords) {}
    @Override public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {}
    @Override public void submitLeash(PoseStack poseStack, LeashState leashState) {}
    @Override public <S> void submitCrumblingOverlay(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, CrumblingOverlay crumblingOverlay) {}
    @Override public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState, int outlineColor) {}
    @Override public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, int progress, boolean isBlockTranslucent) {}
    @Override public void submitShapeOutline(PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width, boolean afterTerrain) {}
    @Override public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, ItemQuads quads, FoilType foilType) {}
    @Override public void submitQuadParticleGroup(QuadParticleRenderState particles) {}
    @Override public void submitGizmoPrimitives(Group group, CameraRenderState camera, boolean onTop) {}
}
