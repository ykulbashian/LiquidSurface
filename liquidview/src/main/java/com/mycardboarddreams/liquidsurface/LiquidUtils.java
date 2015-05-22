package com.mycardboarddreams.liquidsurface;

import com.google.fpl.liquidfun.ParticleColor;

/**
 * Created by PC on 5/23/2015.
 */
public class LiquidUtils {

    public static ParticleColor getColor (int color) {
        ParticleColor pColor = new ParticleColor();
        short a = (short) (color >> 24 & 0xFF);
        short r = (short) (color >> 16 & 0xFF);
        short g = (short) (color >> 8 & 0xFF);
        short b = (short) (color & 0xFF);
        pColor.set(r, g, b, a);

        return pColor;
    }
}
