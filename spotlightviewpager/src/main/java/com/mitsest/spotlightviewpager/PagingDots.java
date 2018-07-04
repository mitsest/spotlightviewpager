package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class PagingDots {
    private static final int SIZE_DP = 4;
    private static final int ITEM_MARGIN_DP = 12;
    private static final int MARGIN_BOTTOM_DB = 30;
    private int size;
    private int itemMargin;
    private int marginBottom;

    private @ColorInt
    int activePageColor;
    private @ColorInt
    int inactivePageColor;

    private @NonNull
    Paint paint; // use to draw page dots

    public PagingDots(@NonNull Context context) {
        paint = new Paint();

        size = Commons.dpToPx(context, SIZE_DP);
        marginBottom = Commons.dpToPx(context, MARGIN_BOTTOM_DB);
        itemMargin = Commons.dpToPx(context, ITEM_MARGIN_DP);

        activePageColor = ContextCompat.getColor(context, R.color.spotlight_active_page_color);
        inactivePageColor = ContextCompat.getColor(context, R.color.spotlight_inactive_page_color);
    }

    void drawPageIndicators(Canvas canvas, int numberOfPages, int page) {

        if (numberOfPages < 2) {
            return;
        }

        canvas.save();
        moveCanvasToBottomCenterVertical(canvas, numberOfPages);

        for (int t = 1; t <= numberOfPages; t++) {
            if (t == page) {
                paint.setColor(activePageColor);
            } else {
                paint.setColor(inactivePageColor);
            }

            canvas.drawCircle(0, 0, size, paint);
            canvas.translate(size * 2 + itemMargin, 0);
        }

        canvas.restore();
    }

    private void moveCanvasToBottomCenterVertical(Canvas canvas, int numberOfPages) {
        int pageContainerSize = numberOfPages * (size + itemMargin) - itemMargin;
        canvas.translate(canvas.getWidth() / 2 - pageContainerSize / 2, canvas.getHeight() - marginBottom);
    }

    public int getSize() {
        return size;
    }

    public int getMarginBottom() {
        return marginBottom;
    }
}


