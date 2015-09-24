package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.fpl.liquidfunpaint.LiquidPaint;
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
        ltv.createLiquidShape(MathHelper.createCircle(getScreenCenter(), 400, 8));

        ltv.createLiquidShape(MathHelper.createCircle(getScreenCenter(), 300, 8), LiquidPaint.LIQUID().setColor(0xFF00FF00), "SecondParticleSystem");

        ltv.createSolidShape(MathHelper.createCircle(getScreenCenter(), 70, 8));
    }

    /**
     * Make sure you call the following onResume() and onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();
        ltv.resumePhysics();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pausePhysics();
    }

    private Vector2f getScreenCenter(){

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Vector2f center = new Vector2f(size.x / 2, size.y / 2);
        return center;
    }
}
