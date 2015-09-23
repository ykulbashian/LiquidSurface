package com.google.fpl.liquidfunpaint;

import android.content.Context;
import android.util.Log;

import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.renderer.DebugRenderer;
import com.google.fpl.liquidfunpaint.renderer.ParticleRenderer;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.renderer.TextureRenderer;
import com.google.fpl.liquidfunpaint.shader.Texture;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;

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

    private static final String TAG = "LiquidWorld";

    private ParticleRenderer mParticleRenderer;

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

        mParticleRenderer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void reset(){

        WorldLock worldLock = WorldLock.getInstance();

        worldLock.lock();
        try {

            mParticleRenderer.reset();

        } finally {
            worldLock.releaseWorld();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createBackground(mContext);

        mParticleRenderer.onSurfaceCreated(gl, config);
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

        WorldLock.getInstance().stepWorld(TIME_STEP);

        // Draw the paper texture.
        TextureRenderer.getInstance().drawTexture(
                mPaperTexture, PhysicsLoop.MAT4X4_IDENTITY, -1, 1, 1, -1,
                PhysicsLoop.getInstance().sScreenWidth,
                PhysicsLoop.getInstance().sScreenHeight);

        mParticleRenderer.onDrawFrame(gl);

        WorldLock.getInstance().unlock();
    }

}
