package com.google.fpl.liquidfunpaint;

import android.graphics.Color;

import com.google.fpl.liquidfun.ParticleColor;
import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.HashMap;

/**
 * Created on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, DrawableParticleSystem> {

    ParticleGroup pGroup;

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
        for(DrawableParticleSystem system : values())
            system.delete();

        clear();
        createParticleSystem(DEFAULT_PARTICLE_SYSTEM);
    }

    private void createParticleSystem(String key) {
        ParticleSystem particleSystem = WorldLock.getInstance().createParticleSystem();
        put(key, new DrawableParticleSystem(particleSystem));
    }

    public int getParticleCount(){
        int count = 0;
        for(DrawableParticleSystem system : values()){
            count += system.getParticleCount();
        }
        return count;
    }


    public void fillShape(Vector2f[] normalizedVertices, LiquidPaint options, String key){

        if (normalizedVertices == null || normalizedVertices.length == 0 || normalizedVertices.length % 2 != 0)
            return;

        final PolygonShape polygon = new PolygonShape();
        float[] points = MathHelper.convertVectToFloats(normalizedVertices);
        polygon.set(points, normalizedVertices.length);

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

        WorldLock.getInstance().lock();
        ParticleSystem ps = get(key).particleSystem;
        try {
            ps.destroyParticlesInShape(polygon, MAT_IDENTITY);

            pGroup = ps.createParticleGroup(pgd);

        } finally {
            WorldLock.getInstance().unlock();
        }
        pgd.delete();
    }

    public void eraseParticles(Vector2f[] normalizedVertices){
        eraseParticles(normalizedVertices, DEFAULT_PARTICLE_SYSTEM);
    }

    public void eraseParticles(Vector2f[] normalizedVertices, String key){
        final PolygonShape polygon = new PolygonShape();
        float[] points = MathHelper.convertVectToFloats(normalizedVertices);

        polygon.set(points, normalizedVertices.length);

        WorldLock.getInstance().lock();
        ParticleSystem ps = get(key).particleSystem;
        try {
            ps.destroyParticlesInShape(polygon, MAT_IDENTITY);

        } finally {
            WorldLock.getInstance().unlock();
        }
    }

    @Override
    public DrawableParticleSystem get(Object key) {
        if(containsKey(key))
            return super.get(key);
        else{
            createParticleSystem(key.toString());
            return get(key);
        }

    }

    public DrawableParticleSystem get(){
        return get(DEFAULT_PARTICLE_SYSTEM);
    }
}
