package at.redi2go.photonics.common.mixins.iris.pipeline.passes;

import at.redi2go.photonics.common.iris.IrisUtil;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sodium 0.9 no longer has its own GlProgram; terrain is drawn through the game's pipelines, so the
 * Photonics buffers are bound whenever a pipeline (and with it its program) gets bound.
 */
@Mixin(GlRenderPipeline.class)
public abstract class GlRenderPipelineMixin {
    @Shadow
    @Final
    private GlProgram program;

    @Inject(method = "bind", at = @At("TAIL"))
    private void photonics$bindBuffers(CallbackInfo ci) {
        IrisUtil.bindBuffers(program.getProgramId());
    }
}
