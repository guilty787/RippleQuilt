package com.wix.mobile.ripplequilt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gil_Moshayof on 10/30/14.
 */
public class BackgroundView
{
    public static int[] bgTexture;

    private float mVertices[] =  {
            -1.0f, -1.0f,  0.0f,        // V1 - bottom left
            -1.0f,  1.0f,  0.0f,        // V2 - top left
            1.0f, -1.0f,  0.0f,        // V3 - bottom right
            1.0f,  1.0f,  0.0f         // V4 - top right
    };

    private float mNormals[] = {
            0, 0, 1f,
            0, 0, 1f,
            0, 0, 1f,
            0, 0, 1f,
    };

    private FloatBuffer mTextureFrameBuffer;  // buffer holding the texture coordinates
    private float mTextureFrame[] = {

            // Mapping coordinates for the vertices
            0.0f, 1.0f,     // top left     (V2)
            0.0f, 0.0f,     // bottom left  (V1)
            1.0f, 1.0f,     // top right    (V4)
            1.0f, 0.0f      // bottom right (V3)
    };

    private FloatBuffer mVertexBuffer;   // buffer holding the vertices

    private FloatBuffer mNormalBuffer;

    public BackgroundView()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(mVertices);
        mVertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(mTextureFrame.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mTextureFrameBuffer = byteBuffer.asFloatBuffer();
        mTextureFrameBuffer.put(mTextureFrame);
        mTextureFrameBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(mNormals.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mNormalBuffer = byteBuffer.asFloatBuffer();
        mNormalBuffer.put(mNormals);
        mNormalBuffer.position(0);
    }

    public void draw(GL10 gl)
    {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glFrontFace(GL10.GL_CW);

        gl.glPushMatrix();

        gl.glTranslatef(0, 0, 500);
        gl.glScalef(500, 500, 1);

        gl.glColor4f(0.15f, 0.15f, 0.15f, 0.15f);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, bgTexture[0]);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureFrameBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);
        //gl.glDrawElements(GL10.GL_TRIANGLES, mShipIndicies.length, GL10.GL_UNSIGNED_BYTE, mShipIndexBuffer);

        gl.glColor4f(1, 1, 1, 1);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);


        gl.glPopMatrix();
    }
}
