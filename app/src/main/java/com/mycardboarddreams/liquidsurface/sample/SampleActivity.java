package com.mycardboarddreams.liquidsurface.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

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

        ltv.createLiquidShape(new float[]{100, 100, 200, 200, 100, 300}, 0xFF00FF00, 1);
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
