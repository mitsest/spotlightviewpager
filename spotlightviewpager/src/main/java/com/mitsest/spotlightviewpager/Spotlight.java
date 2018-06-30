package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

public class Spotlight {
    private static final int PADDING_DP = 12;
    private int padding;
    private static final int RADIUS_DP = 24;
    private int radius;
    private static final int BORDER_SIZE_DP = 6;
    private int borderSize;

    private @ColorInt int borderColor;

    private @NonNull Paint paint; // used to draw spotlight
    private @Nullable Paint borderGradientPaint; // used to draw spotlight border
    private @Nullable Paint borderPaint; // used to draw spotlight border

    Spotlight(@NonNull Context context) {
        paint = new Paint();
        borderPaint = new Paint();
        borderGradientPaint = new Paint();


        padding = Commons.dpToPx(context, PADDING_DP);
        borderColor = ContextCompat.getColor(context, R.color.walkthrough_spotlight_border_color);
        borderSize = Commons.getDimenInPixels(context, R.dimen.walkthrough_border_size);

        setRadius(context);
        setSpotlightPaint();
        setSpotlightBorderPaint(borderSize, borderColor);
        setSpotlightBorderGradientPaint(borderSize, radius, borderColor);

    }

    public void setRadius(@Nullable Context context) {
        if (context == null) {
            return;
        }

        radius = Commons.dpToPx(context, RADIUS_DP);
    }

    public void setSpotlightBorderGradientPaint(int spotlightBorderSize, int spotlightRadius, @ColorInt int borderColor) {
        if (borderGradientPaint == null) {
            borderGradientPaint = new Paint();
        }

        borderGradientPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderGradientPaint.setAntiAlias(true);
        borderGradientPaint.setDither(true);
        borderGradientPaint.setStrokeWidth(spotlightBorderSize);
        borderGradientPaint.setMaskFilter(new BlurMaskFilter(spotlightRadius, BlurMaskFilter.Blur.OUTER));
        borderGradientPaint.setColor(borderColor);

    }

    public void setSpotlightBorderGradientPaint(@Nullable Paint paint) {
        borderGradientPaint = paint;
    }


    public void setSpotlightPaint() {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setSpotlightBorderPaint(int spotlightBorderSize, @ColorInt int borderColor) {
        if (borderPaint == null) {
            borderPaint = new Paint();
        }

        borderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setDither(true);
        borderPaint.setStrokeWidth(spotlightBorderSize);
        borderPaint.setColor(borderColor);

    }

    public void setSpotlightBorderPaint(@Nullable Paint paint) {
        borderPaint = paint;
    }

    void drawSpotlight(Canvas canvas, RectF animatingRectangle) {
        if (animatingRectangle != null) {
            canvas.drawRoundRect(animatingRectangle, radius, radius, paint);
        }
    }

    void drawSpotlightBorder(Canvas canvas, RectF animatingRectangle) {
        if (animatingRectangle != null) {

            if (borderPaint != null) {
                canvas.drawRoundRect(animatingRectangle, radius, radius, borderPaint);
            }

            if (borderGradientPaint != null) {
                canvas.drawRoundRect(animatingRectangle, radius, radius, borderGradientPaint);
            }
        }
    }

    @NonNull
    public Paint getPaint() {
        return paint;
    }

    @Nullable
    public Paint getBorderPaint() {
        return borderPaint;
    }

    @Nullable
    public Paint getBorderGradientPaint() {
        return borderGradientPaint;
    }

    public void setPaint(@NonNull Paint paint) {
        this.paint = paint;
    }

    public void setBorderGradientPaint(@Nullable Paint borderGradientPaint) {
        this.borderGradientPaint = borderGradientPaint;
    }

    public void setBorderPaint(@Nullable Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    public int getPadding() {
        return padding;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public int getBorderColor() {
        return borderColor;
    }
}
