# Photonics 26.3 port (untested)

Targets: MC 26.3, Fabric loader 0.19.5, Fabric API 0.162.0+26.3, Iris 1.11.7 + Sodium 0.9.2 (local jars in modules/versions/26_3/libs) (mc26.3).
Source: Redi2Go/PhotonicEngine `multi-version` branch + new module `modules/versions/26_3`.

## What changed vs the 1_21_11 module
Build: unobfuscated Loom (no mappings/remap), Java 25, Gradle 9.7.1, Loom 1.17.20.

GPU layer (blaze3d -> renderpearl):
- RenderSystem.getDevice()/createCommandEncoder() now return `FrontendGpuDevice`/`FrontendCommandEncoder`
  wrappers. Photonics' device/encoder mixins moved onto those; GL specifics (DirectStateAccess, drawFbo)
  are reached through small interfaces (`GlDeviceExt`, `GlCommandEncoderExt`) mixed into the
  package-private `GlDevice`/`GlCommandEncoder` (targeted by name).
- GpuSampler is an interface -> mixin now on `GlSampler`. GpuBuffer is an interface -> `GlBuffer`
  (extends BaseGpuBuffer). MappedView is now `GpuBufferSlice.MappedView`.
- Removed: GL debug-label mixins (labels done via KHR_debug directly), persistent-mapping hack
  (GlBufferHeap now writes through the persistent mapping of MAP_WRITE buffers), DirectStateAccessor,
  GlBufferAccessor.
Meshing: rewritten around `ModelBlockRenderer.tesselateBlock` + `BlockQuadOutput`, `FluidRenderer.Output`,
and a custom `SubmitNodeCollector` (BlockBuilderSubmitCollector) that captures block-entity models.
Removed FeatureRenderDispatcher/BufferSource mixins and the fake buffer sources.
Iris/Sodium mixins: LevelRenderer.render hook; GlProgram(Sodium) -> GlRenderPipeline.bind (MC);
CompositeRenderer unbind hook moved from RenderPass.drawIndexed to GlStateManager._drawElements.

## Verified how
A javac pass against the real 26.3 jar + Iris/Sodium 26.3 sources (sandbox lacked LWJGL/some libs, so
only those errors remain) and a script that checks every mixin target class/method name exists.
NOT verified: Gradle build, descriptors of string-based injects, any runtime behaviour.

## Known risks
- Block entity capture only handles models/custom geometry/block models; items, text, shadows ignored.
- Sodium 0.9 draws via MC pipelines: Photonics buffers are bound at GlRenderPipeline.bind().
- fabric.mod.json license says All-Rights-Reserved while the repo is LGPLv3 + extra terms.

Update: verified (class/method names, @At descriptors, ordinals) against the real Iris 1.11.7, Sodium 0.9.2
and Fabric API 0.162.0 release jars; the jars for Iris/Sodium are committed in modules/versions/26_3/libs.
