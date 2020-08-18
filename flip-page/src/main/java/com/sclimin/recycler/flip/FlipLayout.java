package com.sclimin.recycler.flip;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FlipLayout extends FrameLayout {

    private final Rect mBounds = new Rect();
    private final Rect mRect = new Rect();

    private final Camera mCamera = new Camera();
    private final Matrix mMatrix = new Matrix();

    private final Paint mPaint = new Paint();

    private final static int FLIP_START = 0;
    private final static int FLIP_END = 1;
    private final static int FLIP_TOP = 2;
    private final static int FLIP_BOTTOM = 3;

    int mDegree;
    @RecyclerView.Orientation
    int mOrientation;

    public FlipLayout(@NonNull Context context) {
        this(context, null);
    }

    public FlipLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        mBounds.set(getPaddingStart(), getPaddingTop(),
                w - getPaddingEnd(), h - getPaddingBottom());
    }

    @Override
    public void draw(Canvas canvas) {
        final int absDegree = Math.abs(mDegree);
        if (mDegree == 0) {
            super.draw(canvas);
        }

        int flip;
        if (mOrientation == RecyclerView.VERTICAL) {
            flip = mDegree < 0 ? FLIP_TOP : FLIP_BOTTOM;
        }
        else {
            flip = mDegree < 0 ? FLIP_START : FLIP_END;
        }

        if (absDegree >= 90) {
            clip2(canvas, flip, absDegree);
        }
        else {
            flip(canvas, mDegree, flip);
            clip(canvas, flip);
        }
    }

    private Matrix computeFlipMatrix(int degree) {
        final Camera camera = mCamera;
        final Matrix matrix = mMatrix;
        final int orientation = mOrientation;
        final Rect bounds = mBounds;

        matrix.reset();

        camera.save();
        camera.setLocation(0, 0, -40);
        if (orientation == RecyclerView.VERTICAL) {
            camera.rotateX(degree);
        }
        else {
            camera.rotateY(-degree);
        }
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-bounds.centerX(), -bounds.centerY());
        matrix.postTranslate(bounds.centerX(), bounds.centerY());

        return matrix;
    }

    private void flip(Canvas canvas, int degree, int flip) {
        canvas.save();

        final Rect rect = mRect;
        final Rect bounds = mBounds;
        final Paint paint = mPaint;

        canvas.getClipBounds(rect);
        switch (flip) {
            case FLIP_START:
                rect.right = bounds.centerX();
                break;
            case FLIP_END:
                rect.left = bounds.centerX();
                break;
            case FLIP_TOP:
                rect.bottom =  bounds.centerY();
                break;
            case FLIP_BOTTOM:
            default:
                rect.top = bounds.centerY();
                break;
        }

        canvas.clipRect(rect);
        canvas.concat(computeFlipMatrix(degree));

        super.draw(canvas);

        paint.setColor(Color.argb((int) (0x99 * Math.abs(degree) / 90.f), 0, 0, 0));
        canvas.drawRect(bounds, paint);

        canvas.restore();
    }

    private void clip(Canvas canvas, int flip) {
        canvas.save();

        final Rect rect = mRect;
        final Rect bounds = mBounds;

        canvas.getClipBounds(rect);
        switch (flip) {
            case FLIP_START:
                rect.left = bounds.centerX();
                break;
            case FLIP_END:
                rect.right = bounds.centerX();
                break;
            case FLIP_TOP:
                rect.top =  bounds.centerY();
                break;
            case FLIP_BOTTOM:
            default:
                rect.bottom = bounds.centerY();
                break;
        }
        canvas.clipRect(rect);
        super.draw(canvas);

        canvas.restore();
    }

    private void clip2(Canvas canvas, int flip, int absDegree) {
        canvas.save();

        final Rect rect = mRect;
        final Rect bounds = mBounds;
        final Paint paint = mPaint;

        canvas.getClipBounds(rect);
        switch (flip) {
            case FLIP_START:
                rect.left = bounds.centerX();
                break;
            case FLIP_END:
                rect.right = bounds.centerX();
                break;
            case FLIP_TOP:
                rect.top =  bounds.centerY();
                break;
            case FLIP_BOTTOM:
            default:
                rect.bottom = bounds.centerY();
                break;
        }
        canvas.clipRect(rect);
        super.draw(canvas);
        paint.setColor(Color.argb((int) (0x99 * (absDegree - 90.f) / 90.f), 0, 0, 0));
        canvas.drawRect(bounds, paint);
        canvas.restore();
    }
}
