package com.mycardboarddreams.liquidsurface;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created on 15-11-27.
 */
public class GestureInterpreter {

    private GestureDetector gDetector;
    private ScaleGestureDetector scaleDetector;

    private GestureListener gestureListener;

    public GestureInterpreter(Context context){

        gDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if(gestureListener != null && gestureListener.onScroll(distanceX, distanceY))
                    return true;
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if(gestureListener != null && gestureListener.onScale(detector.getScaleFactor()))
                    return true;
                return false;
            }
        });
    }

    public void setGestureListener(GestureListener listener) {
        gestureListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(gestureListener != null){
            boolean accepted = gDetector.onTouchEvent(event);
            accepted |= scaleDetector.onTouchEvent(event);
            if(accepted)
                return true;
        }
        return false;
    }

    public interface GestureListener {
        boolean onScroll(float xDistance, float yDistance);
        boolean onScale(float scale);
    }

}
