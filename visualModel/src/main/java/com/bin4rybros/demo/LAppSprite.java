/*
 * 著作権（c）Live2D Inc.。全著作権所有。
 *
 * このソースコードの使用は、Live2Dオープンソフトウェアライセンスによって規制されています。
 * ライセンスは、http://live2d.com/eula/live2d-open-software-license-agreement_en.html で見つけることができます。
 */

package com.bin4rybros.demo;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class LAppSprite {
    public LAppSprite(
            float x,
            float y,
            float width,
            float height,
            int textureId,
            int programId
    ) {
        rect.left = x - width * 0.5f;
        rect.right = x + width * 0.5f;
        rect.up = y + height * 0.5f;
        rect.down = y - height * 0.5f;

        this.textureId = textureId;

        // What number of attribute variable
        positionLocation = GLES20.glGetAttribLocation(programId, "position");
        uvLocation = GLES20.glGetAttribLocation(programId, "uv");
        textureLocation = GLES20.glGetUniformLocation(programId, "texture");
        colorLocation = GLES20.glGetUniformLocation(programId, "baseColor");

        spriteColor[0] = 1.0f;
        spriteColor[1] = 1.0f;
        spriteColor[2] = 1.0f;
        spriteColor[3] = 1.0f;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        int windowWidth = LAppDelegate.getInstance().getWindowWidth();
        int windowHeight = LAppDelegate.getInstance().getWindowHeight();
    }

    public void render() {
        // Set the camera position (View matrix)
        uvVertex[0] = 1.0f;
        uvVertex[1] = 0.0f;
        uvVertex[2] = 0.0f;
        uvVertex[3] = 0.0f;
        uvVertex[4] = 0.0f;
        uvVertex[5] = 1.0f;
        uvVertex[6] = 1.0f;
        uvVertex[7] = 1.0f;


        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glEnableVertexAttribArray(uvLocation);

        GLES20.glUniform1i(textureLocation, 0);

        // Get screen size
        int maxWidth = LAppDelegate.getInstance().getWindowWidth();
        int maxHeight = LAppDelegate.getInstance().getWindowHeight();

        // Vertex data
        positionVertex[0] = (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f);
        positionVertex[1] = (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f);
        positionVertex[2] = (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f);
        positionVertex[3] = (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f);
        positionVertex[4] = (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f);
        positionVertex[5] = (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f);
        positionVertex[6] = (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f);
        positionVertex[7] = (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f);

        if (posVertexFloatBuffer == null) {
            ByteBuffer posVertexByteBuffer = ByteBuffer.allocateDirect(positionVertex.length * 4);
            posVertexByteBuffer.order(ByteOrder.nativeOrder());
            posVertexFloatBuffer = posVertexByteBuffer.asFloatBuffer();
        }
        if (uvVertexFloatBuffer == null) {
            ByteBuffer uvVertexByteBuffer = ByteBuffer.allocateDirect(uvVertex.length * 4);
            uvVertexByteBuffer.order(ByteOrder.nativeOrder());
            uvVertexFloatBuffer = uvVertexByteBuffer.asFloatBuffer();
        }
        posVertexFloatBuffer.put(positionVertex).position(0);
        uvVertexFloatBuffer.put(uvVertex).position(0);

        glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 0, posVertexFloatBuffer);
        glVertexAttribPointer(uvLocation, 2, GL_FLOAT, false, 0, uvVertexFloatBuffer);

        GLES20.glUniform4f(colorLocation, spriteColor[0], spriteColor[1], spriteColor[2], spriteColor[3]);

        GLES20.glBindTexture(GL_TEXTURE_2D, textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
    }

    private final float[] uvVertex = new float[8];
    private final float[] positionVertex = new float[8];

    private FloatBuffer posVertexFloatBuffer;
    private FloatBuffer uvVertexFloatBuffer;

    /**
     * Draw with specified texture ID
     *
     * @param textureId Texture ID
     * @param uvVertex UV vertex coordinates
     */
    public void renderImmediate(int textureId, final float[] uvVertex) {
        // Enable attribute properties
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glEnableVertexAttribArray(uvLocation);

        // Register uniform properties
        GLES20.glUniform1i(textureLocation, 0);

        // Get screen size
        int maxWidth = LAppDelegate.getInstance().getWindowWidth();
        int maxHeight = LAppDelegate.getInstance().getWindowHeight();

        // Vertex data
        float[] positionVertex = {
                (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f),
                (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f),
                (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f),
                (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f)
        };

        // Register attribute properties
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(positionVertex.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer buffer = bb.asFloatBuffer();
            buffer.put(positionVertex);
            buffer.position(0);

            GLES20.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 0, buffer);
        }
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(uvVertex.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer buffer = bb.asFloatBuffer();
            buffer.put(uvVertex);
            buffer.position(0);

            GLES20.glVertexAttribPointer(uvLocation, 2, GL_FLOAT, false, 0, buffer);
        }

        GLES20.glUniform4f(colorLocation, spriteColor[0], spriteColor[1], spriteColor[2], spriteColor[3]);

        // Draw the model
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
    }

    // Resize
    public void resize(float x, float y, float width, float height) {
        rect.left = x - width * 0.5f;
        rect.right = x + width * 0.5f;
        rect.up = y + height * 0.5f;
        rect.down = y - height * 0.5f;
    }

    /**
     * Perform collision detection with image
     *
     * @param pointX X coordinate of the touched point
     * @param pointY Y coordinate of the touched point
     * @return true if hit
     */
    public boolean isHit(float pointX, float pointY) {
        // Get screen height
        int maxHeight = LAppDelegate.getInstance().getWindowHeight();

        // Y coordinate needs to be transformed
        float y = maxHeight - pointY;

        return (pointX >= rect.left && pointX <= rect.right && y <= rect.up && y >= rect.down);
    }

    public void setColor(float r, float g, float b, float a) {
        spriteColor[0] = r;
        spriteColor[1] = g;
        spriteColor[2] = b;
        spriteColor[3] = a;
    }

    /**
     * Rect class
     */
    private static class Rect {
        /**
         * Left side
         */
        public float left;
        /**
         * Right side
         */
        public float right;
        /**
         * Top side
         */
        public float up;
        /**
         * Bottom side
         */
        public float down;
    }


    private final Rect rect = new Rect();
    private final int textureId;

    private final int positionLocation;  // Position attribute
    private final int uvLocation; // UV attribute
    private final int textureLocation;   // Texture attribute
    private final int colorLocation;     // Color attribute
    private final float[] spriteColor = new float[4];   // Display color

    // vpMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mVPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private int vPMatrixHandle;
}
