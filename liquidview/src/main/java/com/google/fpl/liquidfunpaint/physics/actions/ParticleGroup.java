package com.google.fpl.liquidfunpaint.physics.actions;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-26.
 */
public class ParticleGroup implements PhysicsCommand {

    private final Vector2f[] mVertices;
    private final LiquidPaint mPaint;
    private final String mGroup;

    public ParticleGroup(Vector2f[] vertices){
        this(vertices, LiquidPaint.LIQUID(), ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
    }

    public ParticleGroup(Vector2f[] vertices, LiquidPaint paint){
        this(vertices, paint, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);

    }

    public ParticleGroup(Vector2f[] vertices, String particleGroup){
        this(vertices, LiquidPaint.LIQUID(), particleGroup);

    }

    public ParticleGroup(Vector2f[] vertices, LiquidPaint paint, String particleGroup){
        mVertices = vertices;
        mPaint = paint;
        mGroup = particleGroup;
    }

    @Override
    public void run() {
        Vector2f[] normalized = MathHelper.normalizeVertices(mVertices, PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight);
        ParticleSystems.getInstance().fillShape(normalized, mPaint, mGroup);
    }
}
