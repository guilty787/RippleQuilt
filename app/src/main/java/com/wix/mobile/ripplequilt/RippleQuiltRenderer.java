package com.wix.mobile.ripplequilt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gil_Moshayof on 11/2/14.
 */
public class RippleQuiltRenderer implements GLSurfaceView.Renderer
{
    public static final float SEGMENT_FIELD_ROTATION = 30;
    public static final float CAMERA_DISTANCE = 50;

    private static final float MIN_CAMERA_DISTANCE = 10f;

    public static final float SEGMENT_SIZE = 1f;

    public static final int QUILT_DIMENSION = 25;

    public static final long TIME_TO_FLIP_RIPPLE_SEGMENT = 500;
    public static final long TIME_TO_RIPPLE_COMPLETE = 3000;

    private BackgroundView mBackground;
    private List<RippleQuiltSegment> mSegments;
    private PointF mLookAtPosition;

    private Context mContext;

    private long mLastTimeStamp;
    private List<RippleQuiltSegment> mUnAnimatedSegments;
    private long mTotalElapsedRippleAnimationTime;
    private boolean mIsAnimating;
    private PointF mRippleOrigin;

    private float mTouchX;
    private float mTouchY;
    private float mScreenWidth;
    private float mScreenHeight;
    private boolean mTouchPerformed;


    private RippleQuiltSegment mSelectedSegment = null;
    private PointF mStartPosition = new PointF();

    private float mAspectRatio;

    private static int[] resIds = new int[]
            {
                    R.drawable.black_widow,
                    R.drawable.captain_america,
                    R.drawable.groot,
                    R.drawable.hawkeye,
                    R.drawable.hulk,
                    R.drawable.iron_man,
                    R.drawable.iron_patriot,
                    R.drawable.loki,
                    R.drawable.nick_fury,
                    R.drawable.rocket,
                    R.drawable.spider_man,
                    R.drawable.starlord,
                    R.drawable.thor,
                    R.drawable.winter_soldier
            };

    private static void generateTextures(GL10 gl, Context context)
    {
        RippleQuiltSegment.kTextures = new int[resIds.length];

        for (int i = 0; i < resIds.length; i++)
        {
            loadSingleGlTexture(gl, context, i, resIds[i]);
        }

        loadBgTexture(gl, context);
    }

    private static void loadSingleGlTexture(GL10 gl, Context context, int textureId, int resourceId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        gl.glGenTextures(1, RippleQuiltSegment.kTextures, textureId);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, RippleQuiltSegment.kTextures[textureId]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    private static void loadBgTexture(GL10 gl, Context context)
    {
        BackgroundView.bgTexture = new int[1];
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marvel);
        gl.glGenTextures(1, BackgroundView.bgTexture, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, BackgroundView.bgTexture[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public RippleQuiltRenderer(Context context)
    {
        mLookAtPosition = new PointF(0, 0);
        mSegments = new ArrayList<RippleQuiltSegment>();
        mUnAnimatedSegments = new ArrayList<RippleQuiltSegment>();
        mContext = context;
    }

    public void moveBy(float deltaX, float deltaY)
    {
        mSelectedSegment = null;

        mLookAtPosition.x += deltaX;
        mLookAtPosition.y += deltaY;


        //adjustSpheresByPosition();
    }

    private void createSegments()
    {
        float xPos, yPos;

        for (int x = -QUILT_DIMENSION / 2; x < QUILT_DIMENSION / 2; x++)
        {
            for (int y = -QUILT_DIMENSION / 2; y < QUILT_DIMENSION / 2; y++)
            {
                xPos = x * SEGMENT_SIZE;
                yPos = y * SEGMENT_SIZE;

                mSegments.add(new RippleQuiltSegment(xPos, yPos));
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig)
    {
        if (RippleQuiltSegment.kTextures == null)
        {
            generateTextures(gl, mContext);
            createSegments();

            mBackground = new BackgroundView();
        }

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        //gl.glEnable(GL10.GL_DEPTH_TEST);
        //gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glLoadIdentity();

        mAspectRatio = (float)width / (float)height;
        GLU.gluPerspective(gl, 45f, mAspectRatio, MIN_CAMERA_DISTANCE, 700f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private float getDistanceOfSegment(RippleQuiltSegment segment)
    {
        float result = (float)Math.sqrt((segment.getPosition().x - mRippleOrigin.x) * (segment.getPosition().x - mRippleOrigin.x) + (segment.getPosition().y - mRippleOrigin.y) * (segment.getPosition().y - mRippleOrigin.y));
        return result;
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        long elapsedTime = 0;

        if (mLastTimeStamp != 0)
            elapsedTime = System.currentTimeMillis() - mLastTimeStamp;

        mLastTimeStamp = System.currentTimeMillis();

        if (mIsAnimating)
        {
            mTotalElapsedRippleAnimationTime += elapsedTime;

            float animationRatio = (float)mTotalElapsedRippleAnimationTime / (float)TIME_TO_RIPPLE_COMPLETE;

            if (mTotalElapsedRippleAnimationTime > TIME_TO_RIPPLE_COMPLETE)
            {
                mIsAnimating = false;
                animationRatio = 1f;
            }

            RippleQuiltSegment segment;
            for (int i = 0; i < mUnAnimatedSegments.size(); i++)
            {
                segment = mUnAnimatedSegments.get(i);

                if (getDistanceOfSegment(segment) < (SEGMENT_SIZE * QUILT_DIMENSION * 1.5f * animationRatio) && !segment.isAnimating())
                {
                    segment.startAnimating();
                    mUnAnimatedSegments.remove(i);
                    i--;
                }
            }
        }


        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -CAMERA_DISTANCE, 0, 0, 0, 0, 1, 0);

        gl.glRotatef(SEGMENT_FIELD_ROTATION, 1, 0, 0);
        gl.glTranslatef(-mLookAtPosition.x, -mLookAtPosition.y, 0);

        //mBackground.draw(gl);

        //gl.glTranslatef(mLookAtPosition.x, mLookAtPosition.y, 0);

        //gl.glTranslatef(-mLookAtPosition.x, -mLookAtPosition.y, 0);

        for (RippleQuiltSegment segment : mSegments)
        {
            segment.performAnimationTick(elapsedTime);
            segment.draw(gl);
        }
    }

    public void setClickCoords(float touchX, float touchY, float screenWidth, float screenHeight)
    {
        mTouchX = touchX;
        mTouchY = touchY;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mTouchPerformed = true;

        findTouchedSegment();
    }

    private void findTouchedSegment()
    {
        //GLU.gluLookAt(gl, 0, 0, -150, 0, 1, 0, 0, 1, 0);
        float[] view = new float[3];

        float[] cameraLookAt = new float[]{mLookAtPosition.x, mLookAtPosition.y, 0};
        float[] cameraPosition = new float[]{mLookAtPosition.x, mLookAtPosition.y + -CAMERA_DISTANCE * (float) Math.sin(SEGMENT_FIELD_ROTATION * Math.PI / 180f), -CAMERA_DISTANCE * (float) Math.cos(SEGMENT_FIELD_ROTATION * Math.PI / 180f)};


        view[0] = cameraLookAt[0] - cameraPosition[0];
        view[1] = cameraLookAt[1] - cameraPosition[1];
        view[2] = cameraLookAt[2] - cameraPosition[2];

        normalizeVector(view);

        float[] h = new float[3];
        float[] cameraUp = new float[]{0, 1, 0};

        //(a2b3 - a3b2, a3b1 - a1b3, a1b2 - a2b1); a vector quantity

        h[0] = view[1] * cameraUp[2] - view[2] * cameraUp[1];
        h[1] = view[2] * cameraUp[0] - view[0] * cameraUp[2];
        h[2] = view[0] * cameraUp[1] - view[1] * cameraUp[0];
        normalizeVector(h);

        float[] v = new float[3];

        v[0] = h[1] * view[2] - h[2] * view[1];
        v[1] = h[2] * view[0] - h[0] * view[2];
        v[2] = h[0] * view[1] - h[1] * view[0];
        normalizeVector(v);


        float radians = 45f * (float) Math.PI / 180f;

        float vLength = (float) Math.tan(radians / 2f) * MIN_CAMERA_DISTANCE;
        float hLength = vLength * mScreenWidth / mScreenHeight;

        v[0] *= vLength;
        v[1] *= vLength;
        v[2] *= vLength;

        h[0] *= hLength;
        h[1] *= hLength;
        h[2] *= hLength;

        float x = mTouchX - mScreenWidth / 2f;
        float y = mScreenHeight / 2f - mTouchY;

        x /= mScreenWidth / 2f;
        y /= mScreenHeight / 2f;

        float[] pos = new float[3];

        pos[0] = cameraPosition[0] + view[0] * MIN_CAMERA_DISTANCE + h[0] * x + v[0] * y;
        pos[1] = cameraPosition[1] + view[1] * MIN_CAMERA_DISTANCE + h[1] * x + v[1] * y;
        pos[2] = cameraPosition[2] + view[2] * MIN_CAMERA_DISTANCE + h[2] * x + v[2] * y;

        float[] dir = new float[3];
        dir[0] = pos[0] - cameraPosition[0];
        dir[1] = pos[1] - cameraPosition[1];
        dir[2] = pos[2] - cameraPosition[2];
        //normalizeVector(dir);


        float scalar;

        float[] startingPos = new float[]{cameraPosition[0], cameraPosition[1], cameraPosition[2]};
        float[] currentPosition = new float[3];
        float distance;
        float smallestDistance = Float.MAX_VALUE;

        RippleQuiltSegment closestSegment = null;

        for (RippleQuiltSegment segment : mSegments)
        {
            scalar = (0 - cameraPosition[2]) / dir[2];
            currentPosition[0] = cameraPosition[0] + scalar * dir[0];
            currentPosition[1] = cameraPosition[1] + scalar * dir[1];
            //currentPosition[2] = cameraPosition[2] + scalar * dir[2];
            //for (int i = 0; i <  250; i+= 5)

            distance = (float) Math.sqrt((currentPosition[0] - segment.getPosition().x) * (currentPosition[0] - segment.getPosition().x) +
                    (currentPosition[1] - segment.getPosition().y) * (currentPosition[1] - segment.getPosition().y));

            if (distance < smallestDistance)
            {
                smallestDistance = distance;
                closestSegment = segment;
            }
        }

        if (closestSegment != null)
        {
            beginAnimationFromSegment(closestSegment);
        }
    }

    private void beginAnimationFromSegment(RippleQuiltSegment segment)
    {
        if (mIsAnimating)
            return;

        mIsAnimating = true;
        mUnAnimatedSegments.clear();
        mUnAnimatedSegments.addAll(mSegments);
        mTotalElapsedRippleAnimationTime = 0;
        mRippleOrigin = new PointF(segment.getPosition().x, segment.getPosition().y);
    }

    private void normalizeVector(float[] vector)
    {
        float length = (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
    }

}
