package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.impl.mc.blaze3d.common.GpuDeviceImpl;
import at.redi2go.photonics.impl.mc.blaze3d.common.TextureFormats;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlTextureFormats;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.GlTexture2D;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.GlTexture3D;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * 26.3: RenderSystem.getDevice() returns the frontend device, which wraps the GL backend.
 * Photonics only ever creates plain OpenGL objects, so everything GL specific is done here.
 */
@Mixin(FrontendGpuDevice.class)
public abstract class FrontendGpuDeviceMixin implements GpuDeviceImpl, IGpuDevice {
    @Shadow
    public abstract CommandEncoder createCommandEncoder();

    @Shadow
    public abstract GpuSampler createSampler(
            AddressMode addressModeU,
            AddressMode addressModeV,
            FilterMode minFilter,
            FilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    );

    @Shadow
    public abstract GpuBuffer createBuffer(Supplier<String> label, int usage, long size);

    @Override
    public ICommandEncoder ph$createCommandEncoder() {
        return (ICommandEncoder) createCommandEncoder();
    }

    @Override
    public IGpuSampler ph$createSampler(
            IAddressMode addressModeU,
            IAddressMode addressModeV,
            IFilterMode minFilter,
            IFilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    ) {
        return (IGpuSampler) createSampler(
                (AddressMode) (Object) addressModeU,
                (AddressMode) (Object) addressModeV,
                (FilterMode) (Object) minFilter,
                (FilterMode) (Object) magFilter,
                maxAnisotropy,
                maxLod
        );
    }

    @Override
    public IGpuTexture2D ph$createTexture2D(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            int width, int height,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture2D(label, usage, textureFormat, new Vector2i(width, height), mipLevels);
    }

    @Override
    public IGpuTexture3D ph$createTexture3D(
            @Nullable Supplier<String> label,
            int usage, ITextureFormat textureFormat,
            int width, int height, int depth,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture3D(label, usage, textureFormat, new Vector3i(width, height, depth), mipLevels);
    }

    @Override
    public IGpuBuffer ph$createBuffer(@Nullable Supplier<String> label, long byteSize, int usage) {
        return (IGpuBuffer) createBuffer(label, usage, byteSize);
    }

    @Override
    public IGpuBufferHeap ph$createBufferHeap(@Nullable Supplier<String> label, long byteSize, int usage) {
        return new GlBufferHeap(this, label, byteSize, usage);
    }

    @Override
    public TextureFormats getTextureFormats() {
        return GlTextureFormats.INSTANCE;
    }
}
