package com.mitsest.spotlightviewpager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.animation.LinearInterpolator;

public class OpacityDelegate {

    private int opacity = 0;
    private static final int OPACITY_FULL = 235;

    public ValueAnimator getOpacityAnimator() {
        final ValueAnimator opacityAnim = ValueAnimator.ofInt(0, OPACITY_FULL);
        opacityAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    opacity = Integer.valueOf(animation.getAnimatedValue().toString());
                } catch (Exception e) {
                    //
                }
            }
        });

        return  opacityAnim;
    }

    public int getOpacity() {
        return opacity;
    }
}
