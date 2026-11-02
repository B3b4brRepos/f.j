package at.redi2go.photonics.impl.mc.blaze3d.opengl;

import com.mojang.renderpearl.backend.opengl.DirectStateAccess;

/** Implemented on the (package-private) GlDevice by a mixin. */
public interface GlDeviceExt {
    DirectStateAccess ph$directStateAccess();
}
