package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.buffers;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(GpuBufferSlice.MappedView.class)
public abstract class MappedViewMixin implements IGpuBuffer.MappedView {
    @Shadow
    public abstract ByteBuffer data();

    @Override
    public ByteBuffer ph$data() {
        return data();
    }
}
