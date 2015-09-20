package com.google.fpl.liquidfunpaint;

import android.content.Context;
import android.util.Log;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.renderer.DebugRenderer;
import com.google.fpl.liquidfunpaint.renderer.ParticleRenderer;
import com.google.fpl.liquidfunpaint.renderer.GameLoop;
import com.google.fpl.liquidfunpaint.renderer.TextureRenderer;
import com.google.fpl.liquidfunpaint.shader.Texture;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.mycardboarddreams.liquidsurface.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/13/2015.
 */
public class LiquidWorld implements DrawableLayer {
    private World mWorld = null;
    private Lock mWorldLock = new ReentrantLock();

    public static final float WORLD_SPAN = 3f;
    public float sPhysicsWorldWidth = WORLD_SPAN;
    public float sPhysicsWorldHeight = WORLD_SPAN;

    public float sRenderWorldWidth = WORLD_SPAN;
    public float sRenderWorldHeight = WORLD_SPAN;

    // Parameters for world simulation
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final int PARTICLE_ITERATIONS = 5;

    private static final String PAPER_MATERIAL_NAME = "paper";
    private static final String DIFFUSE_TEXTURE_NAME = "uDiffuseTexture";

    private Texture mPaperTexture;

    private static final String TAG = "GameLoop";
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
    public void init(Context activity){
        mContext = activity;
        mParticleRenderer = new ParticleRenderer();
        mParticleRenderer.init(activity);
        createDebugRenderer(activity);
    }

    private void createDebugRenderer(Context activity) {
        if (GameLoop.DEBUG_DRAW) {
            mDebugRenderer = new DebugRenderer();
            mDebugRenderer.init(activity);
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

        SolidWorld.getInstance().onSurfaceChanged(gl, width, height);

        if (GameLoop.DEBUG_DRAW) {
            mDebugRenderer.onSurfaceChanged(gl, width, height);
        }

        mParticleRenderer.onSurfaceChanged(gl, width, height);
    }

    public boolean hasWorld(){
        return mWorld != null;
    }

    @Override
    public void reset(){

        acquireWorld();
        try {
            deleteWorld();
            mWorld = new World(0, 0);
            createDebugRenderer(mContext);

            mParticleRenderer.reset();

            if (GameLoop.DEBUG_DRAW) {
                mWorld.setDebugDraw(mDebugRenderer);
            }

        } finally {
            releaseWorld();
        }
    }

    public void deleteWorld() {
        World world = acquireWorld();

        try {

            if (mDebugRenderer != null) {
                mDebugRenderer.delete();
                mDebugRenderer = null;
            }

            SolidWorld.getInstance().reset();

            if (world != null) {
                world.delete();
                mWorld = null;
                ParticleSystems.getInstance().clear();
            }
        } finally {
            releaseWorld();
        }
    }


    /**
     * Acquire the world for thread-safe operations.
     */
    public World acquireWorld() {
        mWorldLock.lock();
        return mWorld;
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseWorld() {
        mWorldLock.unlock();
    }

    /**
     * Acquire the particle system for thread-safe operations.
     * Uses the same lock as LiquidWorld, as all LiquidFun operations should be
     * synchronized. For example, if we are in the middle of LiquidWorld.sync(), we
     * don't want to call ParticleSystem.createParticleGroup() at the same
     * time.
     */
    public ParticleSystem acquireParticleSystem(String key) {
        mWorldLock.lock();
        return ParticleSystems.getInstance().get(key);
    }

    public ParticleSystem acquireParticleSystem() {
        return acquireParticleSystem(ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
    }

    /**
     * Release the world after thread-safe operations.
     */
    public void releaseParticleSystem() {
        mWorldLock.unlock();
    }


    void stepWorld(float dt){

        World world = acquireWorld();
        try {
            world.step(
                    dt, VELOCITY_ITERATIONS,
                    POSITION_ITERATIONS, PARTICLE_ITERATIONS);
        } finally {
            releaseWorld();
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

    public void update(Float data) {
        showFrameRate();

        stepWorld(data);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createBackground(mContext);

        mParticleRenderer.onSurfaceCreated(gl, config);

        if (GameLoop.DEBUG_DRAW) {
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
        acquireWorld();

        // Draw the paper texture.
        TextureRenderer.getInstance().drawTexture(
                mPaperTexture, GameLoop.MAT4X4_IDENTITY, -1, 1, 1, -1,
                GameLoop.getInstance().sScreenWidth,
                GameLoop.getInstance().sScreenHeight);

        mParticleRenderer.onDrawFrame(gl);

        if (GameLoop.DEBUG_DRAW) {
            mDebugRenderer.onDrawFrame(gl);
        }

        releaseWorld();
    }

}
