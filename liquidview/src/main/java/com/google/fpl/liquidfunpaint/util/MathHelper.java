/**
* Copyright (c) 2014 Google, Inc. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package com.google.fpl.liquidfunpaint.util;

import com.google.fpl.liquidfun.PolygonShape;
import com.google.fpl.liquidfun.Vec2;
import com.google.fpl.liquidfunpaint.physics.WorldLock;

public final class MathHelper {
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Vec2 normalizePosition(Vec2 pos){
        Vec2 normal = new Vec2();
        normal.setX(2*(pos.getX() - WorldLock.getInstance().sRenderWorldWidth/2)/WorldLock.getInstance().sRenderWorldWidth);
        normal.setY(2*(pos.getY() - WorldLock.getInstance().sRenderWorldHeight/2)/WorldLock.getInstance().sRenderWorldHeight);
        return normal;
    }

    public static float[] convertVectToFloats(Vector2f[] vecs){

        float[] points = new float[vecs.length*2];
        for(int i = 0; i < vecs.length; i++){
            points[2*i] = vecs[i].x;
            points[2*i + 1] = vecs[i].y;
        }
        return points;
    }

    public static Vector2f[] normalizeVertices(Vector2f[] originalVertices, float viewWidth, float viewHeight){
        float widthRatio = WorldLock.getInstance().sRenderWorldWidth / viewWidth;
        float heightRatio = WorldLock.getInstance().sRenderWorldHeight / viewHeight;

        Vector2f[] normalizedVerts = new Vector2f[originalVertices.length];

        for(int i = 0; i < originalVertices.length; i++){
            normalizedVerts[i] = new Vector2f(originalVertices[i].x * widthRatio, originalVertices[i].y * heightRatio);
        }

        return normalizedVerts;
    }

    public static Vector2f[] createCircle(Vector2f center, float radius, int numPoints){
        Vector2f[] vertices = new Vector2f[numPoints];

        double angle = 2*Math.PI/numPoints;

        for(int i = 0; i < numPoints; i++){
            vertices[i] = new Vector2f(center.x + (float) (radius*Math.cos(i*angle)),
                    center.y + (float) (radius*Math.sin(i*angle)));
        }

        return vertices;
    }


    public static Vector2f[] createBox(Vector2f center, float width, float height){
        Vector2f[] vertices = new Vector2f[4];

        vertices[0] = new Vector2f(center.x - width/2, center.y - height/2);

        vertices[1] = new Vector2f(center.x + width/2, center.y - height/2);

        vertices[2] = new Vector2f(center.x + width/2, center.y + height/2);

        vertices[3] = new Vector2f(center.x - width/2, center.y + height/2);

        return vertices;
    }

    public static Vec2[] getPolygonVertices(PolygonShape shape){
        int vertexCount = shape.getVertexCount();

        Vec2[] vertices = new Vec2[vertexCount];
        for(int i = 0; i < vertexCount; i++){
            vertices[i] = shape.getVertex(i);
        }

        return vertices;
    }

    public static Vector2f getPolygonSize(PolygonShape shape){
        if(shape.getVertexCount() == 0)
            return new Vector2f(0, 0);

        float lowX = Float.MAX_VALUE;
        float lowY = Float.MAX_VALUE;
        float highX = Float.MIN_VALUE;
        float highY = Float.MIN_VALUE;

        for(int i = 0; i < shape.getVertexCount(); i++){
            Vec2 v = shape.getVertex(i);
            if(v.getX() < lowX) lowX = v.getX();
            if(v.getX() > highX) highX = v.getX();
            if(v.getY() < lowY) lowY = v.getY();
            if(v.getY() > highY) highY = v.getY();
        }

        return new Vector2f(highX - lowX, highY - lowY);
    }

    public static Vector2f[] getPolygonVertices2f(PolygonShape shape){
        int vertexCount = shape.getVertexCount();

        Vector2f[] vertices = new Vector2f[vertexCount];
        for(int i = 0; i < vertexCount; i++){
            vertices[i] = new Vector2f(shape.getVertex(i).getX(), shape.getVertex(i).getY());
        }

        return vertices;
    }

}
