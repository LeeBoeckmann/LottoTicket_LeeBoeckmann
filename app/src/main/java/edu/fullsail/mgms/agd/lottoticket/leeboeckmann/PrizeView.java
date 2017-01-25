package edu.fullsail.mgms.agd.lottoticket.leeboeckmann;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.graphics.Rect;
import android.graphics.Point;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.graphics.Paint.Align;
import android.media.MediaPlayer;

/**
 * Created by LeeBoeckmann on 1/18/17.
 */

public class PrizeView
    extends SurfaceView
    implements SurfaceHolder.Callback, View.OnTouchListener {

    private SurfaceHolder   mHolder;
    Rect   mDimensions;
    Rect   mPlacement;
    Point  mCentroid;
    Bitmap mBMPPrize;
    Bitmap mBMPResin;
    Paint  mAlphaPaint;
    Paint  mTextPaint;
    int    mTouchRadius;
    int[]  mPixelBuffer;
    int    mResinPixels;

    MainActivity mActivity;

    PrizeValue mPrizeValue;

    public enum PrizeValue
    {
        MISS, GOAL, ALMOST
    }

    public void setPrizeValue(PrizeValue value)
    {
        mPrizeValue = value;
    }

    public PrizeView(Context context)
    {
        super(context);
        initialize(context);
    }

    public PrizeView(Context context, AttributeSet attrs)
    {
        super(context);
        initialize(context);
    }

    public PrizeView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context);
        initialize(context);
    }

    private void initialize(Context context)
    {
        getHolder().addCallback(this);
        setOnTouchListener(this);
        setZOrderOnTop(true);

        mActivity = (MainActivity)context;

        mDimensions = new Rect();
        mPlacement = new Rect();
        mCentroid = new Point();

        mAlphaPaint = new Paint();
        mAlphaPaint.setStyle(Paint.Style.FILL);
        mAlphaPaint.setARGB(255,0,0,0);
        mAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mTextPaint = new Paint();
        mTextPaint.setARGB(255, 255, 255, 255);
        mTextPaint.setTextSize(100.0f);
        mTextPaint.setTextAlign(Align.CENTER);

        mTouchRadius = 40;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mHolder = holder;
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        mBMPPrize = BitmapFactory.decodeResource(getResources(), R.drawable.prize1);

        Bitmap bmpResinOrig = BitmapFactory.decodeResource(getResources(), R.drawable.resin);
        mBMPResin = Bitmap.createBitmap(bmpResinOrig.getWidth(), bmpResinOrig.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(mBMPResin);
        can.drawBitmap(bmpResinOrig, null, new Rect(0, 0, bmpResinOrig.getWidth(), bmpResinOrig.getHeight()), null);

        Canvas c = mHolder.lockCanvas();

        mDimensions.set(0, 0, c.getWidth(), c.getHeight());
        mCentroid.set(mDimensions.width()/2, mDimensions.height()/2);

        mBMPPrize = scaleBitmap(mBMPPrize, ((float)mDimensions.width())/((float)mBMPPrize.getWidth()));
        mBMPResin = scaleBitmap(mBMPResin, ((float)mDimensions.width())/((float)mBMPResin.getWidth()));

        mPlacement.set(0, 0, mBMPPrize.getWidth(), mBMPPrize.getHeight());
        mPlacement.offset(mCentroid.x -(mBMPPrize.getWidth()/2), mCentroid.y - (mBMPPrize.getHeight()/2));

        mPixelBuffer = new int[(mBMPResin.getWidth() * mBMPResin.getHeight())];

        mResinPixels = 0;

        mBMPResin.getPixels(mPixelBuffer, 0, mBMPResin.getWidth(), 0, 0, mBMPResin.getWidth(), mBMPResin.getHeight());

        for(int i = 0; i < mPixelBuffer.length; ++i)
        {
            if (((mPixelBuffer[i] >> 24) & 0xFF) > 0)
            {
                ++mResinPixels;
            }
        }

        draw(c);
        mHolder.unlockCanvasAndPost(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int x, int y, int z)
    {

    }

    @Override
    public void draw(Canvas canvas) {

        if(mBMPPrize != null && mBMPResin != null)
        {
            canvas.drawBitmap(mBMPPrize, null, mPlacement, null);

            if(mPrizeValue == PrizeValue.GOAL)
            {
                canvas.drawText("$500", (float)mCentroid.x, (float)mCentroid.y, mTextPaint);
            }
            else if(mPrizeValue == PrizeValue.MISS)
            {
                canvas.drawText("$0", (float)mCentroid.x, (float)mCentroid.y, mTextPaint);
            }
            else
            {
                canvas.drawText("$50", (float)mCentroid.x, (float)mCentroid.y, mTextPaint);
            }

            canvas.drawBitmap(mBMPResin, null, mPlacement, null);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if(mActivity.isGameOver())
        {
            return false;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_MOVE)
        {
            mActivity.playScratchCheer();

            Canvas can = new Canvas(mBMPResin);
            can.drawCircle(motionEvent.getX() - mPlacement.left, motionEvent.getY() - mPlacement.top, mTouchRadius, mAlphaPaint);

            Canvas c = mHolder.lockCanvas();
            draw(c);
            mHolder.unlockCanvasAndPost(c);

            int numResinPixels = 0;
            mBMPResin.getPixels(mPixelBuffer, 0, mPlacement.width(), 0, 0, mPlacement.width(), mPlacement.height());
            for(int i = 0; i < mPixelBuffer.length; ++i)
            {
                if(((mPixelBuffer[i] >> 24) & 0xFF) > 0)
                {
                    ++numResinPixels;
                }
            }

            if((float)numResinPixels/(float)mResinPixels <= 0.3f)
            {
                mActivity.setPrize(mPrizeValue);
                mActivity.setGameOver();
            }
        }
        return false;
    }

    private Bitmap scaleBitmap(Bitmap bm, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
    }
}
