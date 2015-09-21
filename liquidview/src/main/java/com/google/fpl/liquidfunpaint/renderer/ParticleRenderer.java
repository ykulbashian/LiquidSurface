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

import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfunpaint.LiquidWorld;
import com.google.fpl.liquidfunpaint.ParticleSystems;
import com.google.fpl.liquidfunpaint.WorldLock;
import com.google.fpl.liquidfunpaint.shader.Material;
import com.google.fpl.liquidfunpaint.shader.ParticleMaterial;
import com.google.fpl.liquidfunpaint.shader.WaterParticleMaterial;
import com.google.fpl.liquidfunpaint.tool.Tool;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.google.fpl.liquidfunpaint.util.RenderHelper;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import org.json.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

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

    private ByteBuffer mParticleColorBuffer;
    private ByteBuffer mParticlePositionBuffer;
    private ByteBuffer mParticleVelocityBuffer;
    private ByteBuffer mParticleWeightBuffer;

    private List<ParticleGroup> mParticleRenderList =
            new ArrayList<>(256);

    private Context mContext;

    @Override
    public void init(Context context) {
        mContext = context.getApplicationContext();

        mParticlePositionBuffer = ByteBuffer
                .allocateDirect(2 * 4 * WorldLock.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleVelocityBuffer = ByteBuffer
                .allocateDirect(2 * 4 * WorldLock.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleColorBuffer = ByteBuffer
                .allocateDirect(4 * WorldLock.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleWeightBuffer = ByteBuffer
                .allocateDirect(4 * WorldLock.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
    }

    /**
     * This should only execute on the GLSurfaceView thread.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Per frame resets of buffers
        mParticlePositionBuffer.rewind();
        mParticleColorBuffer.rewind();
        mParticleWeightBuffer.rewind();
        mParticleVelocityBuffer.rewind();
        mParticleRenderList.clear();

        WorldLock.getInstance().lock();
        ParticleSystem ps = ParticleSystems.getInstance().get().particleSystem;
        try {
            int worldParticleCount = ps.getParticleCount();
            // grab the most current particle buffers
            ps.copyPositionBuffer(
                    0, worldParticleCount, mParticlePositionBuffer);
            ps.copyVelocityBuffer(
                    0, worldParticleCount, mParticleVelocityBuffer);
            ps.copyColorBuffer(
                    0, worldParticleCount, mParticleColorBuffer);
            ps.copyWeightBuffer(
                    0, worldParticleCount, mParticleWeightBuffer);

            GLES20.glClearColor(0, 0, 0, 0);

            // Draw the particles
            drawParticles();

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(
                    0, 0,
                    PhysicsLoop.getInstance().sScreenWidth,
                    PhysicsLoop.getInstance().sScreenHeight);

            // Copy the water particles to screen
            mWaterScreenRenderer.draw(mTransformFromTexture);

            // Copy the other particles to screen
            mScreenRenderer.draw(mTransformFromTexture);
        } finally {
            WorldLock.getInstance().unlock();
        }
    }

    private void drawParticles() {
        drawWaterParticles();
        drawNonWaterParticles();
    }

    /**
     * Issue the correct draw call for the ParticleGroup that is passed in.
     */
    private void drawParticleGroup(ParticleGroup pg) {
        // Get the buffer offsets
        int particleCount = pg.getParticleCount();
        int instanceOffset = pg.getBufferIndex();

        // Draw!
        GLES20.glDrawArrays(
                GLES20.GL_POINTS, instanceOffset, particleCount);
    }

    /**
     * Draw all the water particles, and save all the other particle groups
     * into a list. We draw these to temp mRenderSurface[0].
     */
    private void drawWaterParticles() {
        // Draw all water particles to temp render surface 0
        mRenderSurface[0].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        mWaterParticleMaterial.beginRender();

        // Set attribute arrays
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aPosition", mParticlePositionBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aVelocity", mParticleVelocityBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aColor", mParticleColorBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aWeight", mParticleWeightBuffer, 0);

        // Set uniforms
        GLES20.glUniformMatrix4fv(
                mWaterParticleMaterial.getUniformLocation("uTransform"),
                1, false, mPerspectiveTransform, 0);

        // Go through each particle group
        ParticleSystem ps = ParticleSystems.getInstance().get().particleSystem;

        ParticleGroup currGroup = ps.getParticleGroupList();

        while (currGroup != null) {
            // Only draw water particles in this pass; queue other groups
            if (currGroup.getGroupFlags() ==
                    Tool.getTool(Tool.ToolType.WATER).getParticleGroupFlags()) {
                drawParticleGroup(currGroup);
            } else {
                mParticleRenderList.add(currGroup);
            }

            currGroup = currGroup.getNext();
        }

        mWaterParticleMaterial.endRender();

        mRenderSurface[0].endRender();

        mBlurRenderer.draw(mRenderSurface[0].getTexture(), mRenderSurface[0]);
    }

    /**
     * Draw all saved ParticleGroups to temp mRenderSurface[1].
     */
    private void drawNonWaterParticles() {
        // Draw all non-water particles to temp render surface 1
        mRenderSurface[1].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        mParticleMaterial.beginRender();

        // Set attribute arrays
        mParticleMaterial.setVertexAttributeBuffer(
                "aPosition", mParticlePositionBuffer, 0);
        mParticleMaterial.setVertexAttributeBuffer(
                "aColor", mParticleColorBuffer, 0);

        // Set uniforms
        GLES20.glUniformMatrix4fv(
                mParticleMaterial.getUniformLocation("uTransform"),
                1, false, mPerspectiveTransform, 0);

        // Go through all the particleGroups in the render list
        for (ParticleGroup currGroup : mParticleRenderList) {
            drawParticleGroup(currGroup);
        }

        mParticleMaterial.endRender();

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

        // Read in our specific json file
        String materialFile = FileHelper.loadAsset(
                mContext.getAssets(), JSON_FILE);
        try {
            JSONObject json = new JSONObject(materialFile);

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

            // Scrolling texture when we copy water particles from FBO to screen
            mWaterScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("waterParticleToScreen"),
                    mRenderSurface[0].getTexture());

            // Scrolling texture when we copy water particles from FBO to screen
            mScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("otherParticleToScreen"),
                    mRenderSurface[1].getTexture());

        } catch (JSONException ex) {
            Log.e(TAG, "Cannot parse" + JSON_FILE + "\n" + ex.getMessage());
        }
    }

    @Override
    public void reset() {
        mParticlePositionBuffer.clear();
        mParticleColorBuffer.clear();
        mParticleWeightBuffer.clear();
        mParticleVelocityBuffer.clear();
    }

}