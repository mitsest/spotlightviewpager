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

    private static float getStartingLeft(int[] positionOnScreenArray, int[] offsetArray) {
        return positionOnScreenArray[0] - offsetArray[0];
    }

    private static float getStartingTop(int[] positionOnScreenArray, int[] offsetArray) {
        return positionOnScreenArray[1] - offsetArray[1];
    }

    private static float computeStartingLeft(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding) {
        return getStartingLeft(positionOnScreenArray, offsetArray) - spotLightPadding;
    }

    private static float computeStartingTop(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding) {
        return getStartingTop(positionOnScreenArray, offsetArray) - spotLightPadding;
    }

    private static float computeStartingRight(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingLeft(positionOnScreenArray, offsetArray) + spotlightView.getWidth() + spotLightPadding;
    }

    private static float computeStartingBottom(int[] positionOnScreenArray, int[] offsetArray, int spotLightPadding, @Nullable final View spotlightView) {
        if (spotlightView == null) {
            return 0;
        }

        return getStartingTop(positionOnScreenArray, offsetArray) + spotlightView.getHeight() + spotLightPadding;
    }

    public @Nullable
    RectF getRectFFromView(@Nullable View spotlightView, int safeArea) {
        if (spotlightView == null) {
            return null;
        }

        int[] positionOnScreen = new int[2];
        spotlightView.getLocationInWindow(positionOnScreen);

        float rectLeft = computeStartingLeft(positionOnScreen, offsetArray, safeArea);
        float rectRight = computeStartingRight(positionOnScreen, offsetArray, safeArea, spotlightView);
        float rectTop = computeStartingTop(positionOnScreen, offsetArray, safeArea);
        float rectBottom = computeStartingBottom(positionOnScreen, offsetArray, safeArea, spotlightView);

        return new RectF(rectLeft, rectTop, rectRight, rectBottom);
    }

    protected void onLayoutDelegate(@NonNull View v, boolean changed, int left, int top, int right, int bottom) {
        v.getLocationInWindow(offsetArray);
    }

}
