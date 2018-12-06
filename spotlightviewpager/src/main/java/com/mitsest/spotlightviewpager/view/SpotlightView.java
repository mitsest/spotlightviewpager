package com.mitsest.spotlightviewpager.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.mitsest.spotlightviewpager.Commons;
import com.mitsest.spotlightviewpager.R;
import com.mitsest.spotlightviewpager.animation_delegate.OffsetDelegate;
import com.mitsest.spotlightviewpager.animation_delegate.OpacityDelegate;
import com.mitsest.spotlightviewpager.model.SpotlightViewModel;
import com.mitsest.spotlightviewpager.paint.SpotlightPaint;

import java.util.List;


public class SpotlightView extends ViewGroup implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int PULSE_ANIMATION_SIZE_DP = 11;
    private static final int ENTER_ANIMATION_DURATION = 800; // ms
    private static final int TEXT_ENTER_ANIMATION_DURATION = 600; // ms
    private static final int GROW_ANIMATION_DURATION = 300; // ms
    private static final int PULSE_ANIMATION_DURATION = 1200; // ms
    private static final int MOVE_ANIMATION_DURATION = 600; // ms
    private static final int CLOSE_ANIMATION_DURATION = 220; // ms

    @NonNull private final SpotlightPaint spotlight;
    @NonNull private final OffsetDelegate offsetDelegate;
    @NonNull private final OpacityDelegate backgroundOpacityDelegate;
    @NonNull private final Paint backgroundPaint;
    @NonNull private final Paint borderPaint;
    @NonNull private final Paint borderGradientPaint;
    @NonNull private final OnSwipeTouchListener swipeTouchListener;

    @Nullable private SpotlightViewModel firstTarget;
    @Nullable private SpotlightViewModel animatingRectangle; // Used in draw (its scale and bounds are changing)

    @Dimension private int spotlightPulseAnimationSize;

    private boolean shouldDrawText;

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
        spotlightPulseAnimationSize = Commons.dpToPx(context, PULSE_ANIMATION_SIZE_DP);
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

        init(context);
    }

    private void showNext() {
        if (shouldDrawText || animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getNext() == null) {
            animateClose();
            return;
        }

        animateMove(animatingRectangle.getNext());

    }

    private void showPrevious() {
        if (shouldDrawText || animatingRectangle == null) {
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
            RectF rectF = offsetDelegate.getRectFFromView(viewModel.getTargetView(), spotlight.getPadding());

            page++;
            viewModel.setMaxWidth(getWidth());
            viewModel.setMaxBottom(getBottom());
            viewModel.setPage(page);

            if (rectF != null) {
                viewModel.setRectF(rectF);
            }

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

        if (!shouldDrawText && animatingRectangle != null) {
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


    private void animate(@NonNull ValueAnimator[] animators, int durationMs, Interpolator interpolator) {
        animate(animators, null, durationMs, interpolator);
    }

    private void animate(@NonNull ValueAnimator[] animators, @Nullable Animator.AnimatorListener listener, int durationMs, @NonNull Interpolator interpolator) {
        final AnimatorSet animatorSet = new AnimatorSet();

        if (animators.length >= 1) {
            animators[animators.length - 1].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    postInvalidate();
                }
            });

            animatorSet.playTogether(animators);
        }

        animatorSet.setInterpolator(interpolator);
        animatorSet.setDuration(durationMs);

        if (listener != null) {
            animatorSet.addListener(listener);
        }

        animatorSet.start();

    }

    /*
     * ----------------------------------
     * Background animation
     * ----------------------------------
     */
    private void animateBackground(final @NonNull SpotlightViewModel viewModel) {
        final ValueAnimator opacityAnim = backgroundOpacityDelegate.getOpacityAnimator();

        animate(new ValueAnimator[]{opacityAnim}, new Commons.AnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onBackgroundEnd(viewModel);
            }
        }, ENTER_ANIMATION_DURATION, new LinearInterpolator());
    }

    private void onBackgroundEnd(final @NonNull SpotlightViewModel viewModel) {
        animateGrow(viewModel);
    }

    /*
     * ----------------------------------
     * Text animation
     * ----------------------------------
     */
    private void animateText(final @NonNull SpotlightViewModel viewModel) {
        final ValueAnimator opacityAnim = viewModel.getTextOpacityAnimation();
        animate(new ValueAnimator[]{opacityAnim}, TEXT_ENTER_ANIMATION_DURATION, new LinearInterpolator());
    }

    /*
     * ----------------------------------
     * Grow animation
     * ----------------------------------
     */
    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.right);

        animate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim}, new Commons.AnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onGrowStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onGrowEnd(viewModel);
            }
        }, GROW_ANIMATION_DURATION, new AccelerateDecelerateInterpolator());

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

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", viewModel.top, viewModel.top - spotlightPulseAnimationSize, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", viewModel.left, viewModel.left - spotlightPulseAnimationSize, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", viewModel.bottom, viewModel.bottom + spotlightPulseAnimationSize, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", viewModel.right, viewModel.right + spotlightPulseAnimationSize, viewModel.right);

        animate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim}, new Commons.AnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onPulseEnd(viewModel);

            }
        }, PULSE_ANIMATION_DURATION, new OvershootInterpolator());
    }

    private void onPulseEnd(@NonNull final SpotlightViewModel viewModel) {
        animateText(viewModel);
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

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, viewModel.right);

        animate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim}, new Commons.AnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onMoveStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onMoveEnd(viewModel);
            }

        }, MOVE_ANIMATION_DURATION, new FastOutSlowInInterpolator());
    }

    private void onMoveStart() {
        shouldDrawText = true;
    }

    private void onMoveEnd(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;
        animatePulse(viewModel);
        shouldDrawText = false;
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
        animate(new ValueAnimator[]{topAnim, leftAnim, bottomAnim, rightAnim, radiusAnim}, new Commons.AnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
               onCloseStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseEnd();
            }
        }, CLOSE_ANIMATION_DURATION, new DecelerateInterpolator());
    }

    private void onCloseStart() {
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
        shouldDrawText = true;

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

    public static void addSpotlightView(@NonNull Activity activity, @NonNull List<SpotlightViewModel> models) {
        final ViewGroup rootLayout = activity.findViewById(android.R.id.content);
        SpotlightView mView = new SpotlightView(activity);
        rootLayout.addView(mView);
        mView.setModels(models);
    }
}