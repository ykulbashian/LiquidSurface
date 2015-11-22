package com.google.fpl.liquidfunpaint.physics;

import android.opengl.GLES20;

import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.shader.ParticleMaterial;
import com.google.fpl.liquidfunpaint.shader.WaterParticleMaterial;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.RenderHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 15-09-20.
 */
public class DrawableParticleSystem {

    protected static final Transform MAT_IDENTITY;

    static {
        MAT_IDENTITY = new Transform();
        MAT_IDENTITY.setIdentity();
    }

    public final ParticleSystem particleSystem;

    private float mDistance;

    public ByteBuffer mParticleColorBuffer;
    public ByteBuffer mParticlePositionBuffer;
    public ByteBuffer mParticleVelocityBuffer;
    public ByteBuffer mParticleWeightBuffer;

    private final float[] mPerspectiveTransform = new float[16];

    public DrawableParticleSystem(ParticleSystem pSystem, float distance){
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

        setDistance(distance);

        resetDimensions(PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight, mDistance);
    }

    public void setDistance(float newDistance){
        mDistance = newDistance;
    }

    public float getDistance(){
        return mDistance;
    }

    private void resetDimensions(float width, float height, float distance){
        RenderHelper.perspectiveTransform(mPerspectiveTransform, width, height, distance);
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

    public void onSurfaceChanged(GL10 gl, int width, int height){
        resetDimensions(width, height, mDistance);
    }

    public void renderWaterParticles(WaterParticleMaterial mWaterParticleMaterial){

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
        ParticleGroup currGroup = particleSystem.getParticleGroupList();

        while (currGroup != null) {
            // Only draw water particles in this pass; queue other groups
            if (currGroup.getGroupFlags() == ParticleGroupFlag.particleGroupCanBeEmpty) {
                drawParticleGroup(currGroup);
            }

            currGroup = currGroup.getNext();
        }

        mWaterParticleMaterial.endRender();
    }


    public void renderNonWaterParticles(ParticleMaterial mParticleMaterial){

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
        ParticleGroup currGroup = particleSystem.getParticleGroupList();

        while (currGroup != null) {
            if (currGroup.getGroupFlags() != ParticleGroupFlag.particleGroupCanBeEmpty) {
                drawParticleGroup(currGroup);
            }

            currGroup = currGroup.getNext();
        }

        mParticleMaterial.endRender();
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
}
