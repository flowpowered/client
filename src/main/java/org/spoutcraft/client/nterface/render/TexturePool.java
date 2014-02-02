package org.spoutcraft.client.nterface.render;

import org.spout.renderer.api.gl.Texture;
import org.spout.renderer.api.gl.Texture.InternalFormat;

/**
 *
 */
public class TexturePool {
    public Texture request(int width, int height, InternalFormat format) {
        return null;
    }

    /**
     * Checks if a candidate format can be used instead of the desired format. -1 is returned when that's not possible. Else, 0 is returned for a perfect match. The larger the value, the less optimal
     * the match.
     *
     * @param desired The desired format
     * @param candidate The candidate format
     * @return -1 if the candidate format doesn't match at all, 0 if the match is perfect or larger as the matches get less optimal
     */
    private static float checkMatch(InternalFormat desired, InternalFormat candidate) {
        if (candidate.getComponentCount() < desired.getComponentCount()
                || desired.hasRed() && !candidate.hasRed()
                || desired.hasGreen() && !candidate.hasGreen()
                || desired.hasBlue() && !candidate.hasBlue()
                || desired.hasAlpha() && !candidate.hasAlpha()
                || desired.hasDepth() && !candidate.hasDepth()
                || desired.isFloatBased() && !candidate.isFloatBased()) {
            return -1;
        }
        float match = 0;
        if (candidate.hasRed() && !desired.hasRed()) {
            match++;
        }
        if (candidate.hasGreen() && !desired.hasGreen()) {
            match++;
        }
        if (candidate.hasBlue() && !desired.hasBlue()) {
            match++;
        }
        if (candidate.hasAlpha() && !desired.hasAlpha()) {
            match++;
        }
        if (candidate.hasDepth() && !desired.hasDepth()) {
            match++;
        }
        if (candidate.isFloatBased() && !desired.isFloatBased()) {
            match++;
        }
        final float byteRatio = candidate.getBytesPerComponent() / (float) desired.getBytesPerComponent();
        if (byteRatio < 1) {
            return -1;
        } else {
            return match + byteRatio - 1;
        }
    }
}
