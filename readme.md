![Sample image 1](https://github.com/ykulbashian/LiquidSurface/tree/master/screenshots/Screenshot_1.png)

# Running the sample
To run this project you need to do the following:

1. In terminal, go to the following folder:
**<project root>/liquidview/src/main/Box2D/swig/jni**

2. Run ndk-build in this folder. If you are one a Windows machine, use Cygwin.
(I'm having difficulty integrating Gradle native code building on Windows, which is why you have to do it manually).

This will generate the native (.so) libraries automatically and the rest should run correctly.

***

# Using the Library

### Add particle resume/pause
In your activity, remember to call:

```
LiquidTextureView.resumeParticles();
```

in the Activity's onResume()

and

```
LiquidTextureView.pauseParticles();
```

in the Activity's onPause()

### Load native libraries
In your Application class (or your home Activity) add the following code to load the native libraries:

```java
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
```

### Simple example
The most simple example of an app is the following:

```java
public class SampleActivity extends ActionBarActivity {

    /**
     * Load the native libraries
     */
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

        /**
         * Create a triangle of blue liquid
         */
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        int blueColor = 0xFF00FFFF;

        ltv.createLiquidShape(new float[]{
                size.x/2 - 200, size.y/2,
                size.x/2 + 200, size.y/2,
                size.x/2, size.y/2 + 400},
                blueColor);
    }

    /**
     * Make sure you call the following onResume() and onPause()
     */
    @Override
    protected void onResume() {
        super.onResume();
        ltv.resumeParticles();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ltv.pauseParticles();
    }
}
```

### Debug
If you want to debug the liquid physics, open the Renderer class and set:
**_(com.google.fpl.liquidfunpaint.Renderer)_**

```
public static final boolean DEBUG_DRAW = true;
```


### TextureView
The library uses a TextureView (instead of the usual GLSurfaceView).
It also has a few helper methods to create blocks of liquid out of the box.

```
public void createLiquidShape(final float[] vertices, final int color)
```

Vertices are passed in in pairs of floats (x, y) which is relative to the width and height of the TextureView itself.

There are other functions that are accessible that will be added in later commits.


## Adjustments to Google's LiquidFun library
This library is based on Google's LiquidFun library, and uses parts of the LiquidFun Paint library.
I've tried to keep most of the code from those two libraries the same, but I was forced to make a few tweaks:

In order to allow for creating arbitrary shapes, I increased the number of vertices you can create per Polygon. This is done in native code:

In **src/main/Box2D/Box2D/Common/b2Settings.h**

```
#define b2_maxPolygonVertices	30
```

That is the maximum number of vertices per polygon. It used to be 8. Be careful in increasing this number.

I also added access to a method for creating PolygonShape through pairs of floats.