package com.google.fpl.liquidfunpaint;

import android.opengl.GLES20;

import com.google.fpl.liquidfun.Body;
import com.google.fpl.liquidfun.BodyDef;
import com.google.fpl.liquidfun.BodyType;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Vec2;
import com.google.fpl.liquidfun.World;

/**
 * Created by PC on 8/15/2015.
 */
public class SolidWorld {
    private Body mCircleBody = null;

    private Body mBoundaryBody = null;

    private static final float BOUNDARY_THICKNESS = 20.0f;

    private static SolidWorld sInstance = new SolidWorld();

    public static SolidWorld getInstance(){
        return sInstance;
    }


    public void createWorldBoundaries(World world, float worldWidth, float worldHeight){
        // clean up previous Body if exists
        if (mBoundaryBody != null) {
            world.destroyBody(mBoundaryBody);
        }

        // Create native objects
        BodyDef bodyDef = new BodyDef();
        PolygonShape boundaryPolygon = new PolygonShape();

        mBoundaryBody = world.createBody(bodyDef);

        // boundary definitions
        // top
        boundaryPolygon.setAsBox(
                worldWidth,
                BOUNDARY_THICKNESS,
                worldWidth / 2,
                worldHeight + BOUNDARY_THICKNESS,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // bottom
        boundaryPolygon.setAsBox(
                worldWidth,
                BOUNDARY_THICKNESS,
                worldWidth / 2,
                -BOUNDARY_THICKNESS,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // left
        boundaryPolygon.setAsBox(
                BOUNDARY_THICKNESS,
                worldHeight,
                -BOUNDARY_THICKNESS,
                worldHeight / 2,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // right
        boundaryPolygon.setAsBox(
                BOUNDARY_THICKNESS,
                worldHeight,
                worldWidth + BOUNDARY_THICKNESS,
                worldHeight / 2,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);

        // Clean up native objects
        bodyDef.delete();
        boundaryPolygon.delete();
    }

    public void createSolidObject(float[] vertices){
        World world = LiquidWorld.getInstance().acquireWorld();

        try {
            // clean up previous Body if exists
            if (mCircleBody != null) {
                world.destroyBody(mCircleBody);
            }

            // Create native objects
            BodyDef bodyDef = new BodyDef();
            PolygonShape boundaryPolygon = new PolygonShape();

            mCircleBody = world.createBody(bodyDef);
            mCircleBody.setType(BodyType.dynamicBody);

            boundaryPolygon.set(vertices, vertices.length / 2);
            mCircleBody.createFixture(boundaryPolygon, 0.1f);

            // Clean up native objects
            bodyDef.delete();
            boundaryPolygon.delete();
        } finally {
            LiquidWorld.getInstance().releaseWorld();
        }
    }

    public void draw(){
        Vec2 center = mCircleBody.getPosition();
    }

    public void delete(){

        if (mBoundaryBody != null) {
            mBoundaryBody.delete();
            mBoundaryBody = null;
        }
        if (mCircleBody != null) {
            mCircleBody.delete();
            mCircleBody = null;
        }
    }
}
