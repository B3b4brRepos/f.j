package at.redi2go.photonics.common.meshing.impl;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.common.mixins.meshing.OuterWrappedRenderTypeAccessor;
import at.redi2go.photonics.common.mixins.meshing.RenderSetupAccessor;
import at.redi2go.photonics.common.mixins.meshing.RenderTypeAccessor;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Resolves the vertex consumer for a render type by looking at the texture it samples. */
public final class BlockBuilderRenderTypes {
    private BlockBuilderRenderTypes() {}

    public static VertexConsumer consumerFor(BlockBuilder blockBuilder, RenderType renderType) {
        if (renderType instanceof OuterWrappedRenderType wrapped)
            renderType = ((OuterWrappedRenderTypeAccessor) wrapped).getWrapped();

        RenderSetup renderSetup = ((RenderTypeAccessor) renderType).getState();
        var textures = ((RenderSetupAccessor) (Object) renderSetup).getTextureSetup();
        var sampler0 = textures.get("Sampler0");

        if (sampler0 == null) return EmptyVertexConsumer.INSTANCE;

        blockBuilder.useAtlas((Id) (Object) sampler0.getLocation());
        return (VertexConsumer) blockBuilder;
    }
}
