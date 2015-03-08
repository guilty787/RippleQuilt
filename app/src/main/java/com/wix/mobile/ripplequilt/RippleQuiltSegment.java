package com.wix.mobile.ripplequilt;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gil_Moshayof on 11/2/14.
 */
public class RippleQuiltSegment
{
    public static final float MAX_Z_TRANSLATION = -5;

    public static int[] kTextures;

    private float mVertices[] =  {
            -(RippleQuiltRenderer.SEGMENT_SIZE / 2f), -(RippleQuiltRenderer.SEGMENT_SIZE / 2f),  0.0f,        // V1 - bottom left
            -(RippleQuiltRenderer.SEGMENT_SIZE / 2f),  (RippleQuiltRenderer.SEGMENT_SIZE / 2f),  0.0f,        // V2 - top left
            (RippleQuiltRenderer.SEGMENT_SIZE / 2f), -(RippleQuiltRenderer.SEGMENT_SIZE / 2f),  0.0f,        // V3 - bottom right
            (RippleQuiltRenderer.SEGMENT_SIZE / 2f),  (RippleQuiltRenderer.SEGMENT_SIZE / 2f),  0.0f         // V4 - top right
    };


    private FloatBuffer mTextureFrameBuffer;  // buffer holding the texture coordinates
    private float[] mTextureFrame; /*[] = {

            // Mapping coordinates for the vertices
            0.0f, 1.0f,     // top left     (V2)
            0.0f, 0.0f,     // bottom left  (V1)
            1.0f, 1.0f,     // top right    (V4)
            1.0f, 0.0f      // bottom right (V3)
    };
    */

    private FloatBuffer mVertexBuffer;   // buffer holding the vertices

    private PointF mPosition;
    private int mCurrentTextureId;


    private boolean mIsAnimating;
    private boolean mTextureSwitched;
    private long mTotalElapsedAnimatingTime;

    public RippleQuiltSegment(float xPos, float yPos)
    {
        mPosition = new PointF(xPos, yPos);

        configureTextureFrame();

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


        mCurrentTextureId = 0;
    }

    public boolean isAnimating()
    {
        return mIsAnimating;
    }

    public void startAnimating()
    {
        if (mIsAnimating)
            return;

        mIsAnimating = true;
        mTotalElapsedAnimatingTime = 0;
        mTextureSwitched = false;
    }

    public void performAnimationTick(long elapsedTime)
    {
        if (!mIsAnimating)
            return;

        mTotalElapsedAnimatingTime += elapsedTime;

        checkForTextureSwitch();

        if (mTotalElapsedAnimatingTime > RippleQuiltRenderer.TIME_TO_FLIP_RIPPLE_SEGMENT)
            mIsAnimating = false;
    }

    private void checkForTextureSwitch()
    {
        if (!mTextureSwitched && getRotation() > 90 +  RippleQuiltRenderer.SEGMENT_FIELD_ROTATION)
        {
            mTextureSwitched = true;
            mCurrentTextureId++;

            if (mCurrentTextureId >= kTextures.length)
                mCurrentTextureId = 0;
        }

    }

    public float getZ()
    {
        if (!mIsAnimating)
            return 0;

        float animationRatio = getAnimationRatio();

        return (float)Math.sin(animationRatio * Math.PI) * MAX_Z_TRANSLATION;
    }

    public float getRotation()
    {
        if (!mIsAnimating)
            return 0;

        float animationRatio = getAnimationRatio();

        return animationRatio * 180;
    }

    public PointF getPosition()
    {
        return mPosition;
    }

    private float getAnimationRatio()
    {
        return (float)mTotalElapsedAnimatingTime / (float)RippleQuiltRenderer.TIME_TO_FLIP_RIPPLE_SEGMENT;
    }

    private void configureTextureFrame()
    {
        float indexX = mPosition.x / RippleQuiltRenderer.SEGMENT_SIZE;
        float indexY = mPosition.y / RippleQuiltRenderer.SEGMENT_SIZE;


        float textureStartX = (indexX + (float)RippleQuiltRenderer.QUILT_DIMENSION / 2f) / (float)RippleQuiltRenderer.QUILT_DIMENSION;
        float textureStartY = 1f - (indexY + (float)RippleQuiltRenderer.QUILT_DIMENSION / 2f) / (float)RippleQuiltRenderer.QUILT_DIMENSION;
        float textureEndX = ((indexX + 1f) + (float)RippleQuiltRenderer.QUILT_DIMENSION / 2f) / (float)RippleQuiltRenderer.QUILT_DIMENSION;
        float textureEndY = 1f - ((indexY + 1f) + (float)RippleQuiltRenderer.QUILT_DIMENSION / 2f) / (float)RippleQuiltRenderer.QUILT_DIMENSION;

        mTextureFrame = new float[] {
                textureStartX, textureStartY,
                textureStartX, textureEndY,
                textureEndX, textureStartY,
                textureEndX, textureEndY
        };
    }

    public void draw(GL10 gl)
    {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glFrontFace(GL10.GL_CW);

        gl.glColor4f(1, 1, 1, 1);

        gl.glPushMatrix();

        gl.glTranslatef(mPosition.x, mPosition.y, getZ());
        gl.glRotatef(-getRotation(), 1, 0, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, kTextures[mCurrentTextureId]);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureFrameBuffer);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);


        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);


        gl.glPopMatrix();
    }
}
