package com.google.fpl.liquidfunpaint;

import android.app.Activity;
import android.graphics.Color;

import com.google.fpl.liquidfun.ParticleColor;
import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.util.Observable;

import java.util.HashMap;

/**
 * Created by PC on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, ParticleSystem> implements Observable.Observer<Float> {

    public static final int MAX_PARTICLE_COUNT = 5000;
    public static final float PARTICLE_RADIUS = 0.06f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.0f;


    private ParticleRenderer mParticleRenderer = new ParticleRenderer();

    protected static final Transform MAT_IDENTITY;

    static {
        MAT_IDENTITY = new Transform();
        MAT_IDENTITY.setIdentity();
    }

    public static final String DEFAULT_PARTICLE_SYSTEM = "default_particle_system";

    private static ParticleSystems sInstance = new ParticleSystems();

    public static ParticleSystems getInstance(){
        return sInstance;
    }

    public void reset(){
        mParticleRenderer.reset();

        World world = LiquidWorld.getInstance().acquireWorld();
        try {
            // Create a new particle system; we only use one.
            ParticleSystemDef psDef = new ParticleSystemDef();
            psDef.setRadius(PARTICLE_RADIUS);
            psDef.setRepulsiveStrength(PARTICLE_REPULSIVE_STRENGTH);
            psDef.setElasticStrength(2.0f);
            psDef.setDensity(0.5f);
            ParticleSystem particleSystem = world.createParticleSystem(psDef);
            particleSystem.setMaxParticleCount(MAX_PARTICLE_COUNT);

            put(DEFAULT_PARTICLE_SYSTEM, particleSystem);
            psDef.delete();
        } finally {
            LiquidWorld.getInstance().releaseWorld();
        }
    }

    public int getParticleCount(){
        int count = 0;
        for(ParticleSystem system : values()){
            count += system.getParticleCount();
        }
        return count;
    }


    public void fillShape(float[] normalizedVertices, GroupOptions options, String key){

        if (normalizedVertices == null || normalizedVertices.length == 0 || normalizedVertices.length % 2 != 0)
            return;

        final PolygonShape polygon = new PolygonShape();
        polygon.set(normalizedVertices, normalizedVertices.length / 2);

        ParticleColor pColor = new ParticleColor(
                (short) Color.red(options.color),
                (short)Color.green(options.color),
                (short)Color.blue(options.color),
                (short)Color.alpha(options.color));

        final ParticleGroupDef pgd = new ParticleGroupDef();
        pgd.setFlags(options.particleType);
        pgd.setGroupFlags(options.particleGroup);
        pgd.setLinearVelocity(options.velocity);
        pgd.setColor(pColor);
        pgd.setStrength(options.strength);

        pgd.setShape(polygon);

        ParticleSystem ps = LiquidWorld.getInstance().acquireParticleSystem(key);
        try {
            ps.destroyParticlesInShape(polygon, MAT_IDENTITY);

            ParticleGroup pGroup = ps.createParticleGroup(pgd);

        } finally {
            LiquidWorld.getInstance().releaseParticleSystem();
        }
        pgd.delete();
    }

    public void onSurfaceChanged(int width, int height){
        mParticleRenderer.onSurfaceChanged(width, height);
    }

    public void onSurfaceCreated(Activity activity){
        mParticleRenderer.onSurfaceCreated(activity);
    }

    @Override
    public void update(Observable<Float> observable, Float data) {
        mParticleRenderer.update(observable, data);
    }

    public void draw(int width, int height) {
        mParticleRenderer.draw(width, height);
    }
}
