package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.fpl.liquidfunpaint.util.Vector2f;
import com.mycardboarddreams.liquidsurface.LiquidTextureView;


public class SampleActivity extends AppCompatActivity implements View.OnClickListener{

    LiquidTextureView ltv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (LiquidTextureView) findViewById(R.id.liquid_texture_view);

        ltv.setOnClickListener(this);
    }

    /**
     * Make sure you call the following onResume() and onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();
        createLiquidTriangle();

        ltv.resumeParticles();
    }

    private void createLiquidTriangle() {
        /**
         * Create a triangle of blue liquid
         */
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        int blueColor = 0xFF00FFCC;

        Vector2f center = new Vector2f(size.x / 2, size.y / 2);

        ltv.createLiquidShape(createCircle(center, 200, 8),
                        blueColor);

        ltv.createSolidShape(createCircle(center, 50, 8));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pauseParticles();
    }


    @Override
    public void onClick(View v) {
        createLiquidTriangle();
    }

    private float[] createCircle(Vector2f center, float radius, int numPoints){
        float[] vertices = new float[numPoints*2];

        float angle = 360f/numPoints;

        for(int i = 0; i < numPoints; i++){
            vertices[2*i] = center.x + (float) (radius*Math.cos(i*angle));
            vertices[2*i + 1] = center.y + (float) (radius*Math.sin(i*angle));
        }

        return vertices;
    }
}
