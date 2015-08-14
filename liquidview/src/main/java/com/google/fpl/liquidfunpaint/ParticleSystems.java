package com.google.fpl.liquidfunpaint;

import com.google.fpl.liquidfun.ParticleSystem;
import com.google.fpl.liquidfun.ParticleSystemDef;
import com.google.fpl.liquidfun.World;

import java.util.HashMap;

/**
 * Created by PC on 8/13/2015.
 */
public class ParticleSystems extends HashMap<String, ParticleSystem> {

    public void createDefaultParticleSystem(String key){
        World world = LiquidWorld.getInstance().acquireWorld();
        try {
            // Create a new particle system; we only use one.
            ParticleSystemDef psDef = new ParticleSystemDef();
            psDef.setRadius(Renderer.PARTICLE_RADIUS);
            psDef.setRepulsiveStrength(Renderer.PARTICLE_REPULSIVE_STRENGTH);
            psDef.setElasticStrength(2.0f);
            ParticleSystem particleSystem = world.createParticleSystem(psDef);
            particleSystem.setMaxParticleCount(Renderer.MAX_PARTICLE_COUNT);

            put(key, particleSystem);
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

}
