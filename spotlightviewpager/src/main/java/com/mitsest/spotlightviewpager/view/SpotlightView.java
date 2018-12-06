package com.mitsest.spotlightviewpager.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.mitsest.spotlightviewpager.Commons;
import com.mitsest.spotlightviewpager.R;
import com.mitsest.spotlightviewpager.animation.AnimationDelegate;
import com.mitsest.spotlightviewpager.animation.OffsetDelegate;
import com.mitsest.spotlightviewpager.animation.OpacityDelegate;
import com.mitsest.spotlightviewpager.model.SpotlightViewModel;
import com.mitsest.spotlightviewpager.paint.SpotlightPaint;

import java.util.List;


public class SpotlightView extends ViewGroup implements ViewTreeObserver.OnGlobalLayoutListener {

    @NonNull private final SpotlightPaint spotlight;
    @NonNull private final OffsetDelegate offsetDelegate;
    @NonNull private final OpacityDelegate backgroundOpacityDelegate;
    @NonNull private final Paint backgroundPaint;
    @NonNull private final Paint borderPaint;
    @NonNull private final Paint borderGradientPaint;
    @NonNull private final OnSwipeTouchListener swipeTouchListener;

    @Nullable private SpotlightViewModel firstTarget;
    @Nullable private SpotlightViewModel animatingRectangle; // Used in draw (its scale and bounds are changing)

    @Px private final int spotlightPadding;
    @Px private final int spotlightPulseAnimationSize;

    // Defaults
    private int backgroundOpacityAnimationDuration = 800; // ms
    private int textOpacityAnimationDuration = 600; // ms
    private int spotlightGrowAnimationDuration = 300; // ms
    private int spotlightPulseAnimationDuration = 1200; // ms
    private int moveAnimationDuration = 750; // ms
    private int closeAnimationDuration = 220; // ms

    private boolean shouldDrawText; // text should be drawn only when the spotlight is not moving

    public SpotlightView(@NonNull Context context) {
        this(context, null);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        spotlight = new SpotlightPaint(context);
        borderGradientPaint = new Paint(spotlight.getBorderGradientPaint());
        borderPaint = new Paint(spotlight.getBorderPaint());
        backgroundPaint = new Paint();
        offsetDelegate = new OffsetDelegate();
        backgroundOpacityDelegate = new OpacityDelegate();
        swipeTouchListener = new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                showNext();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                showPrevious();
            }

