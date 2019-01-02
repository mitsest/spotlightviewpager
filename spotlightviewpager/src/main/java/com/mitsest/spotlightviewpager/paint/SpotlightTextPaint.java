package com.mitsest.spotlightviewpager.paint;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.DynamicLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.mitsest.spotlightviewpager.Commons;
import com.mitsest.spotlightviewpager.R;
import com.mitsest.spotlightviewpager.animation.OpacityDelegate;
import com.mitsest.spotlightviewpager.model.SpotlightViewModel;


public class SpotlightTextPaint {
    private final int paddingTop;
    private final int paddingLeft;

    @NonNull private TextPaint titlePaint;
    @NonNull private TextPaint subtitlePaint;
    @NonNull private TextPaint pageNumberPaint;
    @NonNull private OpacityDelegate textOpacity;

    private final int titleSize;
    private final int subtitleSize;
    @ColorInt private final int textColor;
    @Nullable private DynamicLayout titlePaintLayout;
    @Nullable private DynamicLayout subtitlePaintLayout;
    @Nullable private DynamicLayout pageNumberPaintLayout;
    private final int pageNumberSize;
    private int width;
    private int maxBottom;

    private int page;
    private int numberOfPages;

    public SpotlightTextPaint(@NonNull Context context) {
        titlePaint = new TextPaint();
        subtitlePaint = new TextPaint();
        pageNumberPaint = new TextPaint();
        textOpacity = new OpacityDelegate();

        paddingTop = Commons.getDimenInPixels(context, R.dimen.spotlight_text_padding_top);
        paddingLeft = Commons.getDimenInPixels(context, R.dimen.spotlight_text_padding_left);
        titleSize = Commons.getDimenInPixels(context, R.dimen.spotlight_text_title_dp);
        subtitleSize = Commons.getDimenInPixels(context, R.dimen.spotlight_text_subtitle_dp);
        pageNumberSize = Commons.getDimenInPixels(context, R.dimen.spotlight_page_number_dp);

        textColor = ContextCompat.getColor(context, R.color.spotlight_text_color);

        setTextTitlePaint(titleSize, textColor);
        setTextSubtitlePaint(subtitleSize, textColor);
        setTextPageNumberPaint(pageNumberSize, textColor);

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


    public boolean textFitsBottom(@NonNull final SpotlightViewModel viewModel) {
        return maxBottom - viewModel.bottom > viewModel.top;
    }

    public ValueAnimator getTextOpacityAnimation() {
        return textOpacity.getOpacityAnimator();
    }


    public void drawText(Canvas canvas, SpotlightViewModel viewModel) {

        canvas.save();

        canvas.translate(paddingLeft, 0);

        if (viewModel.getTextPosition() == SpotlightViewModel.TEXT_TOP) {
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
            titlePaint.setAlpha(textOpacity.getOpacity());
            titlePaintLayout.draw(canvas);
        }
    }

    private void drawSubtitle(Canvas canvas) {
        if (subtitlePaintLayout != null) {
            subtitlePaint.setAlpha(textOpacity.getOpacity());
            subtitlePaintLayout.draw(canvas);
        }
    }

    private void drawPageNumbers(Canvas canvas, int numberOfPages) {
        if (numberOfPages >= 2 && pageNumberPaintLayout != null) {
            pageNumberPaint.setAlpha(textOpacity.getOpacity());
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
            titlePaintLayout = Commons.getDynamicLayout(viewModel.getTitle(), titlePaint, width);
        }

        setSubtitlePaintEllipsize(viewModel, viewModel.getMaxLines());

        pageNumberPaintLayout = Commons.getDynamicLayout(page + "/" + numberOfPages, pageNumberPaint, width);
    }

    public void setMaxWidth(int width) {
        this.width = width - (paddingLeft * 2);
    }

    private void setSubtitlePaintEllipsize(@NonNull final SpotlightViewModel viewModel, int maxLines) {
        viewModel.setMaxLines(maxLines);

        if (!TextUtils.isEmpty(viewModel.getSubtitleString())) {
            fitSubtitlePaintLayout(viewModel, maxLines);
        }

    }

    @NonNull private void fitSubtitlePaintLayout(@NonNull final SpotlightViewModel viewModel, int maxLines) {
        subtitlePaintLayout = getSubtitlePaintLayout(viewModel.getSubtitleString(), width, maxLines);

        while (!textFits(viewModel) && maxLines >= 1) {
            subtitlePaintLayout = getSubtitlePaintLayout(viewModel.getSubtitleString(), width, --maxLines);
        }
    }

    @NonNull
    private DynamicLayout getSubtitlePaintLayout(String text, int width, int maxLines) {
        return Commons.getDynamicLayout(
                TextUtils.ellipsize(text, subtitlePaint, width * maxLines, TextUtils.TruncateAt.END),
                subtitlePaint,
                width
        );
    }

    private boolean textFits(@NonNull final SpotlightViewModel viewModel) {
        if (viewModel.getTextPosition() == SpotlightViewModel.TEXT_TOP) {
            return getTextOffsetTop(viewModel) >= 0;
        } else {
            return getTextOffsetBottom(viewModel) <= maxBottom;
        }
    }

    public void setMaxBottom(int bottom) {
        this.maxBottom = bottom;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void reset() {
        textOpacity = new OpacityDelegate();
    }
}
