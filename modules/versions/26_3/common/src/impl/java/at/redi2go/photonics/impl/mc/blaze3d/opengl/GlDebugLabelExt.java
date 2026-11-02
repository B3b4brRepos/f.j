package at.redi2go.photonics.impl.mc.blaze3d.opengl;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.KHRDebug;

/**
 * Labels textures for GL debuggers. 26.3 moved the debug label helpers into the new backend,
 * so this talks to KHR_debug directly instead of mixing into them.
 */
public final class GlDebugLabelExt {
    private static final int GL_TEXTURE = 0x1702;

    private GlDebugLabelExt() {}

    public static void applyLabel(IGlTexture texture) {
        String label = texture.ph$label();
        if (label == null || !GL.getCapabilities().GL_KHR_debug) return;
        KHRDebug.glObjectLabel(GL_TEXTURE, texture.handle(), label.length() > 256 ? label.substring(0, 256) : label);
    }
}
