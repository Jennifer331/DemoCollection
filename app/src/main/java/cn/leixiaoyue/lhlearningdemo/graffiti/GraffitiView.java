package cn.leixiaoyue.lhlearningdemo.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.Stack;

/**
 * Created by Lei Xiaoyue on 2015-12-15.
 */
public class GraffitiView extends ImageView {
    private static final String TAG = "GraffitiView";
    private static final int INVALID_STEP = -1;

    private Path mPath;
    private Paint mPaint;
    private Stack<GraffitiPath> mGraffitiPaths;
    private Bitmap mDrawingBuffer;
    private int mCurrentBufferingStep = INVALID_STEP;
    private int mCurrentPointId;
    private Uri mBitmapPath;
    private RectF mDestBounds;
    private float mLastX, mLastY;
    private boolean mOnePointFlag;

    public GraffitiView(Context context) {
        this(context, null);
    }

    public GraffitiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.v(TAG, "width:" + getWidth() + " height:" + getHeight());
        refreshPaintArea();
    }

    private void buildDrawingBuffer() {
        int width = (int)mDestBounds.width();
        int height = (int)mDestBounds.height();
        Log.v(TAG, "width:" + getWidth() + " height:" + getHeight());
        if (0 == width || 0 == height) {
            return;
        }
        if(!isDrawingBufferValid()){
            mDrawingBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCurrentBufferingStep = INVALID_STEP;
        }else if(mDrawingBuffer.getWidth() != width || mDrawingBuffer.getHeight() != height){
            mDrawingBuffer.recycle();
            mDrawingBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCurrentBufferingStep = INVALID_STEP;
        }else{
            Canvas canvas = new Canvas(mDrawingBuffer);
            canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
            mCurrentBufferingStep = INVALID_STEP;
        }
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraffitiPaths = new Stack<GraffitiPath>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawingBufferValid() && mCurrentBufferingStep != INVALID_STEP) {
            canvas.drawBitmap(mDrawingBuffer, 0, 0, null);
        }
        int drawingStepCount = mGraffitiPaths.size();
        for (int i = mCurrentBufferingStep < 0 ? 0 : mCurrentBufferingStep; i < drawingStepCount; i++) {
            GraffitiPath graffitiPath = mGraffitiPaths.get(i);
            canvas.drawPath(graffitiPath.path, graffitiPath.paint);
        }
    }

    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mGraffitiPaths.add(new GraffitiPath(mPath, mPaint));
        mLastX = x;
        mLastY = y;
        mOnePointFlag = true;
    }

    private void touchMove(float x, float y) {
        GraffitiPath graffitiPath = mGraffitiPaths.peek();
        if (Math.abs(x - mLastX) > Config.BEZIER_START_EDGE
                || Math.abs(y - mLastY) > Config.BEZIER_START_EDGE) {
            graffitiPath.path.quadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
            mLastX = x;
            mLastY = y;
            if (mOnePointFlag) {
                mOnePointFlag = false;
            }
        }
    }

    private void touchUp(float x, float y) {
        GraffitiPath graffitiPath = mGraffitiPaths.peek();
        if (x == mLastX && y == mLastY && mOnePointFlag) {
            graffitiPath.path.addCircle(x, y, graffitiPath.paint.getStrokeWidth() / 2, Path.Direction.CW);
            Log.v(TAG, "[touchUp] 点" + x + "," + y);
        } else {
            graffitiPath.path.lineTo(mLastX, mLastY);
        }
        mPath.reset();

        int currentSteps = mGraffitiPaths.size() - 1;
        if(currentSteps - mCurrentBufferingStep >= Config.BUFFER_FACTOR){
            drawRenderBuffer(mCurrentBufferingStep + 1, currentSteps);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        refreshPaintArea();
        Log.v(TAG, mDestBounds + "");
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (x < mDestBounds.left || x > mDestBounds.right
                || y < mDestBounds.top || y > mDestBounds.bottom) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mCurrentPointId = event.getPointerId(0);
                touchStart(x, y);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (event.getPointerId(0) != mCurrentPointId) {
                    break;
                }
                touchMove(x, y);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                touchUp(x, y);
                break;
            }
        }
        invalidate();
        return true;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        refreshPaintArea();
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        mPaint.setStrokeWidth(width);
    }

    public boolean undo() {
        if (null == mGraffitiPaths || mGraffitiPaths.isEmpty()) {
            return false;
        }
        mGraffitiPaths.pop();
        int drawingStep = mGraffitiPaths.size() - 1;
        if(mCurrentBufferingStep > drawingStep){
            buildDrawingBuffer();
            drawRenderBuffer(0, drawingStep);
        }
        invalidate();
        return true;
    }

    public void setSrc(Uri uri) {
        this.mBitmapPath = uri;
        setImageURI(uri);
        mGraffitiPaths.clear();
        refreshPaintArea();
        buildDrawingBuffer();
    }

    private void refreshPaintArea() {
        mDestBounds = new RectF(getDrawable().getBounds());
        Matrix matrix = getImageMatrix();
        matrix.mapRect(mDestBounds);
        buildDrawingBuffer();
    }

    public Bitmap getResult() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        Bitmap result = Bitmap.createBitmap(bitmap, (int) mDestBounds.left, (int) mDestBounds.top,
                (int) mDestBounds.width(), (int) mDestBounds.height());
        bitmap.recycle();
        bitmap = null;
        setImageBitmap(result);
        return result;
    }

    /**
     * draw paths from start(inclusively) to end(inclusively) to the buffer
     * @param start
     *             not less than {@value #INVALID_STEP}
     * @param end
     *             not less than both INVALID_STEP and from
     * Special cases explanation:
     *             from == INVALID_STEP       means to clear the buffer and redraw all the paths
     *             to == from == INVALID_STEP means clear the buffer
     */

    private void drawRenderBuffer(int start, int end) {
        if (!isDrawingBufferValid() || start > end) {
            return;
        }

        Canvas canvas      = new Canvas(mDrawingBuffer);
        canvas.save();
        if(start == INVALID_STEP){
            start = 0;
        }
        if(mGraffitiPaths.size() - 1 < end){
            end = mGraffitiPaths.size() - 1;
        }
        if (0 == start) {
            canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        }
        for (int i = start; i <= end; i++) {
            GraffitiPath path = mGraffitiPaths.get(i);
            canvas.drawPath(path.path, path.paint);
            mCurrentBufferingStep = i;
        }
        canvas.restore();
    }

    private boolean isDrawingBufferValid(){
        return (null != mDrawingBuffer) && (!mDrawingBuffer.isRecycled());
    }

}
