package com.mitsest.spotlightviewpager.animation;

import android.animation.ValueAnimator;

public class OpacityDelegate {

    private static final int OPACITY_FULL = 235;
    private int opacity = 0;

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

        return opacityAnim;
    }

    public int getOpacity() {
        return opacity;
    }
}
