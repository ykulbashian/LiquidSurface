package com.google.fpl.liquidfunpaint;

import android.graphics.Color;

import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.Vec2;

/**
 * Created by PC on 5/27/2015.
 */
public enum GroupOptions {

    SOLID(Color.BLACK, ParticleFlag.elasticParticle, ParticleGroupFlag.rigidParticleGroup),
    WALL(Color.BLACK, ParticleFlag.wallParticle, ParticleGroupFlag.rigidParticleGroup),
    LIQUID(0xFF00AAFF, ParticleFlag.waterParticle, ParticleGroupFlag.particleGroupCanBeEmpty);

    GroupOptions(int color, int particleFlag, int groupFlag){
        setColor(color);
        setParticleType(particleFlag);
        particleGroup = groupFlag;
    }

    public void setVelocity(Vec2 velocity) {
        this.velocity = velocity;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public void setParticleType(int particleType) {
        this.particleType = particleType;
    }

    Vec2 velocity = new Vec2(0, 0);
    int color = Color.CYAN;
    float strength = 2.0f;
    int particleType = ParticleFlag.waterParticle;
    int particleGroup = ParticleGroupFlag.particleGroupCanBeEmpty;

}
