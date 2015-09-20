package com.mycardboarddreams.liquidsurface.sample;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.google.fpl.liquidfunpaint.ILiquidWorld;
import com.google.fpl.liquidfunpaint.renderer.GameLoop;
import com.google.fpl.liquidfunpaint.SolidWorld;
import com.google.fpl.liquidfunpaint.util.Vector2f;


public class SampleActivity extends AppCompatActivity implements View.OnTouchListener, Runnable {

    ILiquidWorld ltv;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ltv = (ILiquidWorld) findViewById(R.id.liquid_texture_view);

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

        ltv.resumePhysics();

        run();
    }

    @Override
    public void run() {
        handler.postDelayed(this, 2000);

        float[] emptyBox = createBox(new Vector2f(100, 100), 200, 200);
        float[] fillBox = createBox(new Vector2f(GameLoop.getInstance().sScreenWidth, 300), 200, 200);

        ltv.eraseParticles(emptyBox);
//        ltv.createLiquidShape(fillBox);

    }

    private Vector2f getCenterPoint(){

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Vector2f center = new Vector2f(size.x / 2, size.y / 2);
        return center;
    }

    private void createLiquidCircle() {

        ltv.createLiquidShape(createCircle(getCenterPoint(), 600, 8));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pausePhysics();

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            if(event.getX() > GameLoop.getInstance().sScreenWidth/2)
                SolidWorld.getInstance().spinWheel(-0.5f);
            else
                SolidWorld.getInstance().spinWheel(0.5f);
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

    private float[] createBox(Vector2f center, float width, float height){
        float[] vertices = new float[4*2];

        vertices[0] = center.x - width/2;
        vertices[1] = center.y + height/2;

        vertices[2] = center.x + width/2;
        vertices[3] = center.y + height/2;

        vertices[4] = center.x - width/2;
        vertices[5] = center.y - height/2;

        vertices[6] = center.x + width/2;
        vertices[7] = center.y - height/2;

        return vertices;
    }
}
