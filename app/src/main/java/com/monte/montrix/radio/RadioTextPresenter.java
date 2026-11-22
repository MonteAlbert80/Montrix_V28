package com.monte.montrix.radio;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/** RDS presentation only. No tuner commands or radio lifecycle logic here. */
public final class RadioTextPresenter {
    private RadioTextPresenter() {}

    public static void configureMarquee(TextView view) {
        view.setSingleLine(true);
        view.setHorizontallyScrolling(true);
        view.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        view.setMarqueeRepeatLimit(-1);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setSelected(true);
    }

    /** Full-width station header and wrapped RadioText, without duplicate PS line. */
    public static void show(TextView source, TextView headline, TextView detail,
                            String frequency, String radioText, String programService) {
        String f = clean(frequency, "FM");
        String rt = clean(radioText, "Waiting for RadioText");
        String ps = clean(programService, "FM radio");
        source.setText(ps + "  •  " + f);
        source.setTextSize(19);
        source.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        source.setTextColor(0xff5fc7ff);
        headline.setText(rt);
        headline.setTextSize(20);
        headline.setSingleLine(false);
        headline.setHorizontallyScrolling(false);
        headline.setEllipsize(null);
        headline.setMaxLines(3);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            headline.setAutoSizeTextTypeUniformWithConfiguration(14, 20, 1, 2);
        }
        headline.setSelected(false);
        headline.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        headline.setContentDescription("Complete radio text: " + rt);
        detail.setText("");
        detail.setVisibility(View.GONE);
        source.setContentDescription("Station: " + ps + ", frequency " + f);
    }

    /** Restore original two-line music layout after radio mode. */
    public static void restoreMusic(TextView source, TextView headline, TextView detail) {
        source.setTextSize(11);
        source.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL);
        source.setTextColor(0xff5abeff);
        detail.setVisibility(View.VISIBLE);
        configureMarquee(headline);
        configureMarquee(detail);
        headline.setTextSize(19);
    }

    private static String clean(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
