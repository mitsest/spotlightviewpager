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
    private static final int TEXT_ENTER_ANIMATION_DURATION = 1200; // ms
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

    private boolean isMoving;

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

    public static void addSpotlightView(@NonNull Activity activity, @NonNull List<SpotlightViewModel> models) {
        final ViewGroup rootLayout = activity.findViewById(android.R.id.content);
        SpotlightView mView = new SpotlightView(activity);
        rootLayout.addView(mView);
        mView.setModels(models);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        offsetDelegate.onLayout(this, changed, l, t, r, b);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        spotlight.drawSpotlightBorder(canvas, animatingRectangle);
        spotlight.drawSpotlight(canvas, animatingRectangle);

        if (!isMoving && animatingRectangle != null) {
            animatingRectangle.drawText(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        backgroundPaint.setAlpha(backgroundOpacityDelegate.getOpacity());
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
    }

    public void showNext() {
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
            return;
        }

        animateMove(animatingRectangle.getPrevious());
    }

    @Override
    public void onGlobalLayout() {
        if (firstTarget == null) {
            return;
        }

        Commons.removeOnGlobalLayoutListener(this, this);

        SpotlightViewModel viewModel = getFirstTarget();

        int numberOfPages = 0;
        while (viewModel != null) {
            RectF rectF = offsetDelegate.getRectFFromView(viewModel.getTargetView(), spotlight.getPadding());

            numberOfPages++;
            viewModel.setMaxWidth(getWidth());
            viewModel.setMaxBottom(getBottom());
            viewModel.setPage(numberOfPages);

            if (rectF != null) {
                viewModel.setRectF(rectF);
            }

            viewModel = viewModel.getNext();
        }

        animateBackground(firstTarget);
    }

    private void animateBackground(final @NonNull SpotlightViewModel viewModel) {
        final ValueAnimator opacityAnim = backgroundOpacityDelegate.getOpacityAnimator();
        addPostInvalidateOnUpdate(opacityAnim);

        opacityAnim.addListener(new Commons.AnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                animateGrow(viewModel);
            }
        });

        opacityAnim.setInterpolator(new LinearInterpolator());
        opacityAnim.setDuration(ENTER_ANIMATION_DURATION);
        opacityAnim.start();
    }

    private void animateText(final @NonNull SpotlightViewModel viewModel) {
        final ValueAnimator opacityAnim = viewModel.getTextOpacityAnimation();
        addPostInvalidateOnUpdate(opacityAnim);

        opacityAnim.setInterpolator(new LinearInterpolator());
        opacityAnim.setDuration(TEXT_ENTER_ANIMATION_DURATION);
        opacityAnim.start();
    }

    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = viewModel;

        clearBorderToPaint();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.top);

        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.left);

        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom",
                animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.bottom);

        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right",
                animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Commons.AnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                undoClearBorderPaint();
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
        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                viewModel.top, viewModel.top - spotlightPulseAnimationSize, viewModel.top);

        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", viewModel.left, viewModel.left - spotlightPulseAnimationSize, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", viewModel.bottom, viewModel.bottom + spotlightPulseAnimationSize, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", viewModel.right, viewModel.right + spotlightPulseAnimationSize, viewModel.right);

        addPostInvalidateOnUpdate(rightAnim);
        rightAnim.addListener(new Commons.AnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animateText(viewModel);
            }
        });

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

        isMoving = true;

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, viewModel.right);
        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Commons.AnimationListener() {

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
        animatingRectangle = viewModel;
        animatePulse(viewModel);
        isMoving = false;
    }

    public void animateClose() {
        if (animatingRectangle == null) {
            return;
        }

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

        radiusAnim.addListener(new Commons.AnimationListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                spotlight.setBorderPaint(null);
                spotlight.setBorderGradientPaint(null);
                isMoving = true;
            }

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
        undoClearBorderPaint();

        postInvalidate();
    }

    private void clearBorderToPaint() {
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
        postInvalidate();
    }

    private void undoClearBorderPaint() {
        spotlight.setBorderGradientPaint(borderGradientPaint);
        spotlight.setBorderPaint(borderPaint);
    }

    public int getSpotLightPadding() {
        return spotlight.getPadding();
    }


    @Nullable
    public SpotlightViewModel getFirstTarget() {
        return firstTarget;
    }

    public void setModels(@Nullable List<SpotlightViewModel> targets) {
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

    public void show() {

    }
}