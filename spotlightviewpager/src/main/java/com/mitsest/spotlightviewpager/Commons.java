package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

public class Commons {
    public static int dpToPx(@Nullable Context context, int dp) {
        if (context == null) {
            return dp;
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getDimenInPixels(@NonNull Context context, @DimenRes int dimen) {
        try {
            return (int) context.getResources().getDimension(dimen);
        } catch (Resources.NotFoundException e) {
            Log.d("spotlightviewpager", "", e);
            return 0;
        }
    }
}
