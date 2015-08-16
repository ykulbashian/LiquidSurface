package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfunpaint.GroupOptions;
import com.google.fpl.liquidfunpaint.LiquidWorld;
import com.google.fpl.liquidfunpaint.ParticleSystems;
import com.google.fpl.liquidfunpaint.Renderer;


/**
 * Created by yervant on 3/25/2015.
 */
public class LiquidTextureView extends TextureView {

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

        Renderer.getInstance().init(activity);
        Renderer.getInstance().startSimulation();

        setOpaque(false);

        thread = new LiquidRenderThread(activity);
        setSurfaceTextureListener(thread);

        mController = new RotatableController((Activity) getContext());
    }

    public void resumeParticles() {
        mController.updateDownDirection((Activity) getContext());
        Renderer.getInstance().startSimulation();
        mController.onResume();
        thread.setPaused(false);
    }

    public void pauseParticles(){
        Renderer.getInstance().pauseSimulation();
        mController.onPause();
        thread.setPaused(true);
    }

    public boolean isStarted(){
        return thread.running;
    }

    public void createLiquidShape(final float[] vertices, final int color){

        thread.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().fillShape(normalizePositions(vertices), GroupOptions.LIQUID, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
            }
        });
    }



    public void createSolidShape(final float[] vertices){
        thread.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().fillShape(normalizePositions(vertices), GroupOptions.SOLID, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);

            }
        });
    }

    private float getWidthRatio(){
        return LiquidWorld.getInstance().sRenderWorldWidth / getWidth();
    }

    private float getHeightRatio(){
        return LiquidWorld.getInstance().sRenderWorldWidth / getWidth();
    }

    private float[] normalizePositions(float[] originalVertices){
        float[] normalizedVerts = new float[originalVertices.length];

        for(int i = 0; i < originalVertices.length; i++){
            normalizedVerts[i] = originalVertices[i] * ((i % 2 == 0) ? getWidthRatio() : getHeightRatio());
        }

        return normalizedVerts;
    }


}
