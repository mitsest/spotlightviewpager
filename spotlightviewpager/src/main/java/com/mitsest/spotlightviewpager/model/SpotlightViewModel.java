package com.mitsest.spotlightviewpager.model;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.mitsest.spotlightviewpager.paint.SpotlightTextPaint;

import java.lang.ref.WeakReference;

public class SpotlightViewModel extends RectF {

    @Nullable private SpotlightViewModel previous;
    @Nullable private SpotlightViewModel next;
    @NonNull private final String title;
    @Nullable private final WeakReference<View> targetView;
    @NonNull private final SubtitleModel subtitle;
    @NonNull private final SpotlightTextPaint textPaint;


    public SpotlightViewModel(@NonNull RectF r,
            @Nullable SpotlightViewModel previous,
            @Nullable SpotlightViewModel next,
            @NonNull String title,
            @NonNull SubtitleModel subtitle,
            @NonNull View targetView) {
        super(r);
        this.previous = previous;
        this.next = next;
        this.title = title;
        this.subtitle = subtitle;
        this.textPaint = new SpotlightTextPaint(targetView.getContext());
        this.targetView = new WeakReference<>(targetView);
    }

    public SpotlightViewModel(@NonNull String title,
                              @NonNull SubtitleModel subtitle,
                              @NonNull View targetView) {
        this(new RectF(), null, null, title, subtitle, targetView);
    }

    public String getSubtitle() {
        return subtitle.getSubtitle();
    }

    @Nullable public SpotlightViewModel getPrevious() {
        return previous;
    }

    private void setPrevious(@Nullable SpotlightViewModel previous) {
        this.previous = previous;
    }

    @Nullable
    public SpotlightViewModel getNext() {
        return next;
    }

    public void setNext(@Nullable SpotlightViewModel next) {
        if (next != null) {
            next.previous = this;
        }

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

    @NonNull
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

    public int getMaxLines() {
        return subtitle.getMaxLines();
    }

    public void setMaxLines(int lineCount) {
        subtitle.setMaxLines(lineCount);
    }

    public int getTextPosition() {
        return subtitle.getTextPosition();
    }

    private void setTextPosition() {
        boolean fitsBottom = textPaint.tryDrawingTextToBottomOfSpotlight(this);

        if (!fitsBottom) {
            subtitle.setTextPosition(SubtitleModel.SUBTITLE_TOP);
        } else {
            subtitle.setTextPosition(SubtitleModel.SUBTITLE_BOTTOM);
        }
    }

    public void drawText(Canvas canvas) {
        textPaint.drawText(canvas, this);
    }

    private void setText() {
        textPaint.setText(this);
    }

    public void setMaxWidth(int width) {
        textPaint.setMaxWidth(width);
    }

    public void setMaxBottom(int bottom) {
        textPaint.setMaxBottom(bottom);
    }

    public void setPage(int page) {
        textPaint.setPage(page);
    }

    public void setNumberOfPages(int numberOfPages) {
        textPaint.setNumberOfPages(numberOfPages);
    }

    public ValueAnimator getTextOpacityAnimation() {
        return textPaint.getTextOpacityAnimation();
    }

    public void resetTextPaint() {
        textPaint.reset();
    }
}
