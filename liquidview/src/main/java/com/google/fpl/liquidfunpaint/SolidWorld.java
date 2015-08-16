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

    private static SolidWorld sInstance = new SolidWorld();

    public static SolidWorld getInstance(){
        return sInstance;
    }

    public void createSolidObject(World world, float[] vertices){
        // clean up previous Body if exists
        if (mCircleBody != null) {
            world.destroyBody(mCircleBody);
        }

        // Create native objects
        BodyDef bodyDef = new BodyDef();
        PolygonShape boundaryPolygon = new PolygonShape();

        mCircleBody = world.createBody(bodyDef);
        mCircleBody.setType(BodyType.dynamicBody);

        boundaryPolygon.set(vertices, vertices.length/2);
        mCircleBody.createFixture(boundaryPolygon, 0.1f);

        // Clean up native objects
        bodyDef.delete();
        boundaryPolygon.delete();
    }

    public void draw(){
        Vec2 center = mCircleBody.getPosition();
    }
}
