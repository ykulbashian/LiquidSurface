package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleGroup;
import com.google.fpl.liquidfunpaint.physics.actions.SolidShape;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleEraser;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 15-09-19.
 */
public class LiquidSurfaceView extends GLSurfaceView implements ILiquidWorld {

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

    private PhysicsLoop mPhysicsLoop;
    private WorldLock mWorldLock;
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
        
        mPhysicsLoop = PhysicsLoop.getInstance();
        mPhysicsLoop.init(context);
        mWorldLock = WorldLock.getInstance();

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        if (BuildConfig.DEBUG) {
            setDebugFlags(
                    GLSurfaceView.DEBUG_LOG_GL_CALLS |
                            GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        }

        setRenderer(mPhysicsLoop);

        mController = new RotatableController((Activity)context);
    }

    @Override
    public void resumePhysics() {
        mController.updateDownDirection((Activity) getContext());
        mPhysicsLoop.startSimulation();
        mController.onResume();
    }

    @Override
    public void createSolidShape(SolidShape solidShape) {
        mWorldLock.addPhysicsCommand(solidShape);
    }

    @Override
    public void eraseParticles(ParticleEraser eraserShape) {
        mWorldLock.addPhysicsCommand(eraserShape);
    }

    @Override
    public void createParticles(ParticleGroup liquidShape) {
        mWorldLock.addPhysicsCommand(liquidShape);
    }

    @Override
    public void pausePhysics(){
        mPhysicsLoop.pauseSimulation();
        mController.onPause();
    }

    @Override
    public void clearAll() {
        mWorldLock.clearPhysicsCommands();
        mPhysicsLoop.reset();
    }
}
