package com.mitsest.spotlightviewpager;

import android.support.annotation.Nullable;

public class Subtitle {
    @Nullable
    private String subtitle;

    private int lineCount;


    private int textPosition;

    public Subtitle(@Nullable String subtitle, int lineCount) {
        this.subtitle = subtitle;
        this.lineCount = lineCount;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int textPosition) {
        this.textPosition = textPosition;
    }
}
