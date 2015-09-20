package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.google.fpl.liquidfunpaint.GroupOptions;
import com.google.fpl.liquidfunpaint.ILiquidWorld;
import com.google.fpl.liquidfunpaint.LiquidWorld;
import com.google.fpl.liquidfunpaint.ParticleSystems;
import com.google.fpl.liquidfunpaint.SolidWorld;
import com.google.fpl.liquidfunpaint.renderer.GameLoop;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by abc on 15-09-19.
 */
public class LiquidSurfaceView extends GLSurfaceView implements ILiquidWorld, GLSurfaceView.Renderer {

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

    private RotatableController mController;

    public LiquidSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    public LiquidSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    protected void initialize(Context context) {

        if(isInEditMode())
            return;

        GameLoop.getInstance().init((Activity) context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        if (BuildConfig.DEBUG) {
            setDebugFlags(
                    GLSurfaceView.DEBUG_LOG_GL_CALLS |
                            GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        }

        setRenderer(this);

        mController = new RotatableController((Activity)context);
    }

    @Override
    public void resumePhysics() {
        mController.updateDownDirection((Activity) getContext());
        GameLoop.getInstance().startSimulation();
        mController.onResume();
    }

    @Override
    public void pausePhysics(){
        GameLoop.getInstance().pauseSimulation();
        mController.onPause();
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GameLoop.getInstance().onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GameLoop.getInstance().onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GameLoop.getInstance().onDrawFrame(gl);
    }
}
