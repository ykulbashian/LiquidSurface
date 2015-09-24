package com.google.fpl.liquidfunpaint.physics;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Transform;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.HashMap;

/**
 * Created on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, DrawableParticleSystem> {

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
        get(key).createParticleGroup(normalizedVertices, options);
    }

    public void eraseParticles(Vector2f[] normalizedVertices){
        eraseParticles(normalizedVertices, DEFAULT_PARTICLE_SYSTEM);
    }

    public void eraseParticles(Vector2f[] normalizedVertices, String key){
        get(key).clearParticles(normalizedVertices);
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
