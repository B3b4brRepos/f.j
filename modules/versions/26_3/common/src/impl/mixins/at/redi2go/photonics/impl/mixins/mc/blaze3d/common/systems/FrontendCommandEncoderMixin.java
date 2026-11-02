package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlCommandEncoderExt;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDeviceExt;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

/**
 * In 26.3 the command encoder the game hands out is the frontend wrapper; the GL specific
 * encoder/device sit behind it as backend objects.
 */
@Mixin(FrontendCommandEncoder.class)
public abstract class FrontendCommandEncoderMixin implements ICommandEncoder {
    @Shadow
    @Final
    private GpuDeviceBackend device;

    @Shadow
    @Final
    private CommandEncoderBackend backend;

    @Shadow
    public abstract boolean isInRenderPass();

    @Shadow
    public abstract void writeToBuffer(GpuBufferSlice slice, ByteBuffer data);

    @Shadow
    public abstract void copyToBuffer(GpuBufferSlice source, GpuBufferSlice destination);

    @Unique
    private void ph$checkNotInRenderPass() {
        if (isInRenderPass())
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
    }

    @Override
    public void ph$clearColorTexture(IGpuTexture<?> gpuTexture, Vector4fc clearColor) {
        ph$checkNotInRenderPass();

        int drawFbo = ((GlCommandEncoderExt) backend).ph$drawFbo();
        ((GlDeviceExt) device).ph$directStateAccess()
                .bindFrameBufferTextures(drawFbo, ((IGlTexture) gpuTexture).handle(), 0, 0, 36160);

        GL33C.glClearColor(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w());
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(15);
        GlStateManager._clear(16384);

        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void ph$writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer) {
        writeToBuffer(((GpuBuffer) buffer).slice(), byteBuffer);
    }

    @Override
    public void ph$writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer) {
        writeToBuffer((GpuBufferSlice) (Object) slice, byteBuffer);
    }

    @Override
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) (Object) ((GpuBuffer) buffer).map(readable, writeable);
    }

    @Override
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) (Object) ((GpuBufferSlice) (Object) bufferSlice).map(readable, writeable);
    }

    @Override
    public void ph$copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2) {
        copyToBuffer((GpuBufferSlice) (Object) slice1, (GpuBufferSlice) (Object) slice2);
    }

    @Override
    public void ph$writeToTexture(IGpuTexture2D texture, ByteBuffer data, Vector2ic offset, Vector2ic size) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void ph$writeToTexture(IGpuTexture3D texture, ByteBuffer data, Vector3ic offset, Vector3ic size) {
        throw new NotImplementedException("TODO");
    }
}
