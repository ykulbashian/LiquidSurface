package com.google.fpl.liquidfunpaint.util;

import android.content.Context;

import com.google.fpl.liquidfunpaint.GLTextureView;

/**
 * Created on 8/16/2015.
 */
public interface DrawableLayer extends GLTextureView.Renderer{
    void reset();
    void init(Context activity);
}
