package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.physics.ParticleSystems;
import com.google.fpl.liquidfunpaint.physics.WorldLock;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.physics.SolidWorld;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;


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

    private PhysicsLoop mPhysicsLoop;
    private WorldLock mWorldLock;
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

        mPhysicsLoop = PhysicsLoop.getInstance();
        mPhysicsLoop.init(activity);
        mWorldLock = WorldLock.getInstance();

        setSurfaceTextureListener(thread);

        mController = new RotatableController(activity);
    }

    @Override
    public void resumePhysics() {
        mController.updateDownDirection((Activity) getContext());
        mPhysicsLoop.startSimulation();
        mController.onResume();
        thread.setPaused(false);
    }

    @Override
    public void pausePhysics(){
        mPhysicsLoop.pauseSimulation();
        mController.onPause();
        thread.setPaused(true);
    }

    @Override
    public void createLiquidShape(final Vector2f[] vertices){
        createLiquidShape(vertices, LiquidPaint.LIQUID());
    }

    @Override
    public void createLiquidShape(final Vector2f[] vertices, final LiquidPaint paint){
        mWorldLock.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().fillShape(MathHelper.normalizePositions(vertices, getWidth(), getHeight()),
                        paint, ParticleSystems.DEFAULT_PARTICLE_SYSTEM);
            }
        });

    }

    @Override
    public void createLiquidShape(final Vector2f[] vertices, final String particleSystem) {
        createLiquidShape(vertices, LiquidPaint.LIQUID(), particleSystem);
    }

    @Override
    public void createLiquidShape(final Vector2f[] vertices, final LiquidPaint options, final String particleSystem) {
        mWorldLock.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().fillShape(MathHelper.normalizePositions(vertices, getWidth(), getHeight()),
                        options, particleSystem);
            }
        });
    }

    @Override
    public void createSolidShape(final Vector2f[] vertices){
        mWorldLock.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                SolidWorld.getInstance().createSolidObject(MathHelper.normalizePositions(vertices, getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void eraseParticles(final Vector2f[] vertices){
        mWorldLock.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().eraseParticles(MathHelper.normalizePositions(vertices, getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void eraseParticles(final Vector2f[] vertices, final String particleSystem) {
        mWorldLock.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                ParticleSystems.getInstance().eraseParticles(MathHelper.normalizePositions(vertices, getWidth(), getHeight()), particleSystem);
            }
        });
    }

    @Override
    public void clearAll() {
        mWorldLock.clearPhysicsCommands();
        mPhysicsLoop.reset();
    }

}
