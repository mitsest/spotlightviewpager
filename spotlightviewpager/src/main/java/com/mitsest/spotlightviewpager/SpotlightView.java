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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;


public class SpotlightView extends ViewGroup implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    interface ISpotlightView {
        void onPageChanged(boolean isLastPage);
        void onCloseAnimationFinish();
    }


    private @Nullable
    ISpotlightView listener;


    private @NonNull @SuppressWarnings("NullableProblems")
    Spotlight spotlight;

    private @NonNull @SuppressWarnings("NullableProblems")
    Text text;

    private @NonNull @SuppressWarnings("NullableProblems")
    PagingDots pagingDots;

    private @NonNull @SuppressWarnings("NullableProblems")
    OffsetDelegate offsetDelegate;


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
    private @NonNull @SuppressWarnings("NullableProblems")
    Paint borderPaint;

    private @NonNull @SuppressWarnings("NullableProblems")
    Paint borderGradientPaint;

    private boolean isMoving;
    private int numberOfPages;
    private int page = 1;

    private boolean shouldDrawTextToTheBottomOfSpotlight;

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

        spotlight = new Spotlight(context);

        borderGradientPaint = new Paint(spotlight.getBorderGradientPaint());
        borderPaint = new Paint(spotlight.getBorderPaint());

        text = new Text(context);
        pagingDots = new PagingDots(context);

        backgroundPaint = new Paint();

        offsetDelegate = new OffsetDelegate();

        setVisibility(View.GONE);

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

    public void showPrevious() {
        if (isMoving || animatingRectangle == null) {
            return;
        }

        if (animatingRectangle.getPrevious() == null) {
            animateClose();
            return;
        }

        page--;
        animateMove(animatingRectangle.getPrevious());
    }

    public void setFirstTarget(@NonNull SpotlightViewModel firstTarget) {
        this.firstTarget = firstTarget;

        getViewTreeObserver().addOnGlobalLayoutListener(this);

        setVisibility(View.VISIBLE);
    }

    // This is called when initializing the view. Reset leftover state
    public void initView() {
        if (firstTarget == null) {
            return;
        }

        reset(firstTarget);

        if (listener != null) {
            listener.onPageChanged(false);
        }

        animateGrow(firstTarget);
    }

    @Override
    public void onGlobalLayout() {
        Commons.removeOnGlobalLayoutListenerTG(this, this);

        SpotlightViewModel viewModel = getFirstTarget();

        while (viewModel != null) {
            RectF rectF = offsetDelegate.getRectFFromView(viewModel.getTargetView(), getSpotLightPadding());
            if (rectF != null) {
                viewModel.setRectF(rectF);
            }

            viewModel = viewModel.getNext();
        }

        initView();
    }



    private void animateGrow(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = new SpotlightViewModel(viewModel);

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
        growAnimationSet.setInterpolator(new FastOutSlowInInterpolator());
        growAnimationSet.setDuration(GROW_ANIMATION_DURATION);
        growAnimationSet.start();

    }

    private boolean tryDrawingTextToTheBottomOfSpotlight(@NonNull final SpotlightViewModel viewModel, int width) {

        shouldDrawTextToTheBottomOfSpotlight = textFitsToBottomOfSpotlight(viewModel);
        while (!shouldDrawTextToTheBottomOfSpotlight && text.getSubtitlePaintLayoutLineCount() != 0) {
            text.cutSubtitleLine(viewModel, width);
            shouldDrawTextToTheBottomOfSpotlight = textFitsToBottomOfSpotlight(viewModel);
        }

        return shouldDrawTextToTheBottomOfSpotlight;
    }

    private boolean tryDrawingTextToTheTopOfSpotlight(@NonNull final SpotlightViewModel viewModel, int width) {
        boolean shouldDrawTextToTheTopOfSpotlight = textFitsToTopOfSpotlight(viewModel);
        if (!shouldDrawTextToTheTopOfSpotlight) {
            while (!shouldDrawTextToTheTopOfSpotlight && text.getSubtitlePaintLayoutLineCount() != 0) {
                text.cutSubtitleLine(viewModel, width);
                shouldDrawTextToTheTopOfSpotlight = textFitsToTopOfSpotlight(viewModel);
            }
        }

        return shouldDrawTextToTheTopOfSpotlight;
    }

    private void animatePulse(@NonNull final SpotlightViewModel viewModel) {
        animatingRectangle = new SpotlightViewModel(viewModel);

        final int width = getWidth();
        text.setText(viewModel, width, 15, numberOfPages, page);

        shouldDrawTextToTheBottomOfSpotlight = textFitsToBottomOfSpotlight(animatingRectangle);

        if (!shouldDrawTextToTheBottomOfSpotlight) {
            boolean shouldDrawTextToTheTopOfSpotlight = tryDrawingTextToTheTopOfSpotlight(animatingRectangle, width);
            if (!shouldDrawTextToTheTopOfSpotlight) {
                text.setText(viewModel, width, 15, numberOfPages, page);
                tryDrawingTextToTheBottomOfSpotlight(animatingRectangle, width);
            }
        }

        postInvalidate();

        final ObjectAnimator topAnim = ObjectAnimator.ofFloat(animatingRectangle, "top",
                animatingRectangle.top, animatingRectangle.top - spotlightPulseAnimationSize, animatingRectangle.top);

        final ObjectAnimator leftAnim = ObjectAnimator.ofFloat(animatingRectangle, "left", animatingRectangle.left, animatingRectangle.left - spotlightPulseAnimationSize, animatingRectangle.left);
        final ObjectAnimator bottomAnim = ObjectAnimator.ofFloat(animatingRectangle, "bottom", animatingRectangle.bottom, animatingRectangle.bottom + spotlightPulseAnimationSize, animatingRectangle.bottom);
        final ObjectAnimator rightAnim = ObjectAnimator.ofFloat(animatingRectangle, "right", animatingRectangle.right, animatingRectangle.right + spotlightPulseAnimationSize, animatingRectangle.right);

        addPostInvalidateOnUpdate(rightAnim);

        rightAnim.addListener(new Commons.AnimationListenerTG() {
            @Override
            public void onAnimationStart(Animator animation) {
                text.setText(viewModel, width, numberOfPages, page);
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

        if (firstTarget != null) {
            reset(firstTarget);
        }
    }

    private float getTextOffsetTop(@NonNull RectF animatingRectangle) {
        float offset = animatingRectangle.top - text.paddingTop;

        if (text.titlePaintLayout != null) {
            offset = offset - text.titlePaintLayout.getHeight() - text.paddingTop;
        }

        if (text.subtitlePaintLayout != null) {
            offset = offset - text.subtitlePaintLayout.getHeight() - text.paddingTop;
        }

        if (text.pageNumberPaintLayout != null) {
            offset = offset - text.pageNumberPaintLayout.getHeight() - text.paddingTop;
        }

        return offset;
    }

    private float getTextOffsetBottom(@NonNull RectF animatingRectangle) {
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

        return offset;
    }

    private boolean textFitsToBottomOfSpotlight(@Nullable Float offset) {
        if (offset == null) {
            if (animatingRectangle == null) {
                return true;
            }

            offset = getTextOffsetBottom(animatingRectangle);
        }

        return offset <= getBottom() - pagingDots.getMarginBottom() - pagingDots.getSize();
    }

    private boolean textFitsToBottomOfSpotlight(@NonNull RectF animatingRectangle) {
        return textFitsToBottomOfSpotlight(getTextOffsetBottom(animatingRectangle));
    }

    private boolean textFitsToTopOfSpotlight(@Nullable Float offset) {
        if (offset == null) {
            if (animatingRectangle == null) {
                return true;
            }

            offset = getTextOffsetTop(animatingRectangle);
        }

        return offset > 0;
    }

    private boolean textFitsToTopOfSpotlight(@NonNull RectF animatingRectangle) {
        return textFitsToTopOfSpotlight(getTextOffsetTop(animatingRectangle));
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

    public void setListener(@Nullable ISpotlightView listener) {
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

    @Nullable
    public SpotlightViewModel getFirstTarget() {
        return firstTarget;
    }

}