package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

public class SpotlightViewWrapper extends FrameLayout implements View.OnClickListener, SpotlightView.SpotlightViewInterface,
        ViewTreeObserver.OnGlobalLayoutListener {

    private @NonNull WeakReference<SpotlightView> spotlightView;
    private @NonNull int[] offsetArray;

    public SpotlightViewWrapper(@NonNull Context context) {
        super(context);

        spotlightView = new WeakReference<>(new SpotlightView(context));
        offsetArray = new int[2];

        init(context);
    }

    public SpotlightViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        spotlightView = new WeakReference<>(new SpotlightView(context));
        offsetArray = new int[2];

        init(context);
    }

    public SpotlightViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        spotlightView = new WeakReference<>(new SpotlightView(context));
        offsetArray = new int[2];

        init(context);
    }


    private void init(@NonNull Context context) {
        setVisibility(View.GONE);
        addSpotlightView(context);

    }


    private void addSpotlightView(@NonNull Context context) {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }

        spotlightView.setListener(this);

        LayoutParams spotlightViewParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        addView(spotlightView, spotlightViewParams);
    }

    @Override
    public void onClick(View v) {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }
        spotlightView.animateClose();
    }

    @Override
    public void onPageChanged(boolean isLastPage) {
    }

    @Override
    public void onCloseAnimationFinish() {
        setVisibility(View.GONE);
    }

    public void initView() {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }
        spotlightView.initView();
    }

    public int getSpotLightPadding() {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return 0;
        }


        return spotlightView.getSpotLightPadding();
    }

    @Nullable
    public SpotlightView getSpotlightView() {
        return spotlightView.get();
    }

    public @Nullable RectF getRectFFromView(@Nullable View spotlightView) {
        if (spotlightView == null) {
            return null;
        }

        int[] positionOnScreen = new int[2];
        spotlightView.getLocationInWindow(positionOnScreen);

        float rectLeft = computeStartingLeft(positionOnScreen, offsetArray, getSpotLightPadding());
        float rectRight = computeStartingRight(positionOnScreen, offsetArray, getSpotLightPadding(), spotlightView);
        float rectTop = computeStartingTop(positionOnScreen, offsetArray, getSpotLightPadding());
        float rectBottom = computeStartingBottom(positionOnScreen, offsetArray, getSpotLightPadding(), spotlightView);

        return new RectF(rectLeft, rectTop, rectRight, rectBottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getLocationInWindow(offsetArray);
    }

    private static float getStartingLeft(int[] positionOnScreenArray, int[] offsetArray) {
        return positionOnScreenArray[0] - offsetArray[0];
    }

    private static float getStartingTop(int[] positionOnScreenArray, int[] offsetArray) {
        return positionOnScreenArray[1] - offsetArray[1];
    }

    private static float computeStartingLeft(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding) {
        return getStartingLeft(positionOnScreenArray, offsetArray) - spotLightPadding;
    }


    private static float computeStartingTop(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding) {
        return getStartingTop(positionOnScreenArray, offsetArray) - spotLightPadding;
    }


    private static float computeStartingRight(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingLeft(positionOnScreenArray, offsetArray)  + spotlightView.getWidth() + spotLightPadding;
    }

    private static float computeStartingBottom(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingTop(positionOnScreenArray, offsetArray) + spotlightView.getHeight() + spotLightPadding;
    }

    public void setFirstTarget(@NonNull SpotlightViewModel target) {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }

        spotlightView.setFirstTarget(target);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        setVisibility(View.VISIBLE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void animateClose() {
        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }


        spotlightView.animateClose();

    }

    @Override
    public void onGlobalLayout() {
        Commons.removeOnGlobalLayoutListenerTG(this, this);

        final SpotlightView spotlightView = getSpotlightView();

        if (spotlightView == null) {
            return;
        }


        SpotlightViewModel viewModel, firstTarget;
        viewModel = firstTarget = spotlightView.getFirstTarget();

        if (viewModel == null) {
            return;
        }

        RectF rectF = getRectFFromView(viewModel.getSpotlightView());
        if (rectF != null) {
            viewModel.setRectF(rectF);
        }

        while (viewModel.getNext() != null) {
            viewModel = viewModel.getNext();
            if (viewModel == null) {
                break;
            }

            rectF = getRectFFromView(viewModel.getSpotlightView());
            if (rectF == null) {
                continue;
            }

            viewModel.setRectF(rectF);
        }

        setFirstTarget(firstTarget);
        initView();
    }
}
