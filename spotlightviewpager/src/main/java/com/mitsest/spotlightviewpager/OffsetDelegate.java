package com.mitsest.spotlightviewpager;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public class OffsetDelegate {
    private @NonNull
    int[] offsetArray;

    public OffsetDelegate() {
        this.offsetArray = new int[2];
    }

    private float getStartingLeft(int[] positionOnScreenArray) {
        return positionOnScreenArray[0] - offsetArray[0];
    }

    private float getStartingTop(int[] positionOnScreenArray) {
        return positionOnScreenArray[1] - offsetArray[1];
    }

    private float computeStartingLeft(int[] positionOnScreenArray, int safeArea) {
        return getStartingLeft(positionOnScreenArray) - safeArea;
    }

    private float computeStartingTop(int[] positionOnScreenArray, int safeArea) {
        return getStartingTop(positionOnScreenArray) - safeArea;
    }

    private float computeStartingRight(int[] positionOnScreenArray, int safeArea, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingLeft(positionOnScreenArray) + spotlightView.getWidth() + safeArea;
    }

    private float computeStartingBottom(int[] positionOnScreenArray, int safeArea, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingTop(positionOnScreenArray) + spotlightView.getHeight() + safeArea;
    }

    public @Nullable
    RectF getRectFFromView(@Nullable View spotlightView, int safeArea) {
        if (spotlightView == null) {
            return null;
        }

        int[] positionOnScreen = new int[2];
        spotlightView.getLocationInWindow(positionOnScreen);

        float rectLeft = computeStartingLeft(positionOnScreen, safeArea);
        float rectRight = computeStartingRight(positionOnScreen, safeArea, spotlightView);
        float rectTop = computeStartingTop(positionOnScreen, safeArea);
        float rectBottom = computeStartingBottom(positionOnScreen, safeArea, spotlightView);

        return new RectF(rectLeft, rectTop, rectRight, rectBottom);
    }

    protected void onLayoutDelegate(@NonNull View v, boolean changed, int left, int top, int right, int bottom) {
        v.getLocationInWindow(offsetArray);
    }

}
