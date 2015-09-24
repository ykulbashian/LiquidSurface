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

import com.google.fpl.liquidfunpaint.physics.DrawableParticleSystem;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.shader.Material;
import com.google.fpl.liquidfunpaint.shader.ParticleMaterial;
import com.google.fpl.liquidfunpaint.shader.WaterParticleMaterial;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.google.fpl.liquidfunpaint.util.RenderHelper;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import org.json.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer to draw particle water, objects, and wall. It draws particles as
 * fluid (or objects) by following three steps:
 * 1) Draws particles to a texture
 * 2) Blurs it out
 * 3) Applies threshold.
 * This only executes on the GLSurfaceView thread.
 */
public class ParticleRenderer implements DrawableLayer {
    private static final String TAG = "PtlRenderer";
    public static final String JSON_FILE = "materials/particlerenderer.json";

    // Framebuffer for the particles to render on.
    public static final int FB_SIZE = 256;

    private WaterParticleMaterial mWaterParticleMaterial;
    private ParticleMaterial mParticleMaterial;
    private BlurRenderer mBlurRenderer;
    private ScreenRenderer mWaterScreenRenderer;
    private ScreenRenderer mScreenRenderer;

    private final RenderSurface[] mRenderSurface = new RenderSurface[2];
    private final float[] mTransformFromTexture = new float[16];
    private final float[] mPerspectiveTransform = new float[16];

    private Context mContext;

    private String materialFile;

    @Override
    public void init(Context context) {
        mContext = context.getApplicationContext();

        // Read in our specific json file
        materialFile = FileHelper.loadAsset(mContext.getAssets(), JSON_FILE);
    }

    /**
     * This should only execute on the GLSurfaceView thread.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        for(DrawableParticleSystem dps : ParticleSystems.getInstance().values())
            drawParticleSystemToScreen(dps);
    }

    private void drawParticleSystemToScreen(DrawableParticleSystem dps) {
        dps.onDrawFrame();

        // Draw the particles
        drawParticles(dps);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(
                0, 0,
                PhysicsLoop.getInstance().sScreenWidth,
                PhysicsLoop.getInstance().sScreenHeight);

        // Copy the water particles to screen
        mWaterScreenRenderer.draw(mTransformFromTexture);

        // Copy the other particles to screen
        mScreenRenderer.draw(mTransformFromTexture);
    }

    private void drawParticles(DrawableParticleSystem dps) {
        drawWaterParticles(dps);
        drawNonWaterParticles(dps);
    }

    /**
     * Draw all the water particles, and save all the other particle groups
     * into a list. We draw these to temp mRenderSurface[0].
     * @param dps
     */
    private void drawWaterParticles(DrawableParticleSystem dps) {
        // Draw all water particles to temp render surface 0
        mRenderSurface[0].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        dps.renderWaterParticles(mWaterParticleMaterial, mPerspectiveTransform);

        mRenderSurface[0].endRender();

        mBlurRenderer.draw(mRenderSurface[0].getTexture(), mRenderSurface[0]);
    }

    /**
     * Draw all saved ParticleGroups to temp mRenderSurface[1].
     * @param dps
     */
    private void drawNonWaterParticles(DrawableParticleSystem dps) {
        // Draw all non-water particles to temp render surface 1
        mRenderSurface[1].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        dps.renderNonWaterParticles(mParticleMaterial, mPerspectiveTransform);

        mRenderSurface[1].endRender();

        mBlurRenderer.draw(mRenderSurface[1].getTexture(), mRenderSurface[1]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        RenderHelper.createTransformMatrix(mPerspectiveTransform, mTransformFromTexture, height, width);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Create the render surfaces
        for (int i = 0; i < mRenderSurface.length; i++) {
            mRenderSurface[i] = new RenderSurface(FB_SIZE, FB_SIZE);
            mRenderSurface[i].setClearColor(Color.argb(0, 255, 255, 255));
        }

        // Create the blur renderer
        mBlurRenderer = new BlurRenderer();

        try {
            JSONObject json = new JSONObject(materialFile);

            initializeWaterParticleMaterial(json);

            initializeNonWaterParticleMaterial(json);

            // Scrolling texture when we copy water particles from FBO to screen
            mWaterScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("waterParticleToScreen"),
                    mRenderSurface[0].getTexture());

            // Scrolling texture when we copy water particles from FBO to screen
            mScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("otherParticleToScreen"),
                    mRenderSurface[1].getTexture());

        } catch (JSONException ex) {
            Log.e(TAG, "Cannot parse " + JSON_FILE + "\n" + ex.getMessage());
        }
    }

    private void initializeWaterParticleMaterial(JSONObject json) throws JSONException {
        // Water particle material. We are utilizing the position and color
        // buffers returned from LiquidFun directly.
        mWaterParticleMaterial = new WaterParticleMaterial(
                mContext, json.getJSONObject("waterParticlePointSprite"));

        // Initialize attributes specific to this material
        mWaterParticleMaterial.addAttribute(
                "aPosition", 2, Material.AttrComponentType.FLOAT,
                4, false, 0);
        mWaterParticleMaterial.addAttribute(
                "aVelocity", 2, Material.AttrComponentType.FLOAT,
                4, false, 0);
        mWaterParticleMaterial.addAttribute(
                "aColor", 4, Material.AttrComponentType.UNSIGNED_BYTE,
                1, true, 0);
        mWaterParticleMaterial.addAttribute(
                "aWeight", 1, Material.AttrComponentType.FLOAT,
                1, false, 0);
        mWaterParticleMaterial.setBlendFunc(
                Material.BlendFactor.ONE,
                Material.BlendFactor.ONE_MINUS_SRC_ALPHA);
    }

    private void initializeNonWaterParticleMaterial(JSONObject json) throws JSONException {
        // Non-water particle material. We are utilizing the position and
        // color buffers returned from LiquidFun directly.
        mParticleMaterial = new ParticleMaterial(
                mContext, json.getJSONObject("otherParticlePointSprite"));

        // Initialize attributes specific to this material
        mParticleMaterial.addAttribute(
                "aPosition", 2, Material.AttrComponentType.FLOAT,
                4, false, 0);
        mParticleMaterial.addAttribute(
                "aColor", 4, Material.AttrComponentType.UNSIGNED_BYTE,
                1, true, 0);
        mParticleMaterial.setBlendFunc(
                Material.BlendFactor.ONE,
                Material.BlendFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void reset() {
        for(DrawableParticleSystem dps : ParticleSystems.getInstance().values())
            dps.reset();
    }

}