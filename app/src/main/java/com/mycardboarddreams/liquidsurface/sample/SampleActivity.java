package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;

import com.google.fpl.liquidfun.liquidfunJNI;
import com.mycardboarddreams.liquidsurface.LiquidTextureView;


public class SampleActivity extends ActionBarActivity {

    static {
        try{
            System.loadLibrary("liquidfun");
            System.loadLibrary("liquidfun_jni");

            liquidfunJNI.init();
        } catch (UnsatisfiedLinkError e) {
            // only ignore exception in non-android env. This is to aid Robolectric integration.
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) throw e;
        }
    }

    LiquidTextureView ltv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (LiquidTextureView) findViewById(R.id.liquid_texture_view);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int horizontalMiddle = size.x/2;
        int verticalMiddle = size.y/2;

        ltv.createLiquidShape(new float[]{
                horizontalMiddle - 200, verticalMiddle,
                horizontalMiddle + 200, verticalMiddle,
                horizontalMiddle, verticalMiddle + 400}, 0xFF00FFFF);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pauseParticles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ltv.resumeParticles();
    }
}
