package com.mitsest.spotlightviewpager;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

public class SpotlightViewModel extends RectF {

    @Nullable private SpotlightViewModel previous;
    @Nullable private SpotlightViewModel next;

    @Nullable private String title;
    @Nullable private String subtitle;
    @Nullable private WeakReference<View> targetView;

    public SpotlightViewModel() {
    }

    public SpotlightViewModel(
            RectF r,
            @Nullable SpotlightViewModel previous,
            @Nullable SpotlightViewModel next,
            @Nullable String title,
            @Nullable String subtitle,
            @Nullable View targetView) {
        super(r);
        this.previous = previous;
        this.next = next;
        this.title = title;
        this.subtitle = subtitle;
        this.targetView = new WeakReference<>(targetView);
    }

    public SpotlightViewModel(@Nullable String title,
                              @Nullable String subtitle,
                              @Nullable View targetView) {
        this(new RectF(), null, null, title, subtitle, targetView);
    }

    public SpotlightViewModel(SpotlightViewModel viewModel) {
        this(viewModel, viewModel.getPrevious(), viewModel.getNext(), viewModel.getTitle(), viewModel.getSubtitle(), viewModel.getTargetView());
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

    @Nullable
    public String getSubtitle() {
        return subtitle;
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
}
