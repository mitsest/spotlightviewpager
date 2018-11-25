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
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;

public class SpotlightPaint {
    @Px private final int padding;
    @Px private final int borderSize;
    @ColorInt private final int borderColor;
    @NonNull private final Paint paint; // used to draw spotlight

    @Px private int radius;
    @Nullable private Paint borderGradientPaint; // used to draw spotlight border
    @Nullable private Paint borderPaint; // used to draw spotlight border

    SpotlightPaint(@NonNull Context context) {
        paint = new Paint();
        borderPaint = new Paint();
        borderGradientPaint = new Paint();


        padding = Commons.getDimenInPixels(context, R.dimen.spotlight_padding);
        borderColor = ContextCompat.getColor(context, R.color.spotlight_border_color);
        borderSize = Commons.getDimenInPixels(context, R.dimen.spotlight_border_size);
        setRadius(context);

        initSpotlightPaint();
        initSpotlightBorderPaint();
        setSpotlightBorderGradientPaint();

    }

    public void setRadius(@Nullable Context context) {
        if (context == null) {
            return;
        }

        radius = Commons.getDimenInPixels(context, R.dimen.spotlight_border_radius);
    }

    public void setSpotlightBorderGradientPaint() {
        borderGradientPaint = new Paint();

        borderGradientPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderGradientPaint.setAntiAlias(true);
        borderGradientPaint.setDither(true);
        borderGradientPaint.setStrokeWidth(borderSize);
        if (radius > 0) {
            borderGradientPaint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER));
        }

        borderGradientPaint.setColor(borderColor);

    }

    public void setSpotlightBorderGradientPaint(@Nullable Paint paint) {
        borderGradientPaint = paint;
    }

    public void initSpotlightPaint() {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void initSpotlightBorderPaint() {
        borderPaint = new Paint();

        borderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setDither(true);
        borderPaint.setStrokeWidth(borderSize);
        borderPaint.setColor(borderColor);

    }

    public void initSpotlightBorderPaint(@Nullable Paint paint) {
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

    public void setBorderPaint(@Nullable Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    @Nullable
    public Paint getBorderGradientPaint() {
        return borderGradientPaint;
    }

    public void setBorderGradientPaint(@Nullable Paint borderGradientPaint) {
        this.borderGradientPaint = borderGradientPaint;
    }

    public int getPadding() {
        return padding;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(@Px int radius) {
        this.radius = radius;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public int getBorderColor() {
        return borderColor;
    }
}
