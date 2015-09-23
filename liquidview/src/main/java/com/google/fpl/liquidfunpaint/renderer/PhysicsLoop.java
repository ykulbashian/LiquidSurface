/**
 * Copyright (c) 2014 Google, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.google.fpl.liquidfunpaint.renderer;

import com.google.fpl.liquidfunpaint.LiquidWorld;
import com.google.fpl.liquidfunpaint.SolidWorld;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.shader.ShaderProgram;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.Observable;
import com.mycardboarddreams.liquidsurface.BuildConfig;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * PhysicsLoop class. Contains the game update and render loop.
 *
 * This also contains the pointer to the LiquidFun world. The convention for
 * thread-safety is to called acquireWorld to obtain a thread-safe world
 * pointer, and releaseWorld when you are done with the object.
 */
public class PhysicsLoop extends Observable<Float> implements DrawableLayer {
    // Private constants
    private static final PhysicsLoop _instance = new PhysicsLoop();
    public static final boolean DEBUG_DRAW = true;

    private static final String TAG = "PhysicsLoop";
    private static final int ONE_SEC = 1000000000;

    // Public static constants; variables for reuse
    public static final float MAT4X4_IDENTITY[];

    // Public constants; records render states
    public int sScreenWidth = 1;
    public int sScreenHeight = 1;

    /// Member variables
    private Context mContext = null;

    // PhysicsLoop class owns all Box2D objects, for thread-safety
    // Variables for thread synchronization
    private volatile boolean mSimulation = false;

    LiquidWorld mLiquidWorld;
    SolidWorld mSolidWorld;

    final private Queue<Runnable> pendingRunnables = new ConcurrentLinkedQueue<>();

    protected DebugRenderer mDebugRenderer = null;

    // Measure the frame rate
    long totalFrames = -10000;
    private int mFrames;
    private long mStartTime;
    private long mTime;

    static {
        MAT4X4_IDENTITY = new float[16];
        Matrix.setIdentityM(MAT4X4_IDENTITY, 0);
    }


    @Override
    protected void finalize() {
        WorldLock.getInstance().deleteWorld();
    }

    private PhysicsLoop() {
    }

    public static PhysicsLoop getInstance() {
        return _instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;

        mLiquidWorld = LiquidWorld.getInstance();
        mLiquidWorld.init(context);
        mSolidWorld = SolidWorld.getInstance();
        mSolidWorld.init(mContext);

        if (DEBUG_DRAW) {
            mDebugRenderer = new DebugRenderer();
            mDebugRenderer.init(context);
        }

        reset();

        startSimulation();
    }

    /**
     * Resets the world -- which means a delete and a new.
     * Initializes the boundaries and reset the ParticleRenderer as well.
     */
    @Override
    public void reset() {
        clearPhysicsCommands();
        WorldLock.getInstance().resetWorld();
        mLiquidWorld.reset();
        mSolidWorld.reset();

        if (DEBUG_DRAW) {
            mDebugRenderer.reset();
            WorldLock.getInstance().setDebugDraw(mDebugRenderer);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSimulation) {

            while (!pendingRunnables.isEmpty()) {
                pendingRunnables.poll().run();
            }

            setChanged();
            notifyObservers();

            GLES20.glClearColor(1, 1, 1, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Draw particles
            showFrameRate();

            mLiquidWorld.onDrawFrame(gl);

            mSolidWorld.onDrawFrame(gl);

            if (DEBUG_DRAW) {
                mDebugRenderer.onDrawFrame(gl);
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        sScreenWidth = width;
        sScreenHeight = height;

        GLES20.glViewport(0, 0, width, height);

        mLiquidWorld.onSurfaceChanged(gl, width, height);
        mSolidWorld.onSurfaceChanged(gl, width, height);

        if (DEBUG_DRAW) {
            mDebugRenderer.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!WorldLock.getInstance().hasWorld()) {
            throw new IllegalStateException("Init world before rendering");
        }

        ShaderProgram.loadAllShaders(mContext.getAssets());

        TextureRenderer.getInstance().onSurfaceCreated();

        mLiquidWorld.onSurfaceCreated(gl, config);
        mSolidWorld.onSurfaceCreated(gl, config);

        if (DEBUG_DRAW) {
            mDebugRenderer.onSurfaceCreated(gl, config);
        }
    }

    public void pauseSimulation() {
        mSimulation = false;
    }

    public void startSimulation() {
        mSimulation = true;
    }

    public void addPhysicsCommand(Runnable runnable){
        pendingRunnables.add(runnable);
    }

    public void clearPhysicsCommands(){
        pendingRunnables.clear();
    }


    void showFrameRate() {
        if (BuildConfig.DEBUG) {
            long time = System.nanoTime();
            if (time - mTime > ONE_SEC) {
                if (totalFrames < 0) {
                    totalFrames = 0;
                    mStartTime = time - 1;
                }
                final float fps = mFrames / ((float) time - mTime) * ONE_SEC;
                float avefps = totalFrames / ((float) time - mStartTime) * ONE_SEC;
                final int count = ParticleSystems.getInstance().getParticleCount();
                Log.d(TAG, fps + " fps (Now)");
                Log.d(TAG, avefps + " fps (Average)");
                Log.d(TAG, count + " particles");
                mTime = time;
                mFrames = 0;

            }
            mFrames++;
            totalFrames++;
        }
    }
}