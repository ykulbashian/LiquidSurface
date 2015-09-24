package com.mycardboarddreams.liquidsurface;

import android.view.View;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-19.
 */
public interface ILiquidWorld {

    void pausePhysics();
    void resumePhysics();

    void createSolidShape(final Vector2f[] vertices);

    void eraseParticles(final Vector2f[] vertices);
    void eraseParticles(final Vector2f[] vertices, final String particleSystem);

    void createLiquidShape(final Vector2f[] vertices);
    void createLiquidShape(final Vector2f[] vertices, final LiquidPaint options);
    void createLiquidShape(final Vector2f[] vertices, final String particleSystem);
    void createLiquidShape(final Vector2f[] vertices, final LiquidPaint options, final String particleSystem);

    void clearAll();

    void setOnTouchListener(View.OnTouchListener listener);
}
