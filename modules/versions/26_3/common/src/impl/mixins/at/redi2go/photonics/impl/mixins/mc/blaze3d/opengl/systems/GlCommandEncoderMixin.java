package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlCommandEncoderExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class GlCommandEncoderMixin implements GlCommandEncoderExt {
    @Shadow
    @Final
    private int drawFbo;

    @Override
    public int ph$drawFbo() {
        return drawFbo;
    }
}
