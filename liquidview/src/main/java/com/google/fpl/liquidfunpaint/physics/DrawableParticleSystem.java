package com.google.fpl.liquidfunpaint.physics;

import android.graphics.Color;
import android.opengl.GLES20;

import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.renderer.BlurRenderer;
import com.google.fpl.liquidfunpaint.renderer.ParticleRenderer;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.renderer.RenderSurface;
import com.google.fpl.liquidfunpaint.renderer.ScreenRenderer;
import com.google.fpl.liquidfunpaint.shader.ParticleMaterial;
import com.google.fpl.liquidfunpaint.shader.WaterParticleMaterial;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems.DrawableDistance;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created on 15-09-20.
 */
public class DrawableParticleSystem {

    protected static final Transform MAT_IDENTITY;

    static {
        MAT_IDENTITY = new Transform();
        MAT_IDENTITY.setIdentity();
    }

    private static final int NUM_SLICES = 20;
    private static final float[] lateralDistances = new float[NUM_SLICES];
    private static final float WIDE_ANGLE = 23;
    private float frame = 0;
    static {
        for(int i = 0; i < lateralDistances.length; i++)
            lateralDistances[i] = (float) Math.sin((Math.random()*2*WIDE_ANGLE) - WIDE_ANGLE)*i/10;
    }


    public final ParticleSystem particleSystem;

    public ByteBuffer mParticleColorBuffer;
    public ByteBuffer mParticlePositionBuffer;
    public ByteBuffer mParticleVelocityBuffer;
    public ByteBuffer mParticleWeightBuffer;

    private ScreenRenderer mWaterScreenRenderer;
    private ScreenRenderer mScreenRenderer;

    public final RenderSurface[] mRenderSurface = new RenderSurface[2];

