package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.google.fpl.liquidfun.liquidfunJNI;
import com.mycardboarddreams.liquidsurface.LiquidTextureView;


public class SampleActivity extends ActionBarActivity {

    LiquidTextureView ltv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (LiquidTextureView) findViewById(R.id.liquid_texture_view);

    }

    /**
     * Make sure you call the following onResume() and onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Create a triangle of blue liquid
         */
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        int blueColor = 0xFF00FFFF;

        ltv.createLiquidShape(new float[]{
                        size.x / 2 - 200, size.y / 2,
                        size.x / 2 + 200, size.y / 2,
                        size.x / 2, size.y / 2 + 400},
                        blueColor);

        ltv.resumeParticles();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pauseParticles();
    }
}
