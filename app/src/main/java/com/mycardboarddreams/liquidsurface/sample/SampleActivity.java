package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleGroup;
import com.google.fpl.liquidfunpaint.physics.actions.SolidShape;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.mycardboarddreams.liquidsurface.ILiquidWorld;
import com.google.fpl.liquidfunpaint.util.Vector2f;


public class SampleActivity extends AppCompatActivity {

    ILiquidWorld ltv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (ILiquidWorld) findViewById(R.id.liquid_texture_view);

        ltv.clearAll();
        ParticleGroup liquidShape1 = new ParticleGroup(MathHelper.createCircle(getScreenCenter(), 400, 8));
        ParticleGroup liquidShape2 = new ParticleGroup(MathHelper.createCircle(getScreenCenter(), 300, 8), LiquidPaint.LIQUID().setColor(0xFF00FF00), "SecondParticleSystem");
        SolidShape solidShape = new SolidShape(MathHelper.createCircle(getScreenCenter(), 70, 8), "textures/smiley.png");

        ltv.createParticles(liquidShape1);
        ltv.createParticles(liquidShape2);
        ltv.createSolidShape(solidShape);
    }

    /**
     * Make sure you call the following onStart() and onStop()
     */
    @Override
    protected void onStart() {
        super.onStart();
        ltv.resumePhysics();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ltv.pausePhysics();
    }

    private Vector2f getScreenCenter(){
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Vector2f center = new Vector2f(size.x / 2, size.y / 2);
        return center;
    }
}
