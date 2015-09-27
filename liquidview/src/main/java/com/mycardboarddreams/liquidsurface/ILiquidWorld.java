package com.mycardboarddreams.liquidsurface;

import android.view.View;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleEraser;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleGroup;
import com.google.fpl.liquidfunpaint.physics.actions.PhysicsCommand;
import com.google.fpl.liquidfunpaint.physics.actions.SolidShape;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-19.
 */
public interface ILiquidWorld {

    void pausePhysics();
    void resumePhysics();

    void createSolidShape(SolidShape solidShape);
    void eraseParticles(ParticleEraser eraserShape);
    void createParticles(ParticleGroup liquidShape);

    void clearAll();

    void setOnTouchListener(View.OnTouchListener listener);
}
