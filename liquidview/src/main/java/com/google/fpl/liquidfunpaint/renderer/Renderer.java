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
import com.google.fpl.liquidfunpaint.shader.ShaderProgram;
import com.google.fpl.liquidfunpaint.util.DrawableResponder;
import com.google.fpl.liquidfunpaint.util.Observable;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer class. Contains the game update and render loop.
 *
 * This also contains the pointer to the LiquidFun world. The convention for
 * thread-safety is to called acquireWorld to obtain a thread-safe world
 * pointer, and releaseWorld when you are done with the object.
 */
public class Renderer extends Observable<Float> implements DrawableResponder {
    // Private constants
    private static final Renderer _instance = new Renderer();
    public static final boolean DEBUG_DRAW = false;

    private static final float TIME_STEP = 1 / 60f; // 60 fps

    // Public static constants; variables for reuse
    public static final float MAT4X4_IDENTITY[];

    // Public constants; records render states
    public int sScreenWidth = 1;
    public int sScreenHeight = 1;

    /// Member variables
    private Activity mActivity = null;

    // Renderer class owns all Box2D objects, for thread-safety
    // Variables for thread synchronization
    private volatile boolean mSimulation = false;

    static {
        MAT4X4_IDENTITY = new float[16];
        Matrix.setIdentityM(MAT4X4_IDENTITY, 0);
    }


    @Override
    protected void finalize() {
        LiquidWorld.getInstance().deleteWorld();
    }

    private Renderer() {
    }

    public static Renderer getInstance() {
        return _instance;
    }

    public void init(Activity activity) {
        mActivity = activity;

        LiquidWorld.getInstance().init(activity);

        reset();

        startSimulation();
    }

    /**
     * Resets the world -- which means a delete and a new.
     * Initializes the boundaries and reset the ParticleRenderer as well.
     */
    @Override
    public void reset() {
        LiquidWorld.getInstance().reset();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSimulation) {
            setChanged();
            notifyObservers();

            LiquidWorld.getInstance().update(TIME_STEP);

            GLES20.glClearColor(1, 1, 1, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Draw particles
            LiquidWorld.getInstance().onDrawFrame(gl);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        sScreenWidth = width;
        sScreenHeight = height;

        GLES20.glViewport(0, 0, width, height);

        LiquidWorld.getInstance().onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!LiquidWorld.getInstance().hasWorld()) {
            throw new IllegalStateException("Init world before rendering");
        }

        ShaderProgram.loadAllShaders(mActivity.getAssets());

        TextureRenderer.getInstance().onSurfaceCreated();

        LiquidWorld.getInstance().onSurfaceCreated(mActivity, gl, config);
    }

    public void pauseSimulation() {
        mSimulation = false;
    }

    public void startSimulation() {
        mSimulation = true;
    }

}