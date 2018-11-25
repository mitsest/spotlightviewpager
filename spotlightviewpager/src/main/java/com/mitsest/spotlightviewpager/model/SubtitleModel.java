package com.mitsest.spotlightviewpager.model;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubtitleModel {
    public static final int SUBTITLE_TOP = 0;
    public static final int SUBTITLE_BOTTOM = 1;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUBTITLE_TOP, SUBTITLE_BOTTOM})
    @interface Gravity {}

    @Nullable private final String subtitle;
    private int maxLines;


    @Gravity private int textPosition;

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

    int getTextPosition() {
        return textPosition;
    }

    void setTextPosition(@Gravity int textPosition) {
        this.textPosition = textPosition;
    }
}
