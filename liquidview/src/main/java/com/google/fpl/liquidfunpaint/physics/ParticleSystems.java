package com.google.fpl.liquidfunpaint.physics;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.util.RenderHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, ParticleSystems.DrawableLayer> {

    public static final String DEFAULT_PARTICLE_SYSTEM = "default_particle_system";

    public static final int MAX_PARTICLE_COUNT = 5000;
    public static final float PARTICLE_RADIUS = 0.06f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.5f;

    private static ParticleSystems sInstance = new ParticleSystems();

    private final List<DrawableLayer> orderedList = new ArrayList<>();

    public static ParticleSystems getInstance(){
        return sInstance;
    }

    public void reset(World world){
        for(DrawableLayer system : values())
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

        DrawableParticleSystem newSystem = new DrawableParticleSystem(particleSystem);

        DrawableLayer layer = new DrawableLayer(newSystem, getNextParticleDistance());

        put(key, layer);
    }

    @Override
    public Collection<DrawableLayer> values() {
        return orderedList;
    }

    private void reorderList() {
        orderedList.clear();
        orderedList.addAll(super.values());
        Collections.sort(orderedList, new Comparator<DrawableLayer>() {
            @Override
            public int compare(DrawableLayer lhs, DrawableLayer rhs) {
                if(lhs.getDistance() > rhs.getDistance())
                    return -1;
                if(lhs.getDistance() == rhs.getDistance())
                    return 0;

                return 1;
            }
        });
    }

    @Override
    public DrawableLayer put(String key, DrawableLayer value) {
        super.put(key, value);
        reorderList();
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends DrawableLayer> map) {
        super.putAll(map);
        reorderList();
    }

    private float getNextParticleDistance(){
        return (size() + (float) Math.random())/2;
    }

    public int getParticleCount(){
        int count = 0;
        for(DrawableLayer system : values()){
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
    public DrawableLayer get(Object key) {
        if(containsKey(key))
            return super.get(key);
        else{
            World world = WorldLock.getInstance().getWorld();
            createParticleSystem(world, key.toString());
            return get(key);
        }

    }

    public DrawableLayer get(){
        return get(DEFAULT_PARTICLE_SYSTEM);
    }

    public static class DrawableLayer {
        public final DrawableDistance distanceD;
        public final DrawableParticleSystem particleSystem;

        public DrawableLayer(DrawableParticleSystem system, float distance){
            particleSystem = system;
            distanceD = new DrawableDistance(distance);
        }

        public int getParticleCount() {
            return particleSystem.getParticleCount();
        }

        public void delete() {
            particleSystem.delete();
        }

        public void clearParticles(Vector2f[] normalizedVertices) {
            particleSystem.clearParticles(normalizedVertices);
        }

        public float getDistance() {
            return distanceD.getDistance();
        }

        public void createParticleGroup(Vector2f[] normalizedVertices, LiquidPaint options) {
            particleSystem.createParticleGroup(normalizedVertices, options);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            distanceD.resetDimensions(width, height);
        }

        public void reset() {
            particleSystem.reset();
        }
    }

    public static class DrawableDistance {

        private float mDistance;

        public final float[] mPerspectiveTransform = new float[16];

        public DrawableDistance(float distance){
            setDistance(distance);
        }

        public void setDistance(float newDistance){
            mDistance = newDistance;

            resetDimensions(PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight);
        }

        public float getDistance(){
            return mDistance;
        }

        public void resetDimensions(float width, float height){
            RenderHelper.perspectiveParticleTransform(mPerspectiveTransform, width, height, mDistance);
        }
    }
}
