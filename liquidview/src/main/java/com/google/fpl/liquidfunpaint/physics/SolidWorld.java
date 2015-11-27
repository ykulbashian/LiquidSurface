package com.google.fpl.liquidfunpaint.physics;

import android.content.Context;
import android.graphics.PointF;
import android.text.TextUtils;

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
import com.google.fpl.liquidfunpaint.util.RenderHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/15/2015.
 */
public class SolidWorld implements DrawableLayer{

    private final List<Body> bodies = new ArrayList<>();
    private final Map<Body, PolygonShape> polygons = new HashMap<>();
    private final Map<Body, Texture> textures = new HashMap<>();

    private final float[] mPerspectiveTransform = new float[16];

    private static final float BOUNDARY_THICKNESS = 0.2f;

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
        float screenHeight = WorldLock.getInstance().sScreenWorldHeight;
        float screenWidth = WorldLock.getInstance().sScreenWorldWidth;

        float worldWidth = WorldLock.getInstance().sPhysicsWorldWidth;
        float worldHeight = WorldLock.getInstance().sPhysicsWorldHeight;

        float extraPadding = 0;
        float extraWidth = worldWidth + extraPadding;

        float midWidth = screenWidth/2;
        float midHeight = screenHeight/2;
        float left = -1*(worldWidth-screenWidth)/2;
        float right = screenWidth + (worldWidth-screenWidth)/2;
        float bottom = -1*(worldHeight-screenHeight)/2;
        float top = screenHeight + (worldHeight-screenHeight)/2;

        // boundary definitions
        // top
        Vector2f[] vTop = MathHelper.createBox(new Vector2f(midWidth, top), 2*extraWidth, BOUNDARY_THICKNESS);
        createSolidObject(vTop, BodyType.staticBody, null);

        // bottom
        vTop = MathHelper.createBox(new Vector2f(midWidth, bottom), 2*extraWidth, BOUNDARY_THICKNESS);
        createSolidObject(vTop, BodyType.staticBody, null);

        // left
        vTop = MathHelper.createBox(new Vector2f(left, midHeight), BOUNDARY_THICKNESS, worldHeight);
        createSolidObject(vTop, BodyType.staticBody, null);

        // right
        vTop = MathHelper.createBox(new Vector2f(right, midHeight), BOUNDARY_THICKNESS, worldHeight);
        createSolidObject(vTop, BodyType.staticBody, null);

    }

    public void createSolidObject(Vector2f[] vertices, BodyType type, String textureName){
        World world = WorldLock.getInstance().getWorld();

        // Create native objects
        BodyDef bodyDef = new BodyDef();
        PolygonShape boundaryPolygon = new PolygonShape();

        Body body = world.createBody(bodyDef);
        bodies.add(body);
        body.setType(type);

        boundaryPolygon.set(MathHelper.convertVectToFloats(vertices), vertices.length);
        body.createFixture(boundaryPolygon, 0.1f);

        // Clean up native objects
        bodyDef.delete();

        polygons.put(body, boundaryPolygon);

        if(!TextUtils.isEmpty(textureName)){
            Texture texture = new Texture(mContext, textureName);
            textures.put(body, texture);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        createWorldBoundaries();

        WorldLock.getInstance().perspectiveTransform(mPerspectiveTransform, 0);
    }

    public void onDrawFrame(GL10 gl){
        for(Body body : bodies) {
            if (body != null) {

                if(textures.containsKey(body)) {
                    PolygonShape shape = polygons.get(body);

                    Vector2f poly = MathHelper.getPolygonSize(shape);

                    Vec2 center = body.getWorldCenter();
                    TextureRenderer.getInstance().drawTexture(
                            textures.get(body),
                            WorldLock.getInstance().getSolidWorldTransform(),
                            (center.getX()) - poly.x / 2,
                            (center.getY()) + poly.y / 2,
                            (center.getX()) + poly.x / 2,
                            (center.getY()) - poly.y / 2,
                            PhysicsLoop.getInstance().sScreenWidth,
                            PhysicsLoop.getInstance().sScreenHeight);
                }
            }
        }
    }
    @Override
    public void reset(){

        for(Body body : bodies) {
            if (body != null) {
                polygons.get(body).delete();
                body.delete();
            }
        }

        bodies.clear();
    }

}
