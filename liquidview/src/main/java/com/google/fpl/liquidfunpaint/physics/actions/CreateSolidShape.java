package com.google.fpl.liquidfunpaint.physics.actions;

import com.google.fpl.liquidfunpaint.physics.SolidWorld;
import com.google.fpl.liquidfunpaint.renderer.PhysicsLoop;
import com.google.fpl.liquidfunpaint.util.MathHelper;
import com.google.fpl.liquidfunpaint.util.Vector2f;

/**
 * Created on 15-09-26.
 */
public class CreateSolidShape implements PhysicsCommand {

    private final Vector2f[] mVertices;

    public CreateSolidShape(Vector2f[] vertices){
        mVertices = vertices;
    }

    @Override
    public void run() {
        Vector2f[] normalized = MathHelper.normalizeVertices(mVertices, PhysicsLoop.getInstance().sScreenWidth, PhysicsLoop.getInstance().sScreenHeight);
        SolidWorld.getInstance().createSolidObject(normalized);
    }
}
