package com.mitsest.spotlightviewpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;

public class Text {
    static final int PADDING_TOP_DP = 16;
    int paddingTop;
    static final int PADDING_LEFT_DP = 16;
    int paddingLeft;

    private static final int TITLE_DP = 24;
    private int titleSize;
    private static final int SUBTITLE_DP = 18;
    private int subtitleSize;
    private static final int PAGE_NUMBER_DP = 12;
    private int pageNumberSize;

    private @ColorInt
    int textColor;

    @NonNull
    TextPaint titlePaint; // used to draw title text
    @Nullable
    DynamicLayout titlePaintLayout;
    @NonNull
    TextPaint subtitlePaint; // used to draw subtitle
    @Nullable
    DynamicLayout subtitlePaintLayout;
    @NonNull
    TextPaint pageNumberPaint; // used to draw page numbers
    @Nullable
    DynamicLayout pageNumberPaintLayout;


    public Text(@NonNull Context context) {
        titlePaint = new TextPaint();
        subtitlePaint = new TextPaint();
        pageNumberPaint = new TextPaint();

        paddingTop = Commons.dpToPx(context, PADDING_TOP_DP);
        paddingLeft = Commons.dpToPx(context, PADDING_LEFT_DP);
        pageNumberSize = Commons.dpToPx(context, PAGE_NUMBER_DP);
        textColor = ContextCompat.getColor(context, R.color.walkthrough_text_color);
        titleSize = Commons.dpToPx(context, TITLE_DP);
        subtitleSize = Commons.dpToPx(context, SUBTITLE_DP);

        setTextTitlePaint(titleSize, textColor);
        setTextSubtitlePaint(subtitleSize, textColor);
        setTextPageNumberPaint(pageNumberSize, textColor);
    }

    void drawText(Canvas canvas, RectF animatingRectangle, boolean shouldDrawTextToTheBottomOfSpotlight) {
        canvas.save();

        canvas.translate(paddingLeft, 0);

        if (!shouldDrawTextToTheBottomOfSpotlight) {
            drawTextUp(canvas, animatingRectangle);
        } else {
            drawTextDown(canvas, animatingRectangle);
        }

        canvas.restore();
    }

    private void drawTextUp(Canvas canvas, RectF animatingRectangle) {
        float offset = 0;

        if (pageNumberPaintLayout != null) {
            if (animatingRectangle != null) {
                offset = animatingRectangle.top - paddingTop - pageNumberPaintLayout.getHeight();
            }

            canvas.translate(0, offset);
            drawPageNumbers(canvas, pageNumberSize);
        }

        if (subtitlePaintLayout != null) {
            offset = -paddingTop - subtitlePaintLayout.getHeight();
            canvas.translate(0, offset);
            drawSubtitle(canvas);
        }

        if (titlePaintLayout != null) {
            offset = -paddingTop - titlePaintLayout.getHeight();
            canvas.translate(0, offset);
            drawTitle(canvas);
        }
    }

    private void drawTextDown(Canvas canvas, RectF animatingRectangle) {
        float offset = 0;
        if (titlePaintLayout != null) {
            if (animatingRectangle != null) {
                offset = animatingRectangle.bottom + paddingTop;
            }

            canvas.translate(0, offset);
            drawTitle(canvas);
        }

        if (subtitlePaintLayout != null) {
            if (titlePaintLayout != null) {
                offset = titlePaintLayout.getHeight() + paddingTop;
            }

            canvas.translate(0, offset);
            drawSubtitle(canvas);
        }

        if (pageNumberPaintLayout != null) {
            if (subtitlePaintLayout != null) {
                offset = subtitlePaintLayout.getHeight() + paddingTop;
            }

            canvas.translate(0, offset);
            drawPageNumbers(canvas, pageNumberSize);
        }
    }

    private void drawTitle(Canvas canvas) {
        if (titlePaintLayout != null) {
            titlePaintLayout.draw(canvas);
        }
    }

    private void drawSubtitle(Canvas canvas) {
        if (subtitlePaintLayout != null) {
            subtitlePaintLayout.draw(canvas);
        }
    }

    private void drawPageNumbers(Canvas canvas, int numberOfPages) {
        if (numberOfPages >= 2 && pageNumberPaintLayout != null) {
            pageNumberPaintLayout.draw(canvas);
        }
    }

    private void setTextPageNumberPaint(int textPageNumberSize, @ColorInt int walkthroughTextColor) {
        pageNumberPaint.setAntiAlias(true);
        pageNumberPaint.setTextSize(textPageNumberSize);
        pageNumberPaint.setColor(walkthroughTextColor);
    }

    private void setTextTitlePaint(int textTitleSize, @ColorInt int walkthroughTextColor) {
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(textTitleSize);
        titlePaint.setColor(walkthroughTextColor);

    }

    private void setTextSubtitlePaint(int textSubtitleSize, @ColorInt int walkthroughTextColor) {
        subtitlePaint.setAntiAlias(true);
        subtitlePaint.setTextSize(textSubtitleSize);
        subtitlePaint.setColor(walkthroughTextColor);

    }

    public void setText(@NonNull final WalkthroughViewModel viewModel, int maxWidth, int numberOfPages, int page) {
        final int width = maxWidth - paddingLeft * 2;

        if (width < 0) {
            return;
        }

        if (!TextUtils.isEmpty(viewModel.getTitle())) {
            titlePaintLayout = new DynamicLayout(
                    viewModel.getTitle(), titlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }

        final String subtitle = viewModel.getSubtitle();
        if (!TextUtils.isEmpty(subtitle)) {
            subtitlePaintLayout = new DynamicLayout(
                    TextUtils.ellipsize(subtitle, subtitlePaint, width * 4, TextUtils.TruncateAt.MIDDLE),
                    subtitlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }

        pageNumberPaintLayout = new DynamicLayout(
                String.valueOf(page) + "/" + numberOfPages, pageNumberPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

    }




}
