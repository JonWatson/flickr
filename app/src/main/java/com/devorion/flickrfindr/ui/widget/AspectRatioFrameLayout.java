package com.devorion.flickrfindr.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static android.view.View.MeasureSpec.*;

// Ratio = W / H
public class AspectRatioFrameLayout extends FrameLayout {

    public AspectRatioFrameLayout(Context context) {
        super(context);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getRatio() == 0) {
            // if no ratio, just treat as a normal Frame layout
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            switch (params.width) {
                case LayoutParams.WRAP_CONTENT:
                    break;
                default:
                    if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
                        int width = getSize(widthMeasureSpec);
                        int height = Math.round(width / getRatio());
                        super.onMeasure(makeMeasureSpec(width, EXACTLY), MeasureSpec.makeMeasureSpec(height, EXACTLY));
                        return;
                    }
            }

            switch (params.height) {
                case LayoutParams.WRAP_CONTENT:
                    break;
                default:
                    if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
                        int height = getSize(heightMeasureSpec);
                        int width = Math.round(height * getRatio());
                        super.onMeasure(makeMeasureSpec(width, EXACTLY), MeasureSpec.makeMeasureSpec(height, EXACTLY));
                        return;
                    }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getRatio() {
        Object tag = getTag();
        if (tag != null) {
            return Float.valueOf(tag.toString());
        } else {
            // just treat as a normal FrameLayout if no ratio is set
            return 0;
        }
    }
}
