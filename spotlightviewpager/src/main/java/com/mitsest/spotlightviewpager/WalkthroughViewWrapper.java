package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.mitsest.spotlightviewpager.R;

import java.lang.ref.WeakReference;

public class WalkthroughViewWrapper extends FrameLayout implements View.OnClickListener, WalkthroughView.WalkthroughViewInterface {

    private @NonNull WeakReference<WalkthroughView> walkthroughView;
    private @NonNull int[] offsetArray;

    public WalkthroughViewWrapper(@NonNull Context context) {
        super(context);

        walkthroughView = new WeakReference<>(new WalkthroughView(context));
        offsetArray = new int[2];

        init(context);
    }

    public WalkthroughViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        walkthroughView = new WeakReference<>(new WalkthroughView(context));
        offsetArray = new int[2];

        init(context);
    }

    public WalkthroughViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        walkthroughView = new WeakReference<>(new WalkthroughView(context));
        offsetArray = new int[2];

        init(context);
    }


    private void init(@NonNull Context context) {
        setVisibility(View.GONE);
        addWalkthroughView(context);

    }


    private void addWalkthroughView(@NonNull Context context) {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return;
        }

        walkthroughView.setListener(this);

        LayoutParams walkthroughViewParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        addView(walkthroughView, walkthroughViewParams);
    }

    @Override
    public void onClick(View v) {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return;
        }
        walkthroughView.animateClose();
    }

    @Override
    public void onPageChanged(boolean isLastPage) {
    }

    @Override
    public void onCloseAnimationFinish() {
        setVisibility(View.GONE);
    }

    public void setFirstTarget(@NonNull WalkthroughViewModel target) {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return;
        }

        walkthroughView.setFirstTarget(target);
    }

    public void initView() {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return;
        }
        walkthroughView.initView();
    }

    public int getSpotLightPadding() {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return 0;
        }


        return walkthroughView.getSpotLightPadding();
    }

    @Nullable
    public WalkthroughView getWalkthroughView() {
        return walkthroughView.get();
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

    public void initViews(final @NonNull WalkthroughViewModel firstTarget) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                WalkthroughViewModel viewModel = firstTarget;
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
        });

        setVisibility(VISIBLE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void animateClose() {
        final WalkthroughView walkthroughView = getWalkthroughView();

        if (walkthroughView == null) {
            return;
        }


        walkthroughView.animateClose();

    }

}
