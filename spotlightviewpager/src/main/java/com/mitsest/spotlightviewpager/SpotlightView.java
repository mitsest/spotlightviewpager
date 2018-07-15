package com.mitsest.spotlightviewpager;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
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
import android.view.animation.OvershootInterpolator;


public class SpotlightView extends ViewGroup implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private static final int PULSE_ANIMATION_SIZE_DP = 11;
    private static final int ENTER_ANIMATION_DURATION = 320; // ms
    private static final int GROW_ANIMATION_DURATION = 300; // ms
    private static final int PULSE_ANIMATION_DURATION = 1900; // ms
    private static final int MOVE_ANIMATION_DURATION = 600; // ms
    private static final int CLOSE_ANIMATION_DURATION = 220; // ms
    private @Nullable
    ISpotlightView listener;
    private @NonNull
    @SuppressWarnings("NullableProblems")
    SpotlightPaint spotlight;
    @SuppressWarnings("NullableProblems")
    private @NonNull
    OffsetDelegate offsetDelegate;
    private @NonNull
    @SuppressWarnings("NullableProblems")
    Paint backgroundPaint; // used to draw background overlay
    private @Nullable
    SpotlightViewModel firstTarget; // Keeping a reference on first target
    private @Nullable
    SpotlightViewModel animatingRectangle; // Used in draw (its scale and bounds are changing)
    // Keeping a reference for the following two, because we nullify them when spotlight grows.
    private @NonNull
    @SuppressWarnings("NullableProblems")
    Paint borderPaint;
    private @NonNull
    @SuppressWarnings("NullableProblems")
    Paint borderGradientPaint;
    private boolean isMoving;
    private int spotlightPulseAnimationSize;

    public SpotlightView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        offsetDelegate.onLayoutDelegate(this, changed, l, t, r, b);
    }

    private void init(@NonNull Context context) {
        setVisibility(View.GONE);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        spotlightPulseAnimationSize = Commons.dpToPx(context, PULSE_ANIMATION_SIZE_DP);

        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        setOnClickListener(this);

        spotlight = new SpotlightPaint(context);

        borderGradientPaint = new Paint(spotlight.getBorderGradientPaint());
        borderPaint = new Paint(spotlight.getBorderPaint());

        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.spotlight_overlay_color));

        offsetDelegate = new OffsetDelegate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        spotlight.drawSpotlightBorder(canvas, animatingRectangle);
        spotlight.drawSpotlight(canvas, animatingRectangle);
