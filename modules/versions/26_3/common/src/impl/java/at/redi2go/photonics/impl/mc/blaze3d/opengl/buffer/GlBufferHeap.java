package at.redi2go.photonics.impl.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.AbstractGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;


public class GlBufferHeap extends AbstractGpuBufferHeap {
    private final IGpuBuffer gpuBuffer;
    public final ByteBuffer buffer;


    private final Queue<Region> uploadQueue = new ConcurrentLinkedQueue<>();

    public GlBufferHeap(
            IGpuDevice device,
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    ) {
        this.gpuBuffer = device.ph$createBuffer(label, byteSize, usage | BufferUsage.MAP_WRITE);
        this.buffer = ByteBuffer.allocateDirect(Math.toIntExact(byteSize))
                .order(ByteOrder.nativeOrder());

    }

    public IGpuBuffer buffer() {
        return gpuBuffer;
    }

    @Override
    public long capacity() {
        return gpuBuffer.ph$size();
    }

    @Nullable
    @Override
    public MemoryView allocate(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new Allocation(ptr, byteSize);
    }

    @Nullable
    @Override
    public IGpuBufferHeap allocateHeap(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new SubHeap(this, ptr, byteSize);
    }

    @Override
    public void upload() {
        var regionsToUpload = Region.takeFrom(uploadQueue);
        if (regionsToUpload.isEmpty()) return;

        // 26.3 maps MAP_WRITE buffers persistently (and coherently) for their whole lifetime, so
        // we write through that mapping instead of mapping the range ourselves.
        try (var view = ((GpuBuffer) gpuBuffer).map(false, true)) {
            ByteBuffer target = view.data();
            for (var region : regionsToUpload) {
                int offset = (int) region.begin();
                int length = (int) region.end() - offset;
                target.put(offset, buffer, offset, length);
            }
        }
    }

    @Override
    public void close() {
        gpuBuffer.close();
    }

    private class Allocation extends AbstractAllocation {
        private final ByteBuffer slice;

        private Allocation(long begin, long length) {
            super(begin, length);

            slice = buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        public ByteBuffer buffer() {
            return slice;
        }

        @Override
        public void upload() {
            uploadQueue.add(this);
        }
    }

    private class SubHeap extends AbstractSubheap implements Region, IGpuBufferHeap {
        protected SubHeap(AbstractGpuBufferHeap root, long begin, long length) {
            super(root, begin, length);
        }

        @Override
        protected ByteBuffer createSlice(long begin, long length) {
            return buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        protected void uploadSlice(Region region) {
            uploadQueue.add(region);
        }

        @Override
        protected IGpuBufferHeap createHeap(long begin, long length) {
            return new SubHeap(GlBufferHeap.this, begin, length);
        }
    }
}