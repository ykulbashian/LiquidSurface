package com.google.fpl.liquidfunpaint.physics;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, DrawableParticleSystem> {

    public static final String DEFAULT_PARTICLE_SYSTEM = "default_particle_system";

    public static final int MAX_PARTICLE_COUNT = 5000;
    public static final float PARTICLE_RADIUS = 0.06f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.5f;

    private static ParticleSystems sInstance = new ParticleSystems();

    private final List<DrawableParticleSystem> orderedList = new ArrayList<>();

    public static ParticleSystems getInstance(){
        return sInstance;
    }

    public void reset(World world){
        for(DrawableParticleSystem system : values())
            system.delete();

        clear();
        createParticleSystem(world, DEFAULT_PARTICLE_SYSTEM);
    }

    public void createParticleSystem(World world, String key) {
        ParticleSystemDef psDef = new ParticleSystemDef();
        psDef.setRadius(PARTICLE_RADIUS);
        psDef.setRepulsiveStrength(PARTICLE_REPULSIVE_STRENGTH);
        psDef.setElasticStrength(2.0f);
        psDef.setDensity(0.5f);
        ParticleSystem particleSystem = world.createParticleSystem(psDef);
        particleSystem.setMaxParticleCount(MAX_PARTICLE_COUNT);

        psDef.delete();

        put(key, new DrawableParticleSystem(particleSystem, getNextParticleDistance()));
    }

    @Override
    public Collection<DrawableParticleSystem> values() {
        return orderedList;
    }

    private void reorderList() {
        orderedList.clear();
        orderedList.addAll(super.values());
        Collections.sort(orderedList, new Comparator<DrawableParticleSystem>() {
            @Override
            public int compare(DrawableParticleSystem lhs, DrawableParticleSystem rhs) {
                if(lhs.getDistance() > rhs.getDistance())
                    return -1;
                if(lhs.getDistance() == rhs.getDistance())
                    return 0;

                return 1;
            }
        });
    }

    @Override
    public DrawableParticleSystem put(String key, DrawableParticleSystem value) {
        super.put(key, value);
        reorderList();
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends DrawableParticleSystem> map) {
        super.putAll(map);
        reorderList();
    }

    private float getNextParticleDistance(){
        return size() + (float) Math.random();
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
            World world = WorldLock.getInstance().getWorld();
            createParticleSystem(world, key.toString());
            return get(key);
        }

    }

    public DrawableParticleSystem get(){
        return get(DEFAULT_PARTICLE_SYSTEM);
    }
}
