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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;


public class SpotlightView extends View implements View.OnClickListener, View.OnTouchListener {

    @Nullable
    public SpotlightViewModel getFirstTarget() {
        return firstTarget;
    }

    interface SpotlightViewInterface {
        void onPageChanged(boolean isLastPage);

        void onCloseAnimationFinish();
    }

    private @Nullable
    SpotlightViewInterface listener;

    private @NonNull
    Spotlight spotlight;
    private @NonNull
    Text text;
    private @NonNull
    PagingDots pagingDots;

    private static final int PULSE_ANIMATION_SIZE_DP = 11;
    private int spotlightPulseAnimationSize;

    private static final int ENTER_ANIMATION_DURATION = 320; // ms
    private static final int GROW_ANIMATION_DURATION = 310; // ms
    private static final int PULSE_ANIMATION_DURATION = 1900; // ms
    private static final int MOVE_ANIMATION_DURATION = 600; // ms
    private static final int CLOSE_ANIMATION_DURATION = 220; // ms

    private @NonNull
    Paint backgroundPaint; // used to draw background overlay

    private @Nullable
    SpotlightViewModel firstTarget; // Keeping a reference on first target
    private @Nullable
    SpotlightViewModel animatingRectangle; // Used in draw (its scale and bounds are changing)

    // Keeping a reference for the following two, because we nullify them when spotlight grows.
    private @NonNull Paint borderPaint;
    private @NonNull Paint borderGradientPaint;

    private boolean isMoving;
    private int numberOfPages;
    private int page = 1;

    private boolean shouldDrawTextToTheBottomOfSpotlight;

    public SpotlightView(@NonNull Context context) {
        super(context);

        spotlight = new Spotlight(context);

        borderGradientPaint = new Paint(spotlight.getBorderGradientPaint());
        borderPaint = new Paint(spotlight.getBorderPaint());

        text = new Text(context);
        pagingDots = new PagingDots(context);

        backgroundPaint = new Paint();

        init(context);
    }

    private void init(@NonNull Context context) {
        page = 1;

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        spotlightPulseAnimationSize = Commons.dpToPx(context, PULSE_ANIMATION_SIZE_DP);

        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        setOnClickListener(this);

        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.spotlight_overlay_color));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        spotlight.drawSpotlightBorder(canvas, animatingRectangle);
        spotlight.drawSpotlight(canvas, animatingRectangle);
        text.drawText(canvas, animatingRectangle, shouldDrawTextToTheBottomOfSpotlight);
        pagingDots.drawPageIndicators(canvas, numberOfPages, page);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
    }

    private boolean shouldDrawTextToTheBottomOfSpotlight(@NonNull RectF animatingRectangle) {
        float offset = 0;
        offset += animatingRectangle.bottom + text.paddingTop;

        if (text.titlePaintLayout != null) {
            offset += text.titlePaintLayout.getHeight() + text.paddingTop;
        }

        if (text.subtitlePaintLayout != null) {
            offset += text.subtitlePaintLayout.getHeight() + text.paddingTop;
        }

        if (text.pageNumberPaintLayout != null) {
            offset += text.pageNumberPaintLayout.getHeight();
        }

        return offset <= getHeight() - pagingDots.getMarginBottom() - pagingDots.getSize();
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

        page++;
        animateMove(animatingRectangle.getNext());

    }

    public boolean showPrevious() {
        if (animatingRectangle == null) {
            return false;
        }

        if (animatingRectangle.getPrevious() == null) {
            animateClose();
            return false;
        }

        page--;
        animateMove(animatingRectangle.getPrevious());

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (animatingRectangle == null || animatingRectangle.getNext() == null) {
//            setVisibility(View.GONE);
//            return true;
        }

        return false;
    }

    // This is called when initializing the view. Reset leftover state
    public void initView() {
        if (firstTarget == null) {
            return;
        }

        reset(firstTarget);

        if (listener != null) {
            listener.onPageChanged(isLastPage());
        }

        animateGrow(firstTarget);
    }

    public void setFirstTarget(@NonNull SpotlightViewModel firstTarget) {
        this.firstTarget = firstTarget;
    }

    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = new SpotlightViewModel(viewModel);

        clearPaintToGrow();

        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom - animatingRectangle.height() / 2, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right - animatingRectangle.width() / 2, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                spotlight.setSpotlightBorderGradientPaint(borderGradientPaint);
                spotlight.setSpotlightBorderPaint(borderPaint);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatePulse(viewModel);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animatePulse(viewModel);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        final AnimatorSet growAnimationSet = new AnimatorSet();
        growAnimationSet.playTogether(leftAnim, bottomAnim, rightAnim, topAnim);
        growAnimationSet.setInterpolator(new FastOutSlowInInterpolator());
        growAnimationSet.setDuration(GROW_ANIMATION_DURATION);
        growAnimationSet.start();

    }

    private void animatePulse(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = new SpotlightViewModel(viewModel);
        shouldDrawTextToTheBottomOfSpotlight = shouldDrawTextToTheBottomOfSpotlight(animatingRectangle);

        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, animatingRectangle.top - spotlightPulseAnimationSize, animatingRectangle.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, animatingRectangle.left - spotlightPulseAnimationSize, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, animatingRectangle.bottom + spotlightPulseAnimationSize, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, animatingRectangle.right + spotlightPulseAnimationSize, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        final int width = getWidth();

        rightAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                text.setText(viewModel, width, numberOfPages, page);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

        clearPaintToMove();
        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top", animatingRectangle.top, viewModel.top);
        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, viewModel.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, viewModel.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, viewModel.right);


        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isMoving = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onMoveEnd(viewModel);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onMoveEnd(viewModel);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
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
            listener.onPageChanged(isLastPage());
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

        radiusAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onCloseEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

        if (firstTarget != null) {
            reset(firstTarget);
        }
    }


    private void addPostInvalidateOnUpdate(@NonNull ValueAnimator anim) {
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                postInvalidate();
            }
        });
    }

    private void reset(@NonNull SpotlightViewModel firstTarget) {
        isMoving = false;
        setNumberOfPages(firstTarget);

        spotlight.setRadius(getContext());
        spotlight.setBorderPaint(spotlight.getBorderPaint());
        spotlight.setSpotlightBorderGradientPaint(borderGradientPaint);
        spotlight.setSpotlightBorderPaint(borderPaint);

        postInvalidate();
    }


    private void clearPaintToMove() {
        text.titlePaintLayout = null;
        text.subtitlePaintLayout = null;
        text.pageNumberPaintLayout = null;
    }

    private void clearPaintToClose() {
        text.titlePaintLayout = null;
        text.subtitlePaintLayout = null;
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
        text.pageNumberPaintLayout = null;
    }

    private void clearPaintToGrow() {
        spotlight.setBorderPaint(null);
        spotlight.setBorderGradientPaint(null);
    }

    public int getSpotLightPadding() {
        return spotlight.getPadding();
    }

    public void setListener(@Nullable SpotlightViewInterface listener) {
        this.listener = listener;
    }

    public boolean isLastPage() {
        return page == numberOfPages;
    }

    void setNumberOfPages(@NonNull SpotlightViewModel firstTarget) {
        page = 1;
        SpotlightViewModel viewModel = firstTarget;

        int numberOfPages = 1;
        while (viewModel != null && viewModel.getNext() != null) {

            numberOfPages++;
            viewModel = viewModel.getNext();

        }

        this.numberOfPages = numberOfPages;

    }


}