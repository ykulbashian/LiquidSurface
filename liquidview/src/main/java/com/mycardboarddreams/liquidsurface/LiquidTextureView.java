package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.google.fpl.liquidfunpaint.GroupOptions;
import com.google.fpl.liquidfunpaint.ILiquidWorld;
import com.google.fpl.liquidfunpaint.LiquidWorld;
import com.google.fpl.liquidfunpaint.ParticleSystems;
import com.google.fpl.liquidfunpaint.renderer.GameLoop;
import com.google.fpl.liquidfunpaint.SolidWorld;


/**
 * Created on 3/25/2015.
 */
public class LiquidTextureView extends TextureView implements ILiquidWorld {

    /**
     * Load the native libraries
     */
    static {
        try{
            System.loadLibrary("liquidfun");
            System.loadLibrary("liquidfun_jni");

        } catch (UnsatisfiedLinkError e) {
            // only ignore exception in non-android env. This is to aid Robolectric integration.
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) throw e;
        }
    }

    private LiquidRenderThread thread;

    private RotatableController mController;

    public LiquidTextureView(Context context) {
        super(context);
        initialize(context);
    }

    public LiquidTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public LiquidTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context){
        if(isInEditMode())
            return;

        Activity activity = (Activity)context;

        thread = new LiquidRenderThread(activity);
        setSurfaceTextureListener(thread);

        mController = new RotatableController(activity);
    }

    @Override
    public void resumePhysics() {
        mController.updateDownDirection((Activity) getContext());
        GameLoop.getInstance().startSimulation();
        mController.onResume();
        thread.setPaused(false);
    }

    @Override
    public void pausePhysics(){
        GameLoop.getInstance().pauseSimulation();
        mController.onPause();
        thread.setPaused(true);
    }

    @Override
    public void createLiquidShape(final float[] vertices){

        GameLoop.getInstance().addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().fillShape(normalizePositions(vertices), GroupOptions.LIQUID, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
            }
        });
    }

    @Override
    public void createSolidShape(final float[] vertices){
        GameLoop.getInstance().addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                SolidWorld.getInstance().createSolidObject(normalizePositions(vertices));
            }
        });
    }

    @Override
    public void eraseParticles(final float[] vertices){
        GameLoop.getInstance().addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().eraseParticles(normalizePositions(vertices));
            }
        });
    }

    private float getWidthRatio(){
        return LiquidWorld.getInstance().sRenderWorldWidth / getWidth();
    }

    private float getHeightRatio(){
        return LiquidWorld.getInstance().sRenderWorldHeight / getHeight();
    }

    private float[] normalizePositions(float[] originalVertices){
        float[] normalizedVerts = new float[originalVertices.length];

        for(int i = 0; i < originalVertices.length; i++){
            normalizedVerts[i] = originalVertices[i] * ((i % 2 == 0) ? getWidthRatio() : getHeightRatio());
        }

        return normalizedVerts;
    }


}
