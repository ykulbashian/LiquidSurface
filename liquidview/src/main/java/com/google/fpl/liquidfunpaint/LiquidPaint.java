package com.google.fpl.liquidfunpaint;

import android.graphics.Color;

import com.google.fpl.liquidfun.ParticleColor;
import com.google.fpl.liquidfun.ParticleFlag;
import com.google.fpl.liquidfun.ParticleGroup;
import com.google.fpl.liquidfun.ParticleGroupDef;
import com.google.fpl.liquidfun.ParticleGroupFlag;
import com.google.fpl.liquidfun.Shape;
import com.google.fpl.liquidfun.Vec2;

/**
 * Created on 5/27/2015.
 */
public class LiquidPaint {

    public static LiquidPaint SOLID() {return new LiquidPaint(Color.BLACK, ParticleFlag.elasticParticle, ParticleGroupFlag.rigidParticleGroup);}
    public static LiquidPaint ELASTIC() {return ELASTIC(Color.GREEN);}
    public static LiquidPaint WALL() {return new LiquidPaint(Color.BLACK, ParticleFlag.wallParticle, ParticleGroupFlag.rigidParticleGroup);}
    public static LiquidPaint LIQUID() {return LIQUID(0xFF00AAFF);}

    public static LiquidPaint LIQUID(int color) {return new LiquidPaint(color, ParticleFlag.waterParticle, ParticleGroupFlag.particleGroupCanBeEmpty);}
    public static LiquidPaint ELASTIC(int color) {return new LiquidPaint(color, ParticleFlag.elasticParticle, ParticleGroupFlag.rigidParticleGroup);}

    public LiquidPaint(int color, int particleFlag, int groupFlag){
        setColor(color);
        setParticleType(particleFlag);
        particleGroup = groupFlag;
    }

    public LiquidPaint setVelocity(Vec2 velocity) {
        this.velocity = velocity;
        return this;
    }

    public LiquidPaint setColor(int color) {
        this.color = color;
        return this;
    }

    public LiquidPaint setStrength(float strength) {
        this.strength = strength;
        return this;
    }

    public LiquidPaint setParticleType(int particleType) {
        this.particleType = particleType;
        return this;
    }
    
    public ParticleGroupDef createParticleGroupDef(Shape shape){

        ParticleColor pColor = new ParticleColor(
                (short) Color.red(color),
                (short)Color.green(color),
                (short)Color.blue(color),
                (short)Color.alpha(color));

        final ParticleGroupDef pgd = new ParticleGroupDef();
        pgd.setFlags(particleType);
        pgd.setGroupFlags(particleGroup);
        pgd.setLinearVelocity(velocity);
        pgd.setColor(pColor);
        pgd.setStrength(strength);

        pgd.setShape(shape);

        return pgd;
    }

    Vec2 velocity = new Vec2(0, 0);
    int color = Color.CYAN;
    float strength = 2.0f;
    int particleType = ParticleFlag.waterParticle;
    int particleGroup = ParticleGroupFlag.particleGroupCanBeEmpty;

}
