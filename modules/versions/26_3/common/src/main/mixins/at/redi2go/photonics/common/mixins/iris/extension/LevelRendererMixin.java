package at.redi2go.photonics.common.mixins.iris.extension;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.iris.IrisManager;
import at.redi2go.photonics.core.iris.rendering.PhotonicsPipeline;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    // 26.3 replaced renderLevel(...) with render(...); same head hook point as Iris' own pipeline setup.
    @Inject(
            method = "render",
            at = @At("HEAD"),
            order = 900
    )
    public void render(
            GraphicsResourceAllocator graphicsResourceAllocator,
            boolean renderOutline,
            CameraRenderState cameraState,
            GpuBufferSlice terrainFog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            boolean consistentDepthRequired,
            CallbackInfo ci
    ) {
        IrisManager.onFrameBegin();
    }
}