    public DrawableParticleSystem(ParticleSystem pSystem, JSONObject json){
        particleSystem = pSystem;

        mParticlePositionBuffer = ByteBuffer
                .allocateDirect(2 * 4 * ParticleSystems.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleVelocityBuffer = ByteBuffer
                .allocateDirect(2 * 4 * ParticleSystems.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleColorBuffer = ByteBuffer
                .allocateDirect(4 * ParticleSystems.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());
        mParticleWeightBuffer = ByteBuffer
                .allocateDirect(4 * ParticleSystems.MAX_PARTICLE_COUNT)
                .order(ByteOrder.nativeOrder());

        initializeRenderSurfaces(json);
    }

    public void initializeRenderSurfaces(JSONObject json) {

        for (int i = 0; i < mRenderSurface.length; i++) {
            mRenderSurface[i] = new RenderSurface(ParticleRenderer.FB_SIZE, ParticleRenderer.FB_SIZE);
            mRenderSurface[i].setClearColor(Color.argb(0, 255, 255, 255));
        }

        try {
            // Scrolling texture when we copy water particles from FBO to screen
            mWaterScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("waterParticleToScreen"),
                    mRenderSurface[0].getTexture());

            // Scrolling texture when we copy water particles from FBO to screen
            mScreenRenderer = new ScreenRenderer(
                    json.getJSONObject("otherParticleToScreen"),
                    mRenderSurface[1].getTexture());
        } catch (JSONException e){}

    }

    public int getParticleCount(){
        return particleSystem.getParticleCount();
    }

    public void createParticleGroup(Vector2f[] normalizedVertices, LiquidPaint options){

        if (normalizedVertices == null || normalizedVertices.length == 0 || normalizedVertices.length % 2 != 0)
            return;

        PolygonShape polygon = createPolygonShape(normalizedVertices);

        ParticleGroupDef pgd = options.createParticleGroupDef(polygon);

        particleSystem.destroyParticlesInShape(polygon, MAT_IDENTITY);

        particleSystem.createParticleGroup(pgd);

        pgd.delete();
    }

    public void clearParticles(Vector2f[] normalizedVertices) {
        final PolygonShape polygon = createPolygonShape(normalizedVertices);
        particleSystem.destroyParticlesInShape(polygon, MAT_IDENTITY);
    }

    private static PolygonShape createPolygonShape(Vector2f[] normalizedVertices) {
        final PolygonShape polygon = new PolygonShape();
        float[] points = MathHelper.convertVectToFloats(normalizedVertices);
        polygon.set(points, normalizedVertices.length);
        return polygon;
    }

    public void onDraw(WaterParticleMaterial waterMaterial, ParticleMaterial nonWater, BlurRenderer blurRenderer, DrawableDistance dist){
        resetBuffers();
        renderWaterParticles(waterMaterial, blurRenderer, dist);
        renderNonWaterParticles(nonWater, blurRenderer, dist);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(
                0, 0,
                PhysicsLoop.getInstance().sScreenWidth,
                PhysicsLoop.getInstance().sScreenHeight);

        frame += 0.01f;
        // Copy the water particles to screen
        for(int i = NUM_SLICES; i >= 0; i--){
            float distance = dist.getDistance() + i * 0.2f;
            float x = lateralDistances[i % lateralDistances.length] + (float) (lateralDistances[i % lateralDistances.length] * Math.sin(frame));
            mWaterScreenRenderer.draw(WorldLock.getInstance().getScreenTransform(x, distance*0.15f-0.5f, distance, 1 + distance*0.5f));
        }

        // Copy the other particles to screen
        mScreenRenderer.draw(WorldLock.getInstance().getScreenTransform(0, 0, 0));
    }

    public void resetBuffers(){

        mParticlePositionBuffer.rewind();
        mParticleColorBuffer.rewind();
        mParticleWeightBuffer.rewind();
        mParticleVelocityBuffer.rewind();

        int worldParticleCount = particleSystem.getParticleCount();
        // grab the most current particle buffers
        particleSystem.copyPositionBuffer(
                0, worldParticleCount, mParticlePositionBuffer);
        particleSystem.copyVelocityBuffer(
                0, worldParticleCount, mParticleVelocityBuffer);
        particleSystem.copyColorBuffer(
                0, worldParticleCount, mParticleColorBuffer);
        particleSystem.copyWeightBuffer(
                0, worldParticleCount, mParticleWeightBuffer);

        GLES20.glClearColor(0, 0, 0, 0);
    }

    public void renderWaterParticles(WaterParticleMaterial mWaterParticleMaterial, BlurRenderer blurRenderer, DrawableDistance distance){
        mRenderSurface[0].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        mWaterParticleMaterial.beginRender(WorldLock.getInstance().getCameraDistance());

        // Set attribute arrays
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aPosition", mParticlePositionBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aVelocity", mParticleVelocityBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aColor", mParticleColorBuffer, 0);
        mWaterParticleMaterial.setVertexAttributeBuffer(
                "aWeight", mParticleWeightBuffer, 0);

        float[] transform = WorldLock.getInstance().getParticleTransform(0);//distance.getDistance());
        // Set uniforms
        GLES20.glUniformMatrix4fv(
                mWaterParticleMaterial.getUniformLocation("uTransform"),
                1, false, transform, 0);

        // Go through each particle group
        ParticleGroup currGroup = particleSystem.getParticleGroupList();

        while (currGroup != null) {
            // Only draw water particles in this pass; queue other groups
            if (currGroup.getGroupFlags() == ParticleGroupFlag.particleGroupCanBeEmpty) {
                drawParticleGroup(currGroup);
            }

            currGroup = currGroup.getNext();
        }

        mWaterParticleMaterial.endRender();

        mRenderSurface[0].endRender();

        blurRenderer.draw(mRenderSurface[0].getTexture(), mRenderSurface[0]);
    }


    public void renderNonWaterParticles(ParticleMaterial mParticleMaterial, BlurRenderer blurRenderer, DrawableDistance distance){
        mRenderSurface[1].beginRender(GLES20.GL_COLOR_BUFFER_BIT);

        mParticleMaterial.beginRender();

        // Set attribute arrays
        mParticleMaterial.setVertexAttributeBuffer(
                "aPosition", mParticlePositionBuffer, 0);
        mParticleMaterial.setVertexAttributeBuffer(
                "aColor", mParticleColorBuffer, 0);

        float[] transform = WorldLock.getInstance().getParticleTransform(0);//distance.getDistance());
        // Set uniforms
        GLES20.glUniformMatrix4fv(
                mParticleMaterial.getUniformLocation("uTransform"),
                1, false, transform, 0);

        // Go through all the particleGroups in the render list
        ParticleGroup currGroup = particleSystem.getParticleGroupList();

        while (currGroup != null) {
            if (currGroup.getGroupFlags() != ParticleGroupFlag.particleGroupCanBeEmpty) {
                drawParticleGroup(currGroup);
            }

            currGroup = currGroup.getNext();
        }

        mParticleMaterial.endRender();

        mRenderSurface[1].endRender();

        blurRenderer.draw(mRenderSurface[1].getTexture(), mRenderSurface[1]);
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

    public void reset(){
        mParticlePositionBuffer.clear();
        mParticleColorBuffer.clear();
        mParticleWeightBuffer.clear();
        mParticleVelocityBuffer.clear();
    }

    public void delete(){
        particleSystem.delete();
    }

    public ParticleSystem getParticleSystem(){
        return particleSystem;
    }

}
