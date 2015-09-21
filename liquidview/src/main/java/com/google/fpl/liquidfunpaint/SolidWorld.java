package com.google.fpl.liquidfunpaint;

import android.content.Context;

import com.google.fpl.liquidfun.Body;
import com.google.fpl.liquidfun.BodyDef;
import com.google.fpl.liquidfun.BodyType;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Vec2;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.renderer.TextureRenderer;
import com.google.fpl.liquidfunpaint.shader.Texture;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/15/2015.
 */
public class SolidWorld implements DrawableLayer{

    private final List<Body> bodies = new ArrayList<>();

    private Body mBoundaryBody = null;
    private Texture mBoatTexture;

    private static final float BOUNDARY_THICKNESS = 20.0f;
    private static final String TEXTURE_NAME = "textures/kayak.png";

    private static SolidWorld sInstance = new SolidWorld();

    public static SolidWorld getInstance(){
        return sInstance;
    }

    @Override
    public void init(Context context){
        mBoatTexture = new Texture(context, TEXTURE_NAME);
    }

    private void createWorldBoundaries(World world){
        float worldWidth = LiquidWorld.getInstance().sPhysicsWorldWidth;
        float worldHeight = LiquidWorld.getInstance().sPhysicsWorldHeight;

        float extraPadding = 1;
        float extraWidth = worldWidth + extraPadding;

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
                extraWidth,
                BOUNDARY_THICKNESS,
                worldWidth / 2,
                worldHeight + BOUNDARY_THICKNESS,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // bottom
        boundaryPolygon.setAsBox(
                extraWidth,
                BOUNDARY_THICKNESS,
                worldWidth / 2,
                -BOUNDARY_THICKNESS,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // left
        boundaryPolygon.setAsBox(
                BOUNDARY_THICKNESS,
                worldHeight,
                -BOUNDARY_THICKNESS - extraPadding,
                worldHeight / 2,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);
        // right
        boundaryPolygon.setAsBox(
                BOUNDARY_THICKNESS,
                worldHeight,
                worldWidth + BOUNDARY_THICKNESS + extraPadding,
                worldHeight / 2,
                0);
        mBoundaryBody.createFixture(boundaryPolygon, 0.0f);

        // Clean up native objects
        bodyDef.delete();
        boundaryPolygon.delete();
    }

    public void createSolidObject(Vector2f[] vertices){
        World world = LiquidWorld.getInstance().acquireWorld();

        try {
            Body body = null;
            // clean up previous Body if exists
            if (body != null) {
                world.destroyBody(body);
            }

            // Create native objects
            BodyDef bodyDef = new BodyDef();
            PolygonShape boundaryPolygon = new PolygonShape();

            body = world.createBody(bodyDef);
            bodies.add(body);
            body.setType(BodyType.dynamicBody);

            boundaryPolygon.set(MathHelper.convertVectToFloats(vertices), vertices.length);
            body.createFixture(boundaryPolygon, 0.1f);

            // Clean up native objects
            bodyDef.delete();
            boundaryPolygon.delete();
        } finally {
            LiquidWorld.getInstance().releaseWorld();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        World world = LiquidWorld.getInstance().acquireWorld();

        try {
            createWorldBoundaries(world);
        } finally {
            LiquidWorld.getInstance().releaseWorld();
        }
    }

    public void onDrawFrame(GL10 gl){
        for(Body body : bodies) {
            if (body != null) {
                Vec2 center = MathHelper.normalizePosition(body.getWorldCenter());
                TextureRenderer.getInstance().drawTexture(
                        mBoatTexture, PhysicsLoop.MAT4X4_IDENTITY,
                        (center.getX()) - 0.1f,
                        (center.getY()),
                        (center.getX()) + 0.1f,
                        (center.getY()) - 0.2f,
                        PhysicsLoop.getInstance().sScreenWidth,
                        PhysicsLoop.getInstance().sScreenHeight);
            }
        }
    }
    @Override
    public void reset(){

        if (mBoundaryBody != null) {
            mBoundaryBody.delete();
            mBoundaryBody = null;
        }

        for(Body body : bodies)
            if (body != null) {
                body.delete();
            }

        bodies.clear();
    }

    public void spinWheel(float direction){
        if(bodies.size() > 0 && bodies.get(0) != null){
            bodies.get(0).applyTorque(direction, true);
        }
    }

}
