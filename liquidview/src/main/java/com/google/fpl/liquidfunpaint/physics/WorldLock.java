package com.google.fpl.liquidfunpaint.physics;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.renderer.DebugRenderer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 15-09-20.
 */
public class WorldLock {

    public static final float WORLD_SPAN = 3f;
    public float sPhysicsWorldWidth = WORLD_SPAN;
    public float sPhysicsWorldHeight = WORLD_SPAN;

    public float sRenderWorldWidth = WORLD_SPAN;
    public float sRenderWorldHeight = WORLD_SPAN;

    // Parameters for world simulation
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final int PARTICLE_ITERATIONS = 5;

    public static final int MAX_PARTICLE_COUNT = 5000;
    public static final float PARTICLE_RADIUS = 0.06f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.5f;

    private World mWorld = null;
    private Lock mWorldLock = new ReentrantLock();

    private static WorldLock sInstance = new WorldLock();

    public static WorldLock getInstance() {
        return sInstance;
    }

    public boolean hasWorld() {
        return mWorld != null;
    }

    /**
     * Acquire the world for thread-safe operations.
     */
    public World acquireWorld() {
        lock();
        return mWorld;
    }

    public World getWorld(){
        return mWorld;
    }

    public void lock(){
        mWorldLock.lock();
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseWorld() {
        unlock();
    }

    public void createWorld(){
        mWorld = new World(0, 0);
    }

    public void resetWorld(){
        lock();

        try {
            deleteWorld();
            createWorld();

            ParticleSystems.getInstance().reset();
        } finally {
            unlock();
        }
    }

    public void setDebugDraw(DebugRenderer renderer){
        mWorld.setDebugDraw(renderer);
    }

    public void deleteWorld() {
        lock();

        try {
            if (mWorld != null) {
                mWorld.delete();
                mWorld = null;
            }

        } finally {
            unlock();
        }
    }

    public void setWorldDimensions(float width, float height){

        if(height < width) { //landscape
            sRenderWorldHeight = WORLD_SPAN;
            sRenderWorldWidth = width * WORLD_SPAN / height;
        } else { //portrait
            sRenderWorldHeight = height * WORLD_SPAN / width;
            sRenderWorldWidth = WORLD_SPAN;
        }

        sPhysicsWorldWidth = sRenderWorldWidth;
        sPhysicsWorldHeight = sRenderWorldHeight;

    }

    public void stepWorld(float dt){

        lock();
        try {
            mWorld.step(
                    dt, VELOCITY_ITERATIONS,
                    POSITION_ITERATIONS, PARTICLE_ITERATIONS);
        } finally {
            unlock();
        }
    }

    public void unlock() {
        mWorldLock.unlock();
    }

    public void setGravity(float gravityX, float gravityY){

       lock();
        try {
            mWorld.setGravity(
                    gravityX,
                    gravityY);

        } finally {
            unlock();
        }
    }

    /**
     * Acquire the particle system for thread-safe operations.
     * Uses the same lock as WorldLock, as all LiquidFun operations should be
     * synchronized. For example, if we are in the middle of WorldLock.sync(), we
     * don't want to call ParticleSystem.createParticleGroup() at the same
     * time.
     */

    public ParticleSystem createParticleSystem(){
        lock();

        try {
            // Create a new particle system; we only use one.
            ParticleSystemDef psDef = new ParticleSystemDef();
            psDef.setRadius(PARTICLE_RADIUS);
            psDef.setRepulsiveStrength(PARTICLE_REPULSIVE_STRENGTH);
            psDef.setElasticStrength(2.0f);
            psDef.setDensity(0.5f);
            ParticleSystem particleSystem = mWorld.createParticleSystem(psDef);
            particleSystem.setMaxParticleCount(MAX_PARTICLE_COUNT);

            psDef.delete();

            return particleSystem;
        } finally {
            unlock();
        }
    }
}
