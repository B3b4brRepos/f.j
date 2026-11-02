package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDeviceExt;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** GlDevice is package-private in 26.3, so it is targeted by name. */
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlDevice")
public abstract class GlDeviceMixin implements GlDeviceExt {
    @Shadow
    public abstract DirectStateAccess directStateAccess();

    @Override
    public DirectStateAccess ph$directStateAccess() {
        return directStateAccess();
    }
}
