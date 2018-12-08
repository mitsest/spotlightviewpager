package com.mitsest.spotlightviewpager.model;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubtitleModel {

    @Nullable private final String subtitle;
    private int maxLines;



    public SubtitleModel(@Nullable String subtitle, int maxLines) {
        this.subtitle = subtitle;
        this.maxLines = maxLines;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    int getMaxLines() {
        return maxLines;
    }

    void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }
}
