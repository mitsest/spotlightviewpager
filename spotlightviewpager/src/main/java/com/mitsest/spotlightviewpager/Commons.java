package com.mitsest.spotlightviewpager;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

public class Commons {
    public static int getDimenInPixels(@NonNull Context context, @DimenRes int dimen) {
        try {
            return (int) context.getResources().getDimension(dimen);
        } catch (Resources.NotFoundException e) {
            Log.d("spotlightviewpager", "getDimenInPixels exception", e);
            return 0;
        }
    }

    public static void removeOnGlobalLayoutListener(@NonNull View view, @NonNull ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }

    }

    public static class AnimationListener implements Animator.AnimatorListener {

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
            this.onAnimationStart(animation);
        }
    }
    
    public static DynamicLayout getDynamicLayout(@NonNull CharSequence text, @NonNull TextPaint paint, int width) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return DynamicLayout.Builder.obtain(
                    text, paint, width
            ).build();
        } else {
            return new DynamicLayout(
                text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }
    }
}
