package com.google.fpl.liquidfunpaint.physics.actions;

import android.text.TextUtils;

import com.google.fpl.liquidfun.BodyType;
import com.google.fpl.liquidfunpaint.physics.SolidWorld;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-26.
 */
public class SolidShape implements PhysicsCommand {

    private final Vector2f[] mVertices;
    private final String mTextureName;

    public SolidShape(Vector2f[] vertices){
        mVertices = vertices;
        mTextureName = null;
    }

    public SolidShape(Vector2f[] vertices, String textureName){
        mVertices = vertices;

        if(TextUtils.isEmpty(textureName))
            mTextureName = "textures/smiley.png";
        else
            mTextureName = textureName;
    }

    @Override
    public void run() {
        Vector2f[] normalized = MathHelper.normalizeVertices(mVertices, PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight);
        SolidWorld.getInstance().createSolidObject(normalized, BodyType.dynamicBody, mTextureName);
    }
}
