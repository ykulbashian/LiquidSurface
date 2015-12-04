package com.google.fpl.liquidfunpaint.physics;

import android.content.Context;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.World;
import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.renderer.BlurRenderer;
import com.google.fpl.liquidfunpaint.shader.ParticleMaterial;
import com.google.fpl.liquidfunpaint.shader.WaterParticleMaterial;
import com.google.fpl.liquidfunpaint.util.DrawableLayer;
import com.google.fpl.liquidfunpaint.util.FileHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, DrawableParticleSystem> implements DrawableLayer {

    public static final String DEFAULT_PARTICLE_SYSTEM = "default_particle_system";

    public static final int MAX_PARTICLE_COUNT = 5000;
    public static final float PARTICLE_RADIUS = 0.06f;
    public static final float PARTICLE_REPULSIVE_STRENGTH = 0.5f;
    public static final String JSON_FILE = "materials/particlerenderer.json";

    private JSONObject json;

    private static ParticleSystems sInstance = new ParticleSystems();

    private final List<DrawableDistance> orderedDistanceList = new ArrayList<>();

    public static ParticleSystems getInstance(){
        return sInstance;
    }

    public void reset(World world){
        for(DrawableParticleSystem system : values())
            system.delete();

        clear();
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

        DrawableParticleSystem newSystem = new DrawableParticleSystem(particleSystem, json);

        put(key, newSystem);
        addNewLayer(newSystem, getNextParticleDistance());
    }

    private void addNewLayer(DrawableParticleSystem system, float distance){
        DrawableDistance layer = new DrawableDistance(distance, system);
        orderedDistanceList.add(layer);
        reorderList();
    }

    @Override
    public void clear() {
        super.clear();
        orderedDistanceList.clear();
    }

    private void reorderList() {
        Collections.sort(orderedDistanceList, new Comparator<DrawableDistance>() {
            @Override
            public int compare(DrawableDistance lhs, DrawableDistance rhs) {
                if (lhs.getDistance() > rhs.getDistance())
                    return -1;
                if (lhs.getDistance() == rhs.getDistance())
                    return 0;

                return 1;
            }
        });
    }

    private float getNextParticleDistance(){
        return (8*orderedDistanceList.size() + (float) Math.random())/4;
    }

    public int getParticleCount(){
        int count = 0;
        for(DrawableParticleSystem system : values()){
            count += system.getParticleCount();
        }
        return count;
    }

    @Override
    public void reset(){
        for(DrawableParticleSystem dps : values())
            dps.reset();
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

    public List<DrawableDistance> getDistances(){
        return orderedDistanceList;
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    public DrawableParticleSystem get(){
        return get(DEFAULT_PARTICLE_SYSTEM);
    }

    @Override
    public void init(Context context) {

        // Read in our specific json file
        String materialFile = FileHelper.loadAsset(context.getAssets(), JSON_FILE);
        try {
            json = new JSONObject(materialFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        for(DrawableParticleSystem system : values()){
            system.initializeRenderSurfaces(json);
        }

    }

    public static class DrawableDistance {

        private float mDistance;

        public final DrawableParticleSystem particleSystem;

        public DrawableDistance(float distance, DrawableParticleSystem system){
            particleSystem = system;
            setDistance(distance);
        }

        public void setDistance(float newDistance){
            mDistance = newDistance;
        }

        public float getDistance(){
            return mDistance;
        }

        public ParticleSystem getParticleSystem(){
            return particleSystem.particleSystem;
        }

        public void createParticleGroup(Vector2f[] normalizedVertices, LiquidPaint options) {
            particleSystem.createParticleGroup(normalizedVertices, options);
        }

        public void clearParticles(Vector2f[] normalizedVertices) {
            particleSystem.clearParticles(normalizedVertices);
        }

        public void onDraw(WaterParticleMaterial waterMaterial, ParticleMaterial nonWater, BlurRenderer blurRenderer){
            particleSystem.onDraw(waterMaterial, nonWater, blurRenderer, this);
        }
    }
}
