package com.google.fpl.liquidfunpaint;

import android.content.Context;
import android.util.Log;

import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.renderer.DebugRenderer;
import com.google.fpl.liquidfunpaint.renderer.ParticleRenderer;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.renderer.TextureRenderer;
import com.google.fpl.liquidfunpaint.shader.Texture;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.mycardboarddreams.liquidsurface.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/13/2015.
 */
public class LiquidWorld implements DrawableLayer {

    private static final float TIME_STEP = 1 / 60f; // 60 fps

    public static final float WORLD_SPAN = 3f;
    public float sPhysicsWorldWidth = WORLD_SPAN;
    public float sPhysicsWorldHeight = WORLD_SPAN;

    public float sRenderWorldWidth = WORLD_SPAN;
    public float sRenderWorldHeight = WORLD_SPAN;

    private static final String PAPER_MATERIAL_NAME = "paper";
    private static final String DIFFUSE_TEXTURE_NAME = "uDiffuseTexture";

    private Texture mPaperTexture;

    private static final String TAG = "PhysicsLoop";
    private static final int ONE_SEC = 1000000000;

    protected DebugRenderer mDebugRenderer = null;

    private ParticleRenderer mParticleRenderer;

    // Measure the frame rate
    long totalFrames = -10000;
    private int mFrames;
    private long mStartTime;
    private long mTime;

    private static LiquidWorld sInstance = new LiquidWorld();

    public static LiquidWorld getInstance(){
        return sInstance;
    }

    private Context mContext;

    @Override
    public void init(Context context){
        mContext = context.getApplicationContext();
        mParticleRenderer = new ParticleRenderer();
        mParticleRenderer.init(context);

        if (PhysicsLoop.DEBUG_DRAW) {
            mDebugRenderer = new DebugRenderer();
            mDebugRenderer.init(context);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){

        if(height < width) { //landscape
            sRenderWorldHeight = WORLD_SPAN;
            sRenderWorldWidth = width * WORLD_SPAN / height;
        } else { //portrait
            sRenderWorldHeight = height * WORLD_SPAN / width;
            sRenderWorldWidth = WORLD_SPAN;
        }

        sPhysicsWorldWidth = sRenderWorldWidth;
        sPhysicsWorldHeight = sRenderWorldHeight;

        if (PhysicsLoop.DEBUG_DRAW) {
            mDebugRenderer.onSurfaceChanged(gl, width, height);
        }

        mParticleRenderer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void reset(){

        WorldLock.getInstance().acquireWorld();
        try {
            WorldLock.getInstance().resetWorld();

            SolidWorld.getInstance().reset();
            ParticleSystems.getInstance().reset();

            mParticleRenderer.reset();

            if (PhysicsLoop.DEBUG_DRAW) {
                mDebugRenderer.reset();
                WorldLock.getInstance().setDebugDraw(mDebugRenderer);
            }

        } finally {
            WorldLock.getInstance().releaseWorld();
        }
    }

    void showFrameRate() {
        if (BuildConfig.DEBUG) {
            long time = System.nanoTime();
            if (time - mTime > ONE_SEC) {
                if (totalFrames < 0) {
                    totalFrames = 0;
                    mStartTime = time - 1;
                }
                final float fps = mFrames / ((float) time - mTime) * ONE_SEC;
                float avefps = totalFrames / ((float) time - mStartTime) * ONE_SEC;
                final int count = ParticleSystems.getInstance().getParticleCount();
                Log.d(TAG, fps + " fps (Now)");
                Log.d(TAG, avefps + " fps (Average)");
                Log.d(TAG, count + " particles");
                mTime = time;
                mFrames = 0;

            }
            mFrames++;
            totalFrames++;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createBackground(mContext);

        mParticleRenderer.onSurfaceCreated(gl, config);

        if (PhysicsLoop.DEBUG_DRAW) {
            mDebugRenderer.onSurfaceCreated(gl, config);
        }
    }

    private void createBackground(Context context) {
        // Read in our specific json file
        String materialFile = FileHelper.loadAsset(
                context.getAssets(), ParticleRenderer.JSON_FILE);
        try {
            JSONObject json = new JSONObject(materialFile);
            // Texture for paper
            JSONObject materialData = json.getJSONObject(PAPER_MATERIAL_NAME);
            String textureName = materialData.getString(DIFFUSE_TEXTURE_NAME);
            mPaperTexture = new Texture(context, textureName);
        }  catch (JSONException ex) {
            Log.e(TAG, "Cannot parse" + ParticleRenderer.JSON_FILE + "\n" + ex.getMessage());
        }
    }

    @Override
    public void onDrawFrame(GL10 gl){
        WorldLock.getInstance().lock();

        showFrameRate();

        WorldLock.getInstance().stepWorld(TIME_STEP);

        // Draw the paper texture.
        TextureRenderer.getInstance().drawTexture(
                mPaperTexture, PhysicsLoop.MAT4X4_IDENTITY, -1, 1, 1, -1,
                PhysicsLoop.getInstance().sScreenWidth,
                PhysicsLoop.getInstance().sScreenHeight);

        mParticleRenderer.onDrawFrame(gl);

        if (PhysicsLoop.DEBUG_DRAW) {
            mDebugRenderer.onDrawFrame(gl);
        }

        WorldLock.getInstance().unlock();
    }

}
