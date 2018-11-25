package com.mitsest.spotlightviewpager;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Subtitle {
    public static final int SUBTITLE_TOP = 0;
    public static final int SUBTITLE_BOTTOM = 1;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUBTITLE_TOP, SUBTITLE_BOTTOM})
    public @interface Gravity {}

    @Nullable private final String subtitle;
    private int maxLines;


    @Gravity private int textPosition;

    public Subtitle(@Nullable String subtitle, int maxLines) {
        this.subtitle = subtitle;
        this.maxLines = maxLines;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public int getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(@Gravity int textPosition) {
        this.textPosition = textPosition;
    }
}
