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
    @Nullable private Paint backgroundPaint;
    @NonNull private final OnSwipeTouchListener swipeTouchListener;

    @Nullable private SpotlightViewModel firstTarget;
    @Nullable private SpotlightViewModel animatingRectangle; // Used in draw (its scale and bounds are changing)

    @Px private final int spotlightPadding;
    @Px private final int spotlightPulseAnimationSize;

    // Builder setters' defaults
    private int backgroundOpacityAnimationDuration = 800; // ms
    private int textOpacityAnimationDuration = 300; // ms
    private int spotlightGrowAnimationDuration = 400; // ms
    private int spotlightPulseAnimationDuration = 650; // ms
    private int moveAnimationDuration = 750; // ms
    private int closeAnimationDuration = 220; // ms
    private float growRatio = 0.5f;

    // Allow gestures only when not moving
    private boolean isMoving = false;

    // Constructors
    public SpotlightView(@NonNull Context context) {
        this(context, null);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        spotlight = new SpotlightPaint(context);
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
        if (animatingRectangle == null || isMoving) {
            return;
        }

        if (animatingRectangle.getNext() == null) {
            animateClose(animatingRectangle);
            return;
        }

        animateMove(animatingRectangle.getNext());

    }

    private void showPrevious() {
        if (animatingRectangle == null || isMoving) {
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

        if (animatingRectangle != null) {
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
        if (backgroundPaint != null) {
            backgroundPaint.setColor(ContextCompat.getColor(context, R.color.spotlight_overlay_color));
        }
    }


    private void drawBackground(Canvas canvas) {
        if (backgroundPaint != null) {
            backgroundPaint.setAlpha(backgroundOpacityDelegate.getOpacity());
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        }
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
        isMoving = true;

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

    @NonNull
    ValueAnimator[] getGrowAnimators() {
        if (animatingRectangle == null) {
            return new ValueAnimator[1];
        }

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.bottom - animatingRectangle.height() * growRatio, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left",
                animatingRectangle.right - animatingRectangle.width() * growRatio, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom",
                animatingRectangle.bottom - animatingRectangle.height() * growRatio, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right",
                animatingRectangle.right - animatingRectangle.width() * growRatio, animatingRectangle.right);

        return new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim};
    }

    private void onGrowStart() {

    }

    private void onGrowEnd(final @NonNull SpotlightViewModel viewModel) {
        animatePulse(viewModel);

    }

    /*
     * ----------------------------------
     * Pulse animation
     * ----------------------------------
     */
    private void animatePulse(@NonNull final SpotlightViewModel viewModel) {
        new AnimationDelegate(getPulseAnimators()).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onPulseEnd(viewModel);
                    }
                }, spotlightPulseAnimationDuration, new OvershootInterpolator());

    }

    @NonNull
    ValueAnimator[] getPulseAnimators() {
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
        isMoving = false;
        animateText(viewModel);
    }

    /*
     * ----------------------------------
     * Text animation
     * ----------------------------------
     */
    private void animateText(final @NonNull SpotlightViewModel viewModel) {
        new AnimationDelegate(new ValueAnimator[]{viewModel.getTextOpacityAnimation()}).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                },
                textOpacityAnimationDuration, new LinearInterpolator());

    }

    /*
     * ----------------------------------
     * Move animation
     * ----------------------------------
     */
    private void animateMove(@NonNull final SpotlightViewModel viewModel) {
        isMoving = true;

        new AnimationDelegate(getMoveAnimators(viewModel)).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        onMoveStart(viewModel);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onMoveEnd(viewModel);
                    }
                }, moveAnimationDuration, new FastOutSlowInInterpolator());
    }

    @NonNull
    private ValueAnimator[] getMoveAnimators(@NonNull final SpotlightViewModel viewModel) {
        if (animatingRectangle == null) {
            return new ValueAnimator[1];
        }

        final SpotlightViewModel tempViewModel = animatingRectangle;
        animatingRectangle = viewModel;

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", tempViewModel.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", tempViewModel.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", tempViewModel.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", tempViewModel.right, viewModel.right);

        return new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim};
    }

    private void onMoveStart(@NonNull final SpotlightViewModel viewModel) {
        if (animatingRectangle != null) {
            animatingRectangle.onBeforePageChanged();
        }
        viewModel.resetTextPaint();

    }

    private void onMoveEnd(@NonNull final SpotlightViewModel viewModel) {
        animatePulse(viewModel);
    }

    /*
     * ----------------------------------
     * Close animation
     * ----------------------------------
     */
    public void animateClose(@NonNull final SpotlightViewModel viewModel) {
        isMoving = true;

        new AnimationDelegate(getCloseAnimators()).animate(this,
                new Commons.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        onCloseStart(viewModel);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onCloseEnd();
                    }
                }, closeAnimationDuration, new DecelerateInterpolator());

    }

    @NonNull
    private ValueAnimator[] getCloseAnimators() {
        if (animatingRectangle == null) {
            return new ValueAnimator[1];
        }

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, 0);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, 0);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, getHeight());
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, getWidth());
        final ValueAnimator radiusAnim = ValueAnimator.ofInt(spotlight.getRadius(), 0);

        return new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim, radiusAnim};
    }

    private void onCloseStart(@NonNull final SpotlightViewModel viewModel) {
        viewModel.resetTextPaint();
//        spotlight.setBorderPaint(null);
//        spotlight.setBorderGradientPaint(null);
    }

    private void onCloseEnd() {
        isMoving = false;
        setVisibility(View.GONE);
        reset();
    }

    private void reset() {
        animatingRectangle = null;
        backgroundPaint = null;
        spotlight.setRadius(getContext());
    }


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

    /*
     * ----------------------------------
     * Public API
     * ----------------------------------
     */
    public static SpotlightView  addSpotlightView(@NonNull Activity activity, @NonNull List<SpotlightViewModel> models) {
        return addSpotlightView(activity, null, models);
    }

    public static SpotlightView addSpotlightView(@NonNull Activity activity, @Nullable SpotlightView spotlightView, @NonNull List<SpotlightViewModel> models) {
        if (spotlightView == null) {
            spotlightView = new SpotlightView(activity);
        }

        final ViewGroup rootLayout = activity.findViewById(android.R.id.content);
        rootLayout.addView(spotlightView);
        spotlightView.setModels(models);

        return spotlightView;
    }

    public boolean isClosed() {
        return animatingRectangle == null;
    }

    public void close() {
        if (animatingRectangle != null) {
            animateClose(animatingRectangle);
        }
    }

    public static class Builder {
        @NonNull
        private final SpotlightView spotlightView;

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

        public Builder setGrowRatio(float growRatio) {
            spotlightView.growRatio = growRatio;
            return this;
        }

        @NonNull
        public SpotlightView build() {
            return spotlightView;
        }
    }
}