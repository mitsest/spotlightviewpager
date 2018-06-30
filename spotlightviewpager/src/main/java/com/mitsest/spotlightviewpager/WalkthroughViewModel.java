package com.mitsest.spotlightviewpager;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

public class WalkthroughViewModel extends RectF {

    @Nullable private WalkthroughViewModel previous;
    @Nullable private WalkthroughViewModel next;

    @Nullable private String title;
    @Nullable private String subtitle;
    @Nullable private WeakReference<View> spotlightView;

    public WalkthroughViewModel() {
    }

    public WalkthroughViewModel(
            RectF r,
            @Nullable WalkthroughViewModel previous,
            @Nullable WalkthroughViewModel next,
            @Nullable String title,
            @Nullable String subtitle,
            @Nullable View spotlightView) {
        super(r);
        this.previous = previous;
        this.next = next;
        this.title = title;
        this.subtitle = subtitle;
        this.spotlightView = new WeakReference<>(spotlightView);
    }

    public WalkthroughViewModel(@Nullable String title,
                                @Nullable String subtitle,
                                @Nullable View spotlightView) {
        this(null, null, null, title, subtitle, spotlightView);
    }

    public WalkthroughViewModel(WalkthroughViewModel viewModel) {
        this(viewModel, viewModel.getPrevious(), viewModel.getNext(), viewModel.getTitle(), viewModel.getSubtitle(), viewModel.getSpotlightView());
    }

    @Nullable
    public WalkthroughViewModel getPrevious() {
        return previous;
    }

    private void setPrevious(@Nullable WalkthroughViewModel previous) {
        this.previous = previous;
    }

    @Nullable
    public WalkthroughViewModel getNext() {
        return next;
    }

    public void setNext(@Nullable WalkthroughViewModel next) {
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
    public View getSpotlightView() {
        if (spotlightView == null) {
            return null;
        }

        return spotlightView.get();
    }

    public void setSpotlightView(@Nullable View spotlightView) {
        this.spotlightView = new WeakReference<>(spotlightView);
    }
}