            @Override
            public void onClick() {
                super.onClick();
                showNext();
            }
        };

        spotlightPadding = Commons.getDimenInPixels(context, R.dimen.spotlight_padding);
        spotlightPulseAnimationSize = Commons.getDimenInPixels(context, R.dimen.spotlight_pulse_animation_size);
        init(context);
    }

    private void showNext() {
        if (animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getNext() == null) {
            animateClose();
            return;
        }

        animateMove(animatingRectangle.getNext());

    }

    private void showPrevious() {
        if (animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getPrevious() == null) {
            return;
        }

        animateMove(animatingRectangle.getPrevious());
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        offsetDelegate.onLayout(this, changed, l, t, r, b);
    }

    @Override
    public void onGlobalLayout() {
        if (!offsetDelegate.isOnLayoutRan() || firstTarget == null) {
            return;
        }

        Commons.removeOnGlobalLayoutListener(this, this);

        SpotlightViewModel viewModel = firstTarget;

        int page = 0;
        while (viewModel != null) {
            page++;

            final View targetView = viewModel.getTargetView();
            if (targetView == null) {
                return;
            }

            RectF rectF = offsetDelegate.getRectFFromView(targetView, spotlightPadding);
            viewModel.setMaxWidth(getWidth());
            viewModel.setMaxBottom(getBottom());
            viewModel.setPage(page);
            viewModel.setRectF(rectF);

            viewModel = viewModel.getNext();
        }

        animateBackground(firstTarget);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        spotlight.drawSpotlightBorder(canvas, animatingRectangle);
        spotlight.drawSpotlight(canvas, animatingRectangle);

        if (shouldDrawText && animatingRectangle != null) {
            animatingRectangle.drawText(canvas);
        }
    }

    private void init(@NonNull Context context) {
        setVisibility(View.GONE);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        initBackgroundColor(context);
        initBackgroundPaintColor(context);

        setOnClickListener(null);
        setOnTouchListener(swipeTouchListener);
    }

    private void initBackgroundColor(@NonNull Context context) {
        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
    }

    private void initBackgroundPaintColor(@NonNull Context context) {
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.spotlight_overlay_color));
    }


    private void drawBackground(Canvas canvas) {
        backgroundPaint.setAlpha(backgroundOpacityDelegate.getOpacity());
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
    }


    /*
     * ----------------------------------
     * Background animation
     * ----------------------------------
     */
    private void animateBackground(final @NonNull SpotlightViewModel viewModel) {
        new AnimationDelegate(new ValueAnimator[]{backgroundOpacityDelegate.getOpacityAnimator()}).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onBackgroundEnd(viewModel);
                    }
                }, backgroundOpacityAnimationDuration, new LinearInterpolator());
    }

    private void onBackgroundEnd(final @NonNull SpotlightViewModel viewModel) {
        animateGrow(viewModel);
    }


    /*
     * ----------------------------------
     * Grow animation
     * ----------------------------------
     */
    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;

        new AnimationDelegate(getGrowAnimators()).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        onGrowStart();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onGrowEnd(viewModel);
                    }
                }, spotlightGrowAnimationDuration, new AccelerateDecelerateInterpolator());
    }

    @NonNull ValueAnimator[] getGrowAnimators() {
        if (animatingRectangle == null) {
            return new ValueAnimator[1];
        }

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.right);

        return new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim};
    }

    private void onGrowStart() {
        clearBorderPaint();
    }

    private void onGrowEnd(final @NonNull SpotlightViewModel viewModel) {
        undoClearBorderPaint();
        animatePulse(viewModel);

    }

    /*
     * ----------------------------------
     * Pulse animation
     * ----------------------------------
     */
    private void animatePulse(@NonNull final SpotlightViewModel viewModel) {
        if (animatingRectangle == null) {
            return;
        }

        new AnimationDelegate(getPulseAnimators()).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onPulseEnd(viewModel);
                    }
                }, spotlightPulseAnimationDuration, new OvershootInterpolator());

    }

    @NonNull ValueAnimator[] getPulseAnimators() {
        if (animatingRectangle == null) {
            return new ValueAnimator[1];
        }

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, animatingRectangle.top - spotlightPulseAnimationSize, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, animatingRectangle.left - spotlightPulseAnimationSize, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, animatingRectangle.bottom + spotlightPulseAnimationSize, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, animatingRectangle.right + spotlightPulseAnimationSize, animatingRectangle.right);


        return new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim};
    }

        private void onPulseEnd(@NonNull final SpotlightViewModel viewModel) {
        animateText(viewModel);
        shouldDrawText = true;
    }

    /*
     * ----------------------------------
     * Text animation
     * ----------------------------------
     */
    private void animateText(final @NonNull SpotlightViewModel viewModel) {
        new AnimationDelegate(new ValueAnimator[]{viewModel.getTextOpacityAnimation()}).animate(this,
                textOpacityAnimationDuration, new LinearInterpolator());

    }

    /*
     * ----------------------------------
     * Move animation
     * ----------------------------------
     */
    private void animateMove(@NonNull final SpotlightViewModel viewModel) {
        if (animatingRectangle == null) {
            return;
        }

        final SpotlightViewModel tempViewModel = animatingRectangle;
        animatingRectangle = viewModel;

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", tempViewModel.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", tempViewModel.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", tempViewModel.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", tempViewModel.right, viewModel.right);

        new AnimationDelegate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim}).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        onMoveStart();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onMoveEnd(viewModel);
                    }
                }, moveAnimationDuration, new FastOutSlowInInterpolator());
    }

    private void onMoveStart() {
        shouldDrawText = false;
    }

    private void onMoveEnd(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;
        animatePulse(viewModel);
    }

    /*
     * ----------------------------------
     * Close animation
     * ----------------------------------
     */
    public void animateClose() {
        if (animatingRectangle == null) {
            return;
        }

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, 0);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, 0);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, getHeight());
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, getWidth());
        final ValueAnimator radiusAnim = ValueAnimator.ofInt(spotlight.getRadius(), 0);

        new AnimationDelegate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim, radiusAnim}).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        onCloseStart();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onCloseEnd();
                    }
                }, closeAnimationDuration, new DecelerateInterpolator());

    }

    private void onCloseStart() {
        shouldDrawText = false;
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
    }

    private void onCloseEnd() {
        setVisibility(View.GONE);
        reset();
    }


    private void reset() {
        shouldDrawText = false;

        spotlight.setRadius(getContext());
        undoClearBorderPaint();
    }

    private void clearBorderPaint() {
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
        postInvalidate();
    }

    private void undoClearBorderPaint() {
        spotlight.setBorderGradientPaint(borderGradientPaint);
        spotlight.setBorderPaint(borderPaint);
    }

    /*
     * ----------------------------------
     * Public API
     * ----------------------------------
     */
    private void setModels(@Nullable List<SpotlightViewModel> targets) {
        if (targets == null || targets.size() <= 0) {
            return;
        }

        final int size = targets.size();
        for (int i = 0; i < size; i++) {
            final SpotlightViewModel viewModel = targets.get(i);

            viewModel.setNumberOfPages(size);

            if (i <= size - 2) {
                viewModel.setNext(targets.get(i + 1));
            }
        }

        this.firstTarget = targets.get(0);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
        setVisibility(View.VISIBLE);
    }

    public static void addSpotlightView(@NonNull Activity activity, @NonNull SpotlightView spotlightView, @NonNull List<SpotlightViewModel> models) {
        final ViewGroup rootLayout = activity.findViewById(android.R.id.content);
        rootLayout.addView(spotlightView);
        spotlightView.setModels(models);
    }

    public static class Builder {
        @NonNull private final SpotlightView spotlightView;

        private Builder(@NonNull Context context) {
            this.spotlightView = new SpotlightView(context);
        }

        public static Builder getInstance(@NonNull Context context) {
            return new Builder(context);
        }

        public Builder setBackgroundOpacityAnimationDuration(int backgroundOpacityAnimationDuration) {
            spotlightView.backgroundOpacityAnimationDuration = backgroundOpacityAnimationDuration;
            return this;
        }
        public Builder setTextOpacityAnimationDuration(int textOpacityAnimationDuration) {
            spotlightView.textOpacityAnimationDuration = textOpacityAnimationDuration;
            return this;
        }
        public Builder setSpotlightGrowAnimationDuration(int spotlightGrowAnimationDuration) {
            spotlightView.spotlightGrowAnimationDuration = spotlightGrowAnimationDuration;
            return this;
        }
        public Builder setSpotlightPulseAnimationDuration(int spotlightPulseAnimationDuration) {
            spotlightView.spotlightPulseAnimationDuration = spotlightPulseAnimationDuration;
            return this;
        }
        public Builder setMoveAnimationDuration(int moveAnimationDuration) {
            spotlightView.moveAnimationDuration = moveAnimationDuration;
            return this;
        }
        public Builder setCloseAnimationDuration(int closeAnimationDuration) {
            spotlightView.closeAnimationDuration = closeAnimationDuration;
            return this;
        }
        @NonNull public SpotlightView build() {
            return spotlightView;
        }
    }
}