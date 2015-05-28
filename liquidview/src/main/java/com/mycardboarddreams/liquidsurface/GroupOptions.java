package com.mycardboarddreams.liquidsurface;

import android.graphics.Color;

import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.Vec2;

/**
 * Created by PC on 5/27/2015.
 */
public class GroupOptions {

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
    float strength = 0.1f;
    int particleType = ParticleFlag.waterParticle;

}
