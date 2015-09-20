package com.mycardboarddreams.liquidsurface;

import android.view.View;

/**
 * Created on 15-09-19.
 */
public interface ILiquidWorld {

    void pausePhysics();
    void resumePhysics();

    void createSolidShape(float[] vertices);
    void eraseParticles(float[] vertices);
    void createLiquidShape(float[] vertices);

    void setOnTouchListener(View.OnTouchListener listener);
}
