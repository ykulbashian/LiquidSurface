package com.google.fpl.liquidfunpaint;

import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.renderer.DebugRenderer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 15-09-20.
 */
public class WorldLock {

    // Parameters for world simulation
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final int PARTICLE_ITERATIONS = 5;

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
        mWorldLock.lock();
        return mWorld;
    }

    public void lock(){
        mWorldLock.lock();
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseWorld() {
        mWorldLock.unlock();
    }

    public void createWorld(){
        mWorld = new World(0, 0);
    }

    public void resetWorld(){
        deleteWorld();
        createWorld();
    }

    public void setDebugDraw(DebugRenderer renderer){
        mWorld.setDebugDraw(renderer);
    }

    public void deleteWorld() {
        World world = acquireWorld();

        try {

            SolidWorld.getInstance().reset();

            if (world != null) {
                world.delete();
                mWorld = null;
                ParticleSystems.getInstance().clear();
            }
        } finally {
            releaseWorld();
        }
    }

    void stepWorld(float dt){

        World world = acquireWorld();
        try {
            world.step(
                    dt, VELOCITY_ITERATIONS,
                    POSITION_ITERATIONS, PARTICLE_ITERATIONS);
        } finally {
            releaseWorld();
        }
    }

    public void unlock() {
        mWorldLock.unlock();
    }

    public void setGravity(float gravityX, float gravityY){

        World world = WorldLock.getInstance().acquireWorld();
        try {
            world.setGravity(
                    gravityX,
                    gravityY);

        } finally {
            WorldLock.getInstance().releaseWorld();
        }
    }
}
