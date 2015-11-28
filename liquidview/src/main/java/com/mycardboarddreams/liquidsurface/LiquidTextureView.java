package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleEraser;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleGroup;
import com.google.fpl.liquidfunpaint.physics.actions.SolidShape;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;


/**
 * Created on 3/25/2015.
 */
public class LiquidTextureView extends GLTextureView implements ILiquidWorld {

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

    private GestureInterpreter mGestureInterpreter;

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

        mPhysicsLoop = PhysicsLoop.getInstance();
        mPhysicsLoop.init(activity);
        mWorldLock = WorldLock.getInstance();

        setRenderer(mPhysicsLoop);

        mController = new RotatableController(activity);

        mGestureInterpreter = new GestureInterpreter(context);
    }

    @Override
    public void resumePhysics() {
        mPhysicsLoop.startSimulation();
        mController.onResume((Activity) getContext());
        setPaused(false);
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
        setPaused(true);
    }

    @Override
    public void clearAll() {
        mWorldLock.clearPhysicsCommands();
        mPhysicsLoop.reset();
    }

    @Override
    public void translateCamera(float x, float y, float z){
        mWorldLock.translateCamera(x, y, z);
    }

    @Override
    public void addGestureListener(GestureInterpreter.GestureListener detector) {
        mGestureInterpreter.setGestureListener(detector);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mGestureInterpreter.onTouchEvent(event)){
            return true;
        }
        return super.onTouchEvent(event);
    }
}
