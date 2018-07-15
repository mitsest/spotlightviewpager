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
    private static final int PADDING_TOP_DP = 16;
    private static final int PADDING_LEFT_DP = 16;
    private static final int TITLE_DP = 24;
    private static final int SUBTITLE_DP = 18;
    private static final int PAGE_NUMBER_DP = 12;
    int paddingTop;
    int paddingLeft;
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
    private int titleSize;
    private int subtitleSize;
    private int pageNumberSize;
    private @ColorInt
    int textColor;
    private int width;
    private int maxBottom;

    private int page;
    private int numberOfPages;

    public Text(@NonNull Context context) {
        titlePaint = new TextPaint();
        subtitlePaint = new TextPaint();
        pageNumberPaint = new TextPaint();


        paddingTop = Commons.dpToPx(context, PADDING_TOP_DP);
        paddingLeft = Commons.dpToPx(context, PADDING_LEFT_DP);
        pageNumberSize = Commons.dpToPx(context, PAGE_NUMBER_DP);
        textColor = ContextCompat.getColor(context, R.color.spotlight_text_color);
        titleSize = Commons.dpToPx(context, TITLE_DP);
        subtitleSize = Commons.dpToPx(context, SUBTITLE_DP);

        setTextTitlePaint(titleSize, textColor);
        setTextSubtitlePaint(subtitleSize, textColor);
        setTextPageNumberPaint(pageNumberSize, textColor);
    }

    private boolean textFitsToBottomOfSpotlight(@NonNull RectF rectF, int bottom) {
        float offset = getTextOffsetBottom(rectF);
        return offset <= bottom;
    }

    private float getTextOffsetBottom(@NonNull RectF rectF) {
        float offset = 0;
        offset += rectF.bottom + paddingTop;

        if (titlePaintLayout != null) {
            offset += titlePaintLayout.getHeight() + paddingTop;
        }

        if (subtitlePaintLayout != null) {
            offset += subtitlePaintLayout.getHeight() + paddingTop;
        }

        if (pageNumberPaintLayout != null) {
            offset += pageNumberPaintLayout.getHeight();
        }

        return offset;
    }

    private float getTextOffsetTop(@NonNull RectF animatingRectangle) {
        float offset = animatingRectangle.top - paddingTop;

        if (titlePaintLayout != null) {
            offset = offset - titlePaintLayout.getHeight() - paddingTop;
        }

        if (subtitlePaintLayout != null) {
            offset = offset - subtitlePaintLayout.getHeight() - paddingTop;
        }

        if (pageNumberPaintLayout != null) {
            offset = offset - pageNumberPaintLayout.getHeight() - paddingTop;
        }

        return offset;
    }


    boolean tryDrawingTextToBottomOfSpotlight(@NonNull final SpotlightViewModel viewModel) {
        return textFitsToBottomOfSpotlight(viewModel, maxBottom);
    }

    void drawText(Canvas canvas, SpotlightViewModel viewModel) {
        canvas.save();

        canvas.translate(paddingLeft, 0);

        if (viewModel.getTextPosition() == Subtitle.SUBTITLE_TOP) {
            float topOffset = getTextOffsetTop(viewModel);
            drawText(canvas, topOffset);
        } else {
            drawText(canvas, viewModel.bottom + paddingTop);
        }

        canvas.restore();
    }

    private void drawText(Canvas canvas, float startFrom) {
        float offset = startFrom;
        canvas.translate(0, offset);
        drawTitle(canvas);

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

    private void setTextPageNumberPaint(int textPageNumberSize, @ColorInt int spotlightTextColor) {
        pageNumberPaint.setAntiAlias(true);
        pageNumberPaint.setTextSize(textPageNumberSize);
        pageNumberPaint.setColor(spotlightTextColor);
    }

    private void setTextTitlePaint(int textTitleSize, @ColorInt int spotlightTextColor) {
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(textTitleSize);
        titlePaint.setColor(spotlightTextColor);

    }

    private void setTextSubtitlePaint(int textSubtitleSize, @ColorInt int spotlightTextColor) {
        subtitlePaint.setAntiAlias(true);
        subtitlePaint.setTextSize(textSubtitleSize);
        subtitlePaint.setColor(spotlightTextColor);

    }

    public void setText(@NonNull final SpotlightViewModel viewModel) {
        if (width < 0) {
            return;
        }

        if (!TextUtils.isEmpty(viewModel.getTitle())) {
            titlePaintLayout = new DynamicLayout(
                    viewModel.getTitle(), titlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }

        setSubtitlePaintEllipsize(viewModel, viewModel.getMaxLines());

        pageNumberPaintLayout = new DynamicLayout(
                page + "/" + numberOfPages, pageNumberPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
    }

    public void setMaxWidth(int width) {
        this.width = width - (paddingLeft * 2);
    }

    public void setSubtitlePaintEllipsize(@NonNull final SpotlightViewModel viewModel, int maxLines) {
        viewModel.setMaxLines(maxLines);

        if (!TextUtils.isEmpty(viewModel.getSubtitle())) {
            subtitlePaintLayout = new DynamicLayout(
                    TextUtils.ellipsize(viewModel.getSubtitle(), subtitlePaint, width * maxLines, TextUtils.TruncateAt.MIDDLE),
                    subtitlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

            while (subtitlePaintLayout.getLineCount() > viewModel.getMaxLines()) {
                --maxLines;
                subtitlePaintLayout = new DynamicLayout(
                        TextUtils.ellipsize(viewModel.getSubtitle(), subtitlePaint, width * maxLines, TextUtils.TruncateAt.MIDDLE),
                        subtitlePaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
            }
        }

    }

    public void setMaxBottom(int bottom) {
        this.maxBottom = bottom;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}
