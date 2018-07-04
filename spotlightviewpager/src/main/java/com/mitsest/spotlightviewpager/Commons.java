package com.mitsest.spotlightviewpager;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

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

    public static void removeOnGlobalLayoutListenerTG(@NonNull View view, @NonNull ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }

    }

    public static class AnimationListenerTG implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        @CallSuper
        public void onAnimationCancel(Animator animation) {
            this.onAnimationEnd(animation);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

}
