package com.mycardboarddreams.liquidsurface;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.TextureView;

import com.google.fpl.liquidfun.Body;
import com.google.fpl.liquidfun.BodyDef;
import com.google.fpl.liquidfun.ParticleColor;
import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfun.Vec2;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.Renderer;


/**
 * Created by yervant on 3/25/2015.
 */
public class LiquidTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private LiquidRenderThread thread;

    private RotatableController mController;

    protected static final Transform MAT_IDENTITY;

    static {
        MAT_IDENTITY = new Transform();
        MAT_IDENTITY.setIdentity();
    }

    private Handler mainHandler;
    private int timeBetweenCalls = 500;

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

        initializeParticleSimulation(activity);

        setSurfaceTextureListener(this);

        setOpaque(false);

        mainHandler = new Handler(context.getMainLooper());

        thread = new LiquidRenderThread();
        mController = new RotatableController((Activity) getContext());
    }

    private void initializeParticleSimulation(Activity activity) {

        Renderer.getInstance().init(activity);
        Renderer.getInstance().startSimulation();
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



    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        float framesPerSec = (float) getResources().getInteger(R.integer.target_fps);
        thread.startThread(surface, width, height, framesPerSec);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        thread.setDimensions(width, height);
        Renderer.getInstance().onSurfaceChanged(thread.getGL(), width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        thread.stopThread();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void createLiquidShape(final float[] vertices, final int color){
        createLiquidShape(vertices, color, 1);
    }

    public void createLiquidShape(final float[] vertices, final int color, final int numTimes){

        thread.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < numTimes; i++) {

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            fillShape(vertices, color);
                        }
                    };
                    mainHandler.postDelayed(myRunnable, i * timeBetweenCalls);
                }
            }
        });
    }



    public void createSolidShape(final float[] vertices){
        thread.addPhysicsCommand(new Runnable() {
            @Override
            public void run() {
                World world = Renderer.getInstance().acquireWorld();

                try{
                    BodyDef bodyDef = new BodyDef();
                    Body boundaryBody = world.createBody(bodyDef);
                    PolygonShape boundaryPolygon = new PolygonShape();

                    float[] normalizedVertices = normalizePositions(vertices);

                    boundaryPolygon.set(normalizedVertices, normalizedVertices.length / 2);
                    boundaryBody.createFixture(boundaryPolygon, 0.0f);

                    bodyDef.delete();
                    boundaryPolygon.delete();

                } finally {
                    Renderer.getInstance().releaseWorld();
                }

            }
        });
    }

    private float getWidthRatio(){
        return Renderer.getInstance().sRenderWorldWidth / getWidth();
    }

    private float getHeightRatio(){
        return Renderer.getInstance().sRenderWorldWidth / getWidth();
    }

    private float[] normalizePositions(float[] originalVertices){
        float[] normalizedVerts = new float[originalVertices.length];

        for(int i = 0; i < originalVertices.length; i++){
            normalizedVerts[i] = originalVertices[i] * ((i % 2 == 0) ? getWidthRatio() : getHeightRatio());
        }

        return normalizedVerts;
    }

    public void clearAllLiquid() {
        mainHandler.removeCallbacksAndMessages(null);
        Renderer.getInstance().reset();
        thread.clearPhysicsCommands();
    }

    public void fillShape(float[] vertices, int color){

        if (vertices == null || vertices.length == 0 || vertices.length % 2 != 0)
            return;

        float[] normalizedVertices = normalizePositions(vertices);

        final PolygonShape polygon = new PolygonShape();
        polygon.set(normalizedVertices, normalizedVertices.length / 2);

        ParticleColor pColor = getColor(color);

        Vec2 mVelocity = new Vec2(0, 0);

        final ParticleGroupDef pgd = new ParticleGroupDef();
        pgd.setFlags(ParticleFlag.waterParticle);
        pgd.setGroupFlags(ParticleGroupFlag.particleGroupCanBeEmpty);
        pgd.setLinearVelocity(mVelocity);
        pgd.setColor(pColor);
        pgd.setStrength(0.035f);

        pgd.setShape(polygon);

        ParticleSystem ps = Renderer.getInstance().acquireParticleSystem();
        try {
            ps.destroyParticlesInShape(polygon, MAT_IDENTITY);

            ParticleGroup pGroup = ps.createParticleGroup(pgd);

        } finally {
            Renderer.getInstance().releaseParticleSystem();
        }
        pgd.delete();
    }

    public static ParticleColor getColor (int color) {
        ParticleColor pColor = new ParticleColor();
        short a = (short) (color >> 24 & 0xFF);
        short r = (short) (color >> 16 & 0xFF);
        short g = (short) (color >> 8 & 0xFF);
        short b = (short) (color & 0xFF);
        pColor.set(r, g, b, a);

        return pColor;
    }

}
