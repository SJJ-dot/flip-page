package com.sclimin.recycler.flip;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class FlipLayoutManager extends RecyclerView.LayoutManager {

    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;

    @RecyclerView.Orientation
    private final int mOrientation;

    private int mOffset;
    private int mMaxOffset;
    private int mItemLength;

    public FlipLayoutManager(Context context) {
        this(context, null, 0, 0);
    }

    public FlipLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlipLayoutManager,
                defStyleAttr, defStyleAttr);
        mOrientation = ta.getInt(R.styleable.FlipLayoutManager_android_orientation, VERTICAL);
        ta.recycle();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int itemCount = state.getItemCount();
        mItemLength = mOrientation == RecyclerView.VERTICAL ? getHeight() : getWidth();
        mMaxOffset = itemCount > 0 ? ((itemCount - 1) * mItemLength) : 0;
        if (mOffset > mMaxOffset) {
            mOffset = mMaxOffset;
        }

        detachAndScrapAttachedViews(recycler);
        fill(recycler, state);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    private int computeNewOffset(int dx) {
        int newOffset = mOffset + dx;
        if (newOffset > mMaxOffset) {
            newOffset = 0;
        }
        else if (newOffset < 0) {
            newOffset = 0;
        }
        int delta = newOffset - mOffset;
        mOffset = newOffset;
        return delta;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int delta = computeNewOffset(dx);
        if (delta != 0) {
            fill(recycler, state);
        }
        return delta;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int delta = computeNewOffset(dy);
        if (delta != 0) {
            fill(recycler, state);
        }
        return delta;
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        final int currentPosition = mOffset / mItemLength;
        final int nextPosition = currentPosition == state.getItemCount() - 1 ? -1 : currentPosition + 1;

        final int relativeOffset = mOffset - (currentPosition * mItemLength);
        final int degree = (int) ((float) relativeOffset / mItemLength * 180f);

        for (int i = 0; i < getChildCount(); i++) {
            FlipLayout child = childAt(i);
            int childPosition = getPosition(child);
            if (childPosition != currentPosition || childPosition != nextPosition) {
                removeAndRecycleViewAt(i, recycler);
            }
        }

        removeAllViews();

        FlipLayout layout;
        if (degree <= 90) {
            if (nextPosition > 0) {
                layout = fromRecycler(recycler, nextPosition);
                layout.mOrientation = mOrientation;
                layout.mDegree = degree - 180;

                addView(layout);
                measureChildWithMargins(layout, 0, 0);
                layoutDecoratedWithMargins(layout, getPaddingStart(), getPaddingTop(),
                        getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
            }

            layout = fromRecycler(recycler, currentPosition);
            layout.mDegree = degree;
            layout.mOrientation = mOrientation;
            addView(layout);
            measureChildWithMargins(layout, 0, 0);
            layoutDecoratedWithMargins(layout, getPaddingStart(), getPaddingTop(),
                    getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
        }
        else {
            layout = fromRecycler(recycler, currentPosition);
            layout.mDegree = degree;
            layout.mOrientation = mOrientation;
            addView(layout);
            measureChildWithMargins(layout, 0, 0);
            layoutDecoratedWithMargins(layout, getPaddingStart(), getPaddingTop(),
                    getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());


            if (nextPosition > 0) {
                layout = fromRecycler(recycler, nextPosition);
                layout.mOrientation = mOrientation;
                layout.mDegree = degree - 180;

                addView(layout);
                measureChildWithMargins(layout, 0, 0);
                layoutDecoratedWithMargins(layout, getPaddingStart(), getPaddingTop(),
                        getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
            }
        }
    }

    private FlipLayout childAt(int index) {
        return (FlipLayout) getChildAt(index);
    }

    private FlipLayout fromRecycler(RecyclerView.Recycler recycler, int position) {
        View view = recycler.getViewForPosition(position);
        if (view instanceof FlipLayout) {
            return (FlipLayout) view;
        }
        else {
            throw new RuntimeException("FlipLayoutManager child must be FlipLayout");
        }
    }
}
