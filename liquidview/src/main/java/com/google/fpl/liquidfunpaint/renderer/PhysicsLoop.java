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

import com.google.fpl.liquidfunpaint.physics.SolidWorld;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.shader.ShaderProgram;
import com.google.fpl.liquidfunpaint.shader.Texture;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.google.fpl.liquidfunpaint.util.Observable;
import com.mycardboarddreams.liquidsurface.BuildConfig;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String PAPER_MATERIAL_NAME = "paper";
    private static final String DIFFUSE_TEXTURE_NAME = "uDiffuseTexture";

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


    private ParticleRenderer mParticleRenderer;
    private SolidWorld mSolidWorld;

    WorldLock mWorldLock;

    private Texture mPaperTexture;

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

    private PhysicsLoop() {
        mWorldLock = WorldLock.getInstance();
    }

    public static PhysicsLoop getInstance() {
        return _instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;

        mParticleRenderer = new ParticleRenderer();
        mParticleRenderer.init(context);
        mSolidWorld = SolidWorld.getInstance();
        mSolidWorld.init(context);

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

        mWorldLock.lock();
        try {
            mWorldLock.resetWorld();

            mParticleRenderer.reset();
            mSolidWorld.reset();

            if (DEBUG_DRAW) {
                mDebugRenderer.reset();
                mWorldLock.setDebugDraw(mDebugRenderer);
            }

        } finally {
            mWorldLock.unlock();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSimulation) {

            setChanged();
            notifyObservers();

            GLES20.glClearColor(1, 1, 1, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Draw particles
            showFrameRate();

            mWorldLock.lock();

            try {

                drawBackgroundTexture();

                mWorldLock.stepWorld();

                mParticleRenderer.onDrawFrame(gl);

                mSolidWorld.onDrawFrame(gl);

                if (DEBUG_DRAW) {
                    mDebugRenderer.onDrawFrame(gl);
                }
            } finally {
                mWorldLock.unlock();
            }

        }
    }

    private void drawBackgroundTexture() {
        TextureRenderer.getInstance().drawTexture(
                mPaperTexture, PhysicsLoop.MAT4X4_IDENTITY, -1, 1, 1, -1,
                sScreenWidth,
                sScreenHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        sScreenWidth = width;
        sScreenHeight = height;

        GLES20.glViewport(0, 0, width, height);

        mWorldLock.lock();

        try {
            mWorldLock.setWorldDimensions(width, height);

            mParticleRenderer.onSurfaceChanged(gl, width, height);
            mSolidWorld.onSurfaceChanged(gl, width, height);

            if (DEBUG_DRAW) {
                mDebugRenderer.onSurfaceChanged(gl, width, height);
            }
        } finally {
            mWorldLock.unlock();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        ShaderProgram.loadAllShaders(mContext.getAssets());

        TextureRenderer.getInstance().onSurfaceCreated();

        mWorldLock.lock();

        try {
            createBackground(mContext);

            mParticleRenderer.onSurfaceCreated(gl, config);
            mSolidWorld.onSurfaceCreated(gl, config);

            if (DEBUG_DRAW) {
                mDebugRenderer.onSurfaceCreated(gl, config);
            }
        } finally {
            mWorldLock.unlock();
        }
    }

    public void pauseSimulation() {
        mSimulation = false;
    }

    public void startSimulation() {
        mSimulation = true;
    }


    private void createBackground(Context context) {
        // Read in our specific json file
        String materialFile = FileHelper.loadAsset(
                context.getAssets(), ParticleRenderer.JSON_FILE);
        try {
            JSONObject json = new JSONObject(materialFile);
            // Texture for paper
            JSONObject materialData = json.getJSONObject(PAPER_MATERIAL_NAME);
            String textureName = materialData.getString(DIFFUSE_TEXTURE_NAME);
            mPaperTexture = new Texture(context, textureName);
        }  catch (JSONException ex) {
            Log.e(TAG, "Cannot parse" + ParticleRenderer.JSON_FILE + "\n" + ex.getMessage());
        }
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