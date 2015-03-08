package com.wix.mobile.ripplequilt;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;


public class RippleQuiltActivity extends Activity
{
    private RippleQuiltSurfaceView mRippleQuiltSurfaceView;
    private RippleQuiltRenderer mRippleQuiltRenderer;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private float mPrevDeltaX;
    private float mPrevDeltaY;

    private float mPrevX;
    private float mPrevY;

    private boolean mIsDecellerating;

    private float mWidth;
    private float mHeight;

    private long mTouchDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ripple_quilt);

        mRippleQuiltRenderer = new RippleQuiltRenderer(this);
        mRippleQuiltSurfaceView = (RippleQuiltSurfaceView)findViewById(R.id.rippleQuiltSurfaceView);

        mRippleQuiltSurfaceView.setRenderer(mRippleQuiltRenderer);

        mTimer = new Timer();
        mTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (mIsDecellerating)
                    performDecellerate();

                mRippleQuiltSurfaceView.requestRender();
            }
        };

        mTimer.schedule(mTimerTask, 1000 / 40, 1000 / 40);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
    }

    private void performDecellerate()
    {
        mPrevDeltaX *= 0.9f;
        mPrevDeltaY *= 0.9f;

        mRippleQuiltRenderer.moveBy(mPrevDeltaX * 0.05f, mPrevDeltaY * 0.05f);
        mRippleQuiltSurfaceView.requestRender();

        if (getDeltaDistance() <= 1f)
            mIsDecellerating = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                mTouchDownTime = System.currentTimeMillis();
                mIsDecellerating = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - mPrevX;
                float deltaY = event.getY() - mPrevY;

                mPrevX = event.getX();
                mPrevY = event.getY();

                mPrevDeltaX = deltaX;
                mPrevDeltaY = deltaY;

                mRippleQuiltRenderer.moveBy(deltaX * 0.05f, deltaY * 0.05f);
                mRippleQuiltSurfaceView.requestRender();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (getDeltaDistance() > 1f)
                    mIsDecellerating = true;

                if (System.currentTimeMillis() - mTouchDownTime < 250)
                    mRippleQuiltRenderer.setClickCoords(event.getX(), event.getY(), mWidth, mHeight);

                break;
        }

        return true;// super.onTouchEvent(event);
    }

    private float getDeltaDistance()
    {
        float distance = (float)Math.sqrt(mPrevDeltaX * mPrevDeltaX + mPrevDeltaY * mPrevDeltaY);
        return distance;
    }

}
