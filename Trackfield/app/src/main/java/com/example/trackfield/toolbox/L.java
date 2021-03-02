package com.example.trackfield.toolbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.example.trackfield.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Layout
public class L {

    public static float scale;

    public enum Direction { LEFT, TOP, RIGHT, BOTTOM }

    // screen

    public static int px(float dp) {
        return (int) (dp * scale + 0.5f);
    }

    // set

    public static void setScale(Context c) {
        try {
            scale = c.getResources().getDisplayMetrics().density;
        }
        catch (Exception e) {
            handleError(c.getString(R.string.toast_err_display_density), e, c);
        }
    }

    public static void setRipple(View v, Context c) {
        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        v.setBackgroundResource(backgroundResource);
        typedArray.recycle();
    }

    public static int getBackgroundResourceFromAttr(int attr, Context c) {
        int[] attrs = new int[]{attr};
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        return backgroundResource;
    }

    public static void setColor(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void makeStatusBarTransparent(Window window, boolean darkIcons, @Nullable View top) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                darkIcons ? View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR :
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        // set optional margins
        window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            if (top != null) {
                // TODO: params margin finns inte?
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) top.getLayoutParams();
                int margin = params.topMargin + insets.getSystemWindowInsetTop();
                setMargin(top, margin, Direction.TOP);
            }
            return insets;
        });
        //getWindow().getDecorView().requestApplyInsets();

        
        // försök 2
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // försök 1
        //setWindowFlag(window, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        //window.setStatusBarColor(Color.TRANSPARENT);
    }

    public static void setMargin(View view, int margin, Direction dir) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        switch (dir) {
            case LEFT:
                params.setMargins(margin, 0, 0, 0);
                break;
            case TOP:
                params.setMargins(0, margin, 0, 0);
                break;
            case RIGHT:
                params.setMargins(0, 0, margin, 0);
                break;
            case BOTTOM:
                params.setMargins(0, 0, 0, margin);
                break;
        }

        view.setLayoutParams(params);
    }

    public static void setWindowFlag(Window window, final int bits, boolean on) {
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) winParams.flags |= bits;
        else winParams.flags &= ~bits;
        window.setAttributes(winParams);
    }

    public static void setVisibleOrGone(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void setVisibleOrInvisible(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    // get

    public static int getScreenWidth(Activity a) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity a) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @ColorInt
    public static int getColorInt(int resId, Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static boolean isEmpty(EditText et) {
        if (et.getText().toString().trim().length() > 0) return false;
        return true;
    }

    // toast

    public static void toast(String s, Context c) {
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
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

    // animate

    private static final int ANIM_DURATION = 100;
    private static final int ANIM_DURATION_LONG = 250;
    private static final int ANIM_DURATION_RECYCLER = 175;

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
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
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

        @SuppressLint("ObjectAnimatorBinding") final ObjectAnimator colorFade = ObjectAnimator.ofInt(view, "background", fromColor, toColor);
        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                view.setBackgroundColor(animatedValue);
            }
        });
        colorFade.start();
    }

    public static void animateColor(final View view, @ColorInt int disabledColor, @ColorInt int enabledColor, boolean enabled) {
        animateColor(view, enabled ? disabledColor : enabledColor, enabled ? enabledColor : disabledColor);
    }

    public static void animateFab(final FloatingActionButton target, @ColorInt int fromColor, @ColorInt int toColor, Drawable toIcon) {

        @SuppressLint("ObjectAnimatorBinding") final ObjectAnimator colorFade = ObjectAnimator.ofInt(target, "backgroundTint", fromColor, toColor);
        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                target.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        colorFade.start();
        target.setImageDrawable(toIcon);
    }

}
