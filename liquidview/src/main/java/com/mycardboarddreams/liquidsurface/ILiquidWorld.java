package com.mycardboarddreams.liquidsurface;

import android.view.View;

import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-19.
 */
public interface ILiquidWorld {

    void pausePhysics();
    void resumePhysics();

    void createSolidShape(Vector2f[] vertices);
    void eraseParticles(Vector2f[] vertices);
    void createLiquidShape(Vector2f[] vertices);

    void setOnTouchListener(View.OnTouchListener listener);
}
