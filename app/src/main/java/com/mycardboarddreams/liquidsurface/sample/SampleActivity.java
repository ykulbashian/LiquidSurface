package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.google.fpl.liquidfunpaint.Renderer;
import com.google.fpl.liquidfunpaint.SolidWorld;
import com.google.fpl.liquidfunpaint.util.Vector2f;
import com.mycardboarddreams.liquidsurface.LiquidTextureView;


public class SampleActivity extends AppCompatActivity implements View.OnTouchListener{

    LiquidTextureView ltv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (LiquidTextureView) findViewById(R.id.liquid_texture_view);

        ltv.setOnTouchListener(this);
    }

    /**
     * Make sure you call the following onResume() and onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();
        createLiquidCircle();

        ltv.createSolidShape(createCircle(getCenterPoint(), 100, 8));

        ltv.resumeParticles();
    }

    private Vector2f getCenterPoint(){

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Vector2f center = new Vector2f(size.x / 2, size.y / 2);
        return center;
    }

    private void createLiquidCircle() {

        ltv.createLiquidShape(createCircle(getCenterPoint(), 500, 8));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pauseParticles();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            if(event.getX() > Renderer.getInstance().sScreenWidth/2)
                SolidWorld.getInstance().spinWheel(-0.05f);
            else
                SolidWorld.getInstance().spinWheel(0.05f);
        } else if(action == MotionEvent.ACTION_UP){
            SolidWorld.getInstance().spinWheel(0);
        }

        return true;
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
