package com.mitsest.spotlightviewpager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

public class SpotlightViewModel extends RectF {

    @Nullable
    private SpotlightViewModel previous;
    @Nullable
    private SpotlightViewModel next;
    @Nullable
    private String title;
    @Nullable
    private WeakReference<View> targetView;
    private @NonNull
    Subtitle subtitle;
    private @NonNull
    TextPaintDelegate textPaintDelegate;

    public SpotlightViewModel() {
    }

    public SpotlightViewModel(
            RectF r,
            @Nullable SpotlightViewModel previous,
            @Nullable SpotlightViewModel next,
            @Nullable String title,
            @NonNull Subtitle subtitle,
            @Nullable View targetView) {
        super(r);
        this.previous = previous;
        this.next = next;
        this.title = title;
        this.subtitle = subtitle;

        if (targetView != null) {
            this.textPaintDelegate = new TextPaintDelegate(targetView.getContext());
        }

        this.targetView = new WeakReference<>(targetView);
    }
    public SpotlightViewModel(@Nullable String title,
                              @NonNull Subtitle subtitle,
                              @Nullable View targetView) {
        this(new RectF(), null, null, title, subtitle, targetView);
    }

    @NonNull
    public Subtitle getSubtitleDelegate() {
        return subtitle;
    }

    public String getSubtitle() {
        return subtitle.getSubtitle();
    }

    public void setSubtitle(@NonNull Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    @Nullable
    public SpotlightViewModel getPrevious() {
        return previous;
    }

    public void setPrevious(@Nullable SpotlightViewModel previous) {
        this.previous = previous;
    }

    @Nullable
    public SpotlightViewModel getNext() {
        return next;
    }

    public void setNext(@Nullable SpotlightViewModel next) {
        this.next = next;
    }

    public void setLeft(float value) {
        left = value;
    }

    public void setRight(float value) {
        right = value;
    }

    public void setTop(float value) {
        top = value;
    }

    public void setBottom(float value) {
        bottom = value;
    }

    @Nullable
    public String getTitle() {
        return title;
    }


    public void setRectF(@NonNull RectF r) {
        this.left = r.left;
        this.right = r.right;
        this.top = r.top;
        this.bottom = r.bottom;

        this.setText();
        this.setTextPosition();
    }

    @Nullable
    public View getTargetView() {
        if (targetView == null || targetView.isEnqueued()) {
            return null;
        }

        return targetView.get();
    }

    public void setTargetView(@Nullable View targetView) {
        this.targetView = new WeakReference<>(targetView);
    }

    public int getMaxLines() {
        return subtitle.getMaxLines();
    }

    public void setMaxLines(int lineCount) {
        subtitle.setMaxLines(lineCount);
    }

    public int getTextPosition() {
        return subtitle.getTextPosition();
    }

    public void setTextPosition() {
        boolean fitsBottom = textPaintDelegate.tryDrawingTextToBottomOfSpotlight(this);

        if (!fitsBottom) {
            setTextPosition(Subtitle.SUBTITLE_TOP);
        } else {
            setTextPosition(Subtitle.SUBTITLE_BOTTOM);
        }
    }

    public void setTextPosition(int i) {
        subtitle.setTextPosition(i);
    }

    public void drawText(Canvas canvas) {
        textPaintDelegate.drawText(canvas, this);
    }

    public void setText() {
        textPaintDelegate.setText(this);
    }

    public void setMaxWidth(int width) {
        textPaintDelegate.setMaxWidth(width);
    }
    public void setMaxBottom(int bottom) {
        textPaintDelegate.setMaxBottom(bottom);
    }
    public void setPage(int page) {
        textPaintDelegate.setPage(page);
    }

    public void setNumberOfPages(int numberOfPages) {
        textPaintDelegate.setNumberOfPages(numberOfPages);
    }

    public ValueAnimator getTextOpacityAnimation() {
        return textPaintDelegate.getTextOpacityAnimation();
    }
}
