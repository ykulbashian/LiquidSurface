package com.google.fpl.liquidfunpaint.physics;

import android.content.Context;
import android.opengl.Matrix;

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

    private final float[] mTransformFromWorld = new float[16];

    private Body mBoundaryBody = null;
    private Texture mSmileyTexture;

    private static final float BOUNDARY_THICKNESS = 20.0f;
    private static final String TEXTURE_NAME = "textures/smiley.png";

    private static SolidWorld sInstance = new SolidWorld();

    public static SolidWorld getInstance(){
        return sInstance;
    }
    private Context mContext;

    @Override
    public void init(Context context){
        mContext = context.getApplicationContext();
    }

    private void createWorldBoundaries(){
        World world = WorldLock.getInstance().getWorld();

        float worldWidth = WorldLock.getInstance().sPhysicsWorldWidth;
        float worldHeight = WorldLock.getInstance().sPhysicsWorldHeight;

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
        World world = WorldLock.getInstance().getWorld();

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
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSmileyTexture = new Texture(mContext, TEXTURE_NAME);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        createWorldBoundaries();

        Matrix.setIdentityM(mTransformFromWorld, 0);
        Matrix.translateM(mTransformFromWorld, 0, -1, -1, 0);
        Matrix.scaleM(
                mTransformFromWorld,
                0,
                2f / WorldLock.getInstance().sRenderWorldWidth,
                2f / WorldLock.getInstance().sRenderWorldHeight,
                1);
    }

    public void onDrawFrame(GL10 gl){
        for(Body body : bodies) {
            if (body != null) {
                Vec2 center = body.getWorldCenter();
                TextureRenderer.getInstance().drawTexture(
                        mSmileyTexture, mTransformFromWorld,
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

}