//        text.drawText(canvas, animatingRectangle);
        if (animatingRectangle != null) {
            animatingRectangle.drawText(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
    }

    @Override
    public void onClick(View v) {
        if (isMoving || animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getNext() == null) {
            animateClose();
            return;
        }

        animateMove(animatingRectangle.getNext());

    }

    public void showPrevious() {
        if (isMoving || animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getPrevious() == null) {
            animateClose();
            return;
        }

        animateMove(animatingRectangle.getPrevious());
    }


    @Override
    public void onGlobalLayout() {
        if (firstTarget == null) {
            return;
        }

        Commons.removeOnGlobalLayoutListenerTG(this, this);

        SpotlightViewModel viewModel = getFirstTarget();

        int numberOfPages = 0;
        while (viewModel != null) {

            numberOfPages++;
            viewModel.setText(getWidth(), getBottom(), numberOfPages);

            RectF rectF = offsetDelegate.getRectFFromView(viewModel.getTargetView(), spotlight.getPadding());
            if (rectF != null) {
                viewModel.setRectF(rectF);
                viewModel.setTextPosition();
            }
            viewModel.setText();

            viewModel = viewModel.getNext();
        }

        viewModel = getFirstTarget();
        while (viewModel != null) {
            viewModel.setNumberOfPages(numberOfPages);
            viewModel = viewModel.getNext();
        }

        if (listener != null) {
            listener.onPageChanged(false);
        }

        animateGrow(firstTarget);
    }

    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;

        clearPaintToGrow();

        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.top);

        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.left);

        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.bottom);

        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Commons.AnimationListenerTG() {
            @Override
            public void onAnimationStart(Animator animation) {
                spotlight.setSpotlightBorderGradientPaint(borderGradientPaint);
                spotlight.setSpotlightBorderPaint(borderPaint);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatePulse(viewModel);
            }

        });

        final AnimatorSet growAnimationSet = new AnimatorSet();
        growAnimationSet.playTogether(leftAnim, bottomAnim, rightAnim, topAnim);
        growAnimationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        growAnimationSet.setDuration(GROW_ANIMATION_DURATION);
        growAnimationSet.start();

    }

    private void animatePulse(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;
        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.top, animatingRectangle.top - spotlightPulseAnimationSize, animatingRectangle.top);

        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, animatingRectangle.left - spotlightPulseAnimationSize, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, animatingRectangle.bottom + spotlightPulseAnimationSize, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, animatingRectangle.right + spotlightPulseAnimationSize, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        final AnimatorSet pulseAnimationSet = new AnimatorSet();
        pulseAnimationSet.playTogether(leftAnim, bottomAnim, rightAnim, topAnim);
        pulseAnimationSet.setInterpolator(new OvershootInterpolator());
        pulseAnimationSet.setDuration(PULSE_ANIMATION_DURATION);
        pulseAnimationSet.start();
    }

    private void animateMove(@NonNull final SpotlightViewModel viewModel) {
        if (animatingRectangle == null) {
            return;
        }

        clearPaintToMove();
        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, viewModel.right);


        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Commons.AnimationListenerTG() {
            @Override
            public void onAnimationStart(Animator animation) {
                isMoving = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onMoveEnd(viewModel);
            }
        });

        final AnimatorSet moveAnimation = new AnimatorSet();
        moveAnimation.playTogether(topAnim, leftAnim, bottomAnim, rightAnim);

        moveAnimation.setInterpolator(new FastOutSlowInInterpolator());
        moveAnimation.setDuration(MOVE_ANIMATION_DURATION).start();
    }

    private void onMoveEnd(@NonNull final SpotlightViewModel viewModel) {
        animatePulse(viewModel);

        if (listener != null) {
//            listener.onPageChanged(isLastPage());
        }

        isMoving = false;
    }

    public void animateClose() {
        if (animatingRectangle == null) {
            return;
        }

        clearPaintToClose();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, 0);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, 0);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, getHeight());
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, getWidth());
        final ValueAnimator radiusAnim = ValueAnimator.ofInt(spotlight.getRadius(), 0);
        radiusAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    spotlight.setRadius(Integer.valueOf(animation.getAnimatedValue().toString()));
                } catch (Exception e) {
                    //
                }
            }
        });
        addPostInvalidateOnUpdate(radiusAnim);

        radiusAnim.addListener(new Commons.AnimationListenerTG() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseEnd();
            }
        });

        final AnimatorSet moveAnimation = new AnimatorSet();
        moveAnimation.playTogether(topAnim, leftAnim, bottomAnim, rightAnim, radiusAnim);

        moveAnimation.setInterpolator(new DecelerateInterpolator());
        moveAnimation.setDuration(CLOSE_ANIMATION_DURATION).start();
    }

    private void onCloseEnd() {
        if (listener != null) {
            listener.onCloseAnimationFinish();
        }

        setVisibility(View.GONE);
        reset();

    }

    private void addPostInvalidateOnUpdate(@NonNull ValueAnimator anim) {
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                postInvalidate();
            }
        });
    }

    private void reset() {
        isMoving = false;

        spotlight.setRadius(getContext());
        spotlight.setBorderPaint(spotlight.getBorderPaint());
        spotlight.setSpotlightBorderGradientPaint(borderGradientPaint);
        spotlight.setSpotlightBorderPaint(borderPaint);

        postInvalidate();
    }

    private void clearPaintToMove() {
//        text.titlePaintLayout = null;
//        text.subtitlePaintLayout = null;
//        text.pageNumberPaintLayout = null;
    }

    private void clearPaintToClose() {
//        text.titlePaintLayout = null;
//        text.subtitlePaintLayout = null;
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
//        text.pageNumberPaintLayout = null;
    }

    private void clearPaintToGrow() {
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
    }

    public int getSpotLightPadding() {
        return spotlight.getPadding();
    }

    public void setListener(@Nullable ISpotlightView listener) {
        this.listener = listener;
    }

    @Nullable
    public SpotlightViewModel getFirstTarget() {
        return firstTarget;
    }

    public void setFirstTarget(@NonNull SpotlightViewModel firstTarget) {
        this.firstTarget = firstTarget;

        getViewTreeObserver().addOnGlobalLayoutListener(this);

        setVisibility(View.VISIBLE);
    }

    interface ISpotlightView {
        void onPageChanged(boolean isLastPage);

        void onCloseAnimationFinish();
    }

}