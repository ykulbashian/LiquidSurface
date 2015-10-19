package com.mycardboarddreams.liquidsurface;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created on 15-10-18.
 */
public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private GLSurfaceView.Renderer mRenderer;

    private static final int TARGET_FRAME_RATE = 55;

    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final String TAG = "RenderThread";
    private SurfaceTexture mSurface;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private EGL10 mEgl;
    private EGLConfig eglConfig;
    private GL10 mGl;

    private int targetFrameDurationMillis;

    private int surfaceHeight;
    private int surfaceWidth;

    public boolean isRunning = false;
    private boolean paused = true;
    private boolean rendererChanged = false;

    private RenderThread thread;

    private int targetFps;

    public GLTextureView(Context context) {
        super(context);
        initialize(context);
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public synchronized void setRenderer(GLSurfaceView.Renderer renderer){
        mRenderer = renderer;
        rendererChanged = true;
    }


    private void initialize(Context context) {
        targetFps = TARGET_FRAME_RATE;

        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startThread(surface, width, height, targetFps);
    }

    public void startThread(SurfaceTexture surface, int width, int height, float targetFramesPerSecond){
        Log.d(TAG, "Starting GLTextureView thread");
        thread = new RenderThread();
        mSurface = surface;
        setDimensions(width, height);
        targetFrameDurationMillis = (int) ((1f/targetFramesPerSecond)*1000);

        thread.start();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        setDimensions(width, height);
        if(mRenderer != null)
            mRenderer.onSurfaceChanged(mGl, width, height);
    }

    public synchronized void setPaused(boolean isPaused){
        Log.d(TAG, String.format("Setting GLTextureView paused to %s", isPaused));
        paused = isPaused;
    }

    public synchronized boolean isPaused(){
        return paused;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopThread();
        return false;
    }

    public void stopThread(){
        if(thread != null){
            Log.d(TAG, "Stopping and joining GLTextureView");
            isRunning = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            thread = null;
        }

    }

    private boolean shouldSleep(){
        return isPaused() || mRenderer == null;
    }

    private class RenderThread extends Thread {
        @Override
        public void run() {
            isRunning = true;

            initGL();
            checkGlError();

            long lastFrameTime = System.currentTimeMillis();

            while (isRunning) {
                while (mRenderer == null){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        // Ignore
                    }

                }

                if(rendererChanged){
                    initializeRenderer(mRenderer);
                    rendererChanged = false;
                }

                if (!shouldSleep()) {

                    lastFrameTime = System.currentTimeMillis();

                    drawSingleFrame();
                }

                try {
                    if (shouldSleep())
                        Thread.sleep(100);
                    else {
                        long thisFrameTime = System.currentTimeMillis();
                        long timDiff = thisFrameTime - lastFrameTime;
                        lastFrameTime = thisFrameTime;
                        Thread.sleep(Math.max(10l, targetFrameDurationMillis - timDiff));
                    }
                } catch (InterruptedException e) {
// Ignore
                }
            }
        }

    }

    private synchronized void initializeRenderer(GLSurfaceView.Renderer renderer) {
        if(renderer != null && isRunning) {
            renderer.onSurfaceCreated(mGl, eglConfig);
            renderer.onSurfaceChanged(mGl, surfaceWidth, surfaceHeight);
        }
    }

    private synchronized void drawSingleFrame() {
        checkCurrent();

        if(mRenderer != null)
            mRenderer.onDrawFrame(mGl);

        checkGlError();
        if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
            Log.e(TAG, "cannot swap buffers!");
        }
    }

    public void setDimensions(int width, int height){
        surfaceWidth = width;
        surfaceHeight = height;
    }

    private void checkCurrent() {
        if (!mEglContext.equals(mEgl.eglGetCurrentContext())
                || !mEglSurface.equals(mEgl
                .eglGetCurrentSurface(EGL10.EGL_DRAW))) {
            checkEglError();
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                    mEglSurface, mEglContext)) {
                throw new RuntimeException(
                        "eglMakeCurrent failed "
                                + GLUtils.getEGLErrorString(mEgl
                                .eglGetError()));
            }
            checkEglError();
        }
    }

    private void checkEglError() {
        final int error = mEgl.eglGetError();
        if (error != EGL10.EGL_SUCCESS) {
            Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
        }
    }

    private void checkGlError() {
        final int error = mGl.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            Log.e(TAG, "GL error = 0x" + Integer.toHexString(error));
        }
    }

    private void initGL() {
        mEgl = (EGL10) EGLContext.getEGL();
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };
        eglConfig = null;
        if (!mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1,
                configsCount)) {
            throw new IllegalArgumentException(
                    "eglChooseConfig failed "
                            + GLUtils.getEGLErrorString(mEgl
                            .eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }
        int[] attrib_list = {
                EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay,
                eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError();
        mEglSurface = mEgl.eglCreateWindowSurface(
                mEglDisplay, eglConfig, mSurface, null);
        checkEglError();
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG,
                        "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException(
                    "eglCreateWindowSurface failed "
                            + GLUtils.getEGLErrorString(error));
        }
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        checkEglError();
        mGl = (GL10) mEglContext.getGL();
        checkEglError();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

}
