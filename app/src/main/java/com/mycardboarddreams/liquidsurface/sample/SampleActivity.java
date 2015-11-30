package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.fpl.liquidfunpaint.LiquidPaint;
import com.google.fpl.liquidfunpaint.physics.actions.ParticleGroup;
import com.google.fpl.liquidfunpaint.physics.actions.SolidShape;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.mycardboarddreams.liquidsurface.GestureInterpreter;
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
        ParticleGroup liquidShape2 = new ParticleGroup(MathHelper.createCircle(getScreenCenter(), 300, 8), LiquidPaint.LIQUID(), "SecondParticleSystem");
        SolidShape solidShape = new SolidShape(MathHelper.createCircle(getScreenCenter(), 70, 8), "textures/smiley.png");

        ltv.createParticles(liquidShape1);
        ltv.createParticles(liquidShape2);
        ltv.createSolidShape(solidShape);

        ltv.addGestureListener(new GestureInterpreter.GestureListener() {
            @Override
            public boolean onScroll(float xDistance, float yDistance) {
                ltv.translateCamera(xDistance/400, -yDistance/400, 0);
                return true;
            }

            @Override
            public boolean onScale(float scale) {
                if(scale < 1)
                    ltv.translateCamera(0, 0, (-1/scale)/30);
                else
                    ltv.translateCamera(0, 0, scale/30);

                return false;
            }
        });
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
