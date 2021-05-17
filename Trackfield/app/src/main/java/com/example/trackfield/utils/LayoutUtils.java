package com.example.trackfield.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;

import com.example.trackfield.R;
import com.example.trackfield.ui.custom.graph.Borders;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Layout
public final class LayoutUtils {

    public enum Direction {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }

    private static final int ANIM_DURATION = 100;
    private static final int ANIM_DURATION_LONG = 250;
    private static final int ANIM_DURATION_RECYCLER = 175;

    //

    private LayoutUtils() {
        // this utility class is not publicly instantiable
    }

    // set

    public static void setRipple(View v, Context c) {
        int[] attrs = new int[] { R.attr.selectableItemBackground };
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        v.setBackgroundResource(backgroundResource);
        typedArray.recycle();
    }

    public static void setColor(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void setMargin(View view, int margin, Borders dir) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        if (dir.isLeft()) params.setMargins(margin, 0, 0, 0);
        if (dir.isTop()) params.setMargins(0, margin, 0, 0);
        if (dir.isRight()) params.setMargins(0, 0, margin, 0);
        if (dir.isBottom()) params.setMargins(0, 0, 0, margin);

        view.setLayoutParams(params);
    }

    public static void setVisibleOrGone(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void setVisibleOrInvisible(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    // get

    @ColorInt
    public static int getColorInt(int resId, Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    /**
     * Gets resource id from attribute id.
     *
     * @param attrResId Attribute id to get
     * @param c Context
     * @return The resource id pointed to by the attribute id
     */
    public static int getAttr(int attrResId, Context c) {
        int[] attrs = new int[] { attrResId };
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        return backgroundResource;
    }

    public static boolean isEmpty(EditText et) {
        return et.getText().toString().trim().length() <= 0;
    }

    // toast

    public static void toast(String s, Context c) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    public static void toast(@StringRes int stringResId, Context c) {
        Toast.makeText(c, c.getString(stringResId), Toast.LENGTH_SHORT).show();
    }

    public static void toast(boolean b, Context c) {
        if (b) return;
        Toast.makeText(c, "success: " + b, Toast.LENGTH_SHORT).show();
    }

    public static void handleError(Exception e, Context c) {
        e.printStackTrace();
        Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    public static void handleError(String desc, Exception e, Context c) {
        e.printStackTrace();
        Toast.makeText(c, desc + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    public static void handleError(@StringRes int stringResId, Exception e, Context c) {
        e.printStackTrace();
        Toast.makeText(c, c.getString(stringResId) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    // animate

    public static void crossfade(View view, float toAlpha) {
        view.animate().alpha(toAlpha).setDuration(ANIM_DURATION).setListener(null);
    }

    public static void crossfadeIn(View view, float toAlpha) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(toAlpha).setDuration(ANIM_DURATION).setListener(null);
    }

    public static void crossfadeInLong(View view, float toAlpha) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(toAlpha).setDuration(ANIM_DURATION_LONG).setListener(null);
    }

    public static void crossfadeOut(final View view) {
        view.animate().alpha(0f).setDuration(ANIM_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
    }

    public static void crossfadeRecycler(View recycler) {
        recycler.setAlpha(0f);
        recycler.animate().alpha(1f).setDuration(ANIM_DURATION_RECYCLER).setListener(null);
    }

    public static void animateHeight(final View view, int toHeight) {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), toHeight);
        anim.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(ANIM_DURATION_LONG);
        anim.start();
    }

    public static void animateHeight(final View view, int expandedHeight, int collapsedHeight, boolean expand) {
        animateHeight(view, expand ? expandedHeight : collapsedHeight);
    }

    public static void animateHeight(final View view, int expandedHeight, boolean expand) {
        animateHeight(view, expandedHeight, 0, expand);
    }

    public static void animateColor(final View view, @ColorInt int fromColor, @ColorInt int toColor) {
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator colorFade = ObjectAnimator.ofInt(view, "background", fromColor, toColor);

        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            view.setBackgroundColor(animatedValue);
        });
        colorFade.start();
    }

    public static void animateColor(final View view, @ColorInt int disabledColor, @ColorInt int enabledColor,
        boolean enabled) {

        animateColor(view, enabled ? disabledColor : enabledColor, enabled ? enabledColor : disabledColor);
    }

    public static void animateFab(final FloatingActionButton target, @ColorInt int fromColor, @ColorInt int toColor,
        Drawable toIcon) {

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator colorFade = ObjectAnimator.ofInt(target, "backgroundTint", fromColor, toColor);

        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            target.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
        });
        colorFade.start();
        target.setImageDrawable(toIcon);
    }

}
