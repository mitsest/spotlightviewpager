package com.mitsest.spotlightviewpager;

import android.support.annotation.Nullable;

public class Subtitle {
    public static final int SUBTITLE_TOP = 0;
    public static final int SUBTITLE_BOTTOM = 1;


    @Nullable
    private String subtitle;

    private int maxLines;


    private int textPosition;

    public Subtitle(@Nullable String subtitle, int maxLines) {
        this.subtitle = subtitle;
        this.maxLines = maxLines;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
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

    public void setTextPosition(int textPosition) {
        this.textPosition = textPosition;
    }
}
