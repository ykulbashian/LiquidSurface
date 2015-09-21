package com.google.fpl.liquidfunpaint.util;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created on 8/16/2015.
 */
public interface DrawableLayer {
    void reset();
    void init(Context activity);
    void onSurfaceCreated(GL10 gl, EGLConfig config);
    void onSurfaceChanged(GL10 gl, int width, int height);
    void onDrawFrame(GL10 gl);
}
