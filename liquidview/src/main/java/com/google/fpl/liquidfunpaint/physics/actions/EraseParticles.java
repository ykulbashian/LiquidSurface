package com.google.fpl.liquidfunpaint.physics.actions;

import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-26.
 */
public class EraseParticles implements PhysicsCommand {

    private final Vector2f[] mVertices;
    private final String mGroup;

    public EraseParticles(Vector2f[] vertices){
        this(vertices, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
    }

    public EraseParticles(Vector2f[] vertices, String particleGroup){
        mVertices = vertices;
        mGroup = particleGroup;
    }

    @Override
    public void run() {
        Vector2f[] normalized = MathHelper.normalizeVertices(mVertices, PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight);
        ParticleSystems.getInstance().eraseParticles(normalized, mGroup);
    }
}
