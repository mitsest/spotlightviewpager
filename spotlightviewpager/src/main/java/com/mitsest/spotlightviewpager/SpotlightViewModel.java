package com.mitsest.spotlightviewpager;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

public class SpotlightViewModel extends RectF {

    public static final int SUBTITLE_TOP = 0;
    public static final int SUBTITLE_BOTTOM = 1;
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
    Text text;

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
            this.text = new Text(targetView.getContext());
        }

        this.targetView = new WeakReference<>(targetView);
    }
    public SpotlightViewModel(@Nullable String title,
                              @NonNull Subtitle subtitle,
                              @Nullable View targetView) {
        this(new RectF(), null, null, title, subtitle, targetView);
    }

    public SpotlightViewModel(SpotlightViewModel viewModel) {
        this(viewModel, viewModel.getPrevious(), viewModel.getNext(), viewModel.getTitle(), viewModel.getSubtitleDelegate(), viewModel.getTargetView());
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

    private void setPrevious(@Nullable SpotlightViewModel previous) {
        this.previous = previous;
    }

    @Nullable
    public SpotlightViewModel getNext() {
        return next;
    }

    public void setNext(@Nullable SpotlightViewModel next) {
        if (next != null) {
            next.setPrevious(this);
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

    @Nullable
    public String getTitle() {
        return title;
    }


    public void setRectF(@NonNull RectF r) {
        this.left = r.left;
        this.right = r.right;
        this.top = r.top;
        this.bottom = r.bottom;
    }

    @Nullable
    public View getTargetView() {
        if (targetView == null) {
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
        boolean fitsBottom = text.tryDrawingTextToBottomOfSpotlight(this);

        if (!fitsBottom) {
            setTextPosition(SpotlightViewModel.SUBTITLE_TOP);
        } else {
            setTextPosition(SpotlightViewModel.SUBTITLE_BOTTOM);
        }
    }

    public void setTextPosition(int i) {
        subtitle.setTextPosition(i);
    }

    public void drawText(Canvas canvas) {
        text.drawText(canvas, this);
    }

    public void setText() {
        text.setText(this);
    }

    public void setText(int width, int bottom, int page) {
        text.setMaxWidth(width);
        text.setMaxBottom(bottom);
        text.setPage(page);
    }

    public void setNumberOfPages(int numberOfPages) {
        text.setNumberOfPages(numberOfPages);
    }
}
