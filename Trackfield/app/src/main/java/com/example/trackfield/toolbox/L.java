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

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Layout
public class L {

    public static float scale;

    // screen
    public static void setScale(Context c) {
        try {
            scale = c.getResources().getDisplayMetrics().density;
        } catch (Exception e) {
            toast("Couldn't fetch display density: " + e.getMessage(), c);
        }
    }
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
    public static int px(float dp) {
        return (int) (dp * scale + 0.5f);
    }

    public static void transStatusBar(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //setWindowFlag(window, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        //window.setStatusBarColor(Color.TRANSPARENT);
    }
    public static void setWindowFlag(Window window, final int bits, boolean on) {
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) winParams.flags |= bits;
        else winParams.flags &= ~bits;
        window.setAttributes(winParams);
    }

    // resources
    public static void ripple(View v, Context c) {
        int[] attrs = new int[] {R.attr.selectableItemBackground};
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        v.setBackgroundResource(backgroundResource);
        typedArray.recycle();
    }
    public static int getBackgroundResourceFromAttr(int attr, Context c) {
        int[] attrs = new int[] { attr };
        TypedArray typedArray = c.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        return backgroundResource;
    }
    @ColorInt public static int getColorInt(int resId, Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }
    public static void setColor(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    // check
    public static void checkReader(Helper.Reader reader, Context c) {
        if (reader == null) { reader = new Helper.Reader(c); }
    }
    public static void setVisibleOrGone(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    public static void setVisibleOrInvisible(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
    public static boolean isEmpty(EditText et) {
        if (et.getText().toString().trim().length() > 0) return false;
        return true;
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
            @Override public void onAnimationEnd(Animator animation) {
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
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
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

        @SuppressLint("ObjectAnimatorBinding")
        final ObjectAnimator colorFade = ObjectAnimator.ofInt(view, "background", fromColor, toColor);
        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
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

        @SuppressLint("ObjectAnimatorBinding")
        final ObjectAnimator colorFade = ObjectAnimator.ofInt(target, "backgroundTint", fromColor, toColor);
        colorFade.setDuration(ANIM_DURATION);
        colorFade.setEvaluator(new ArgbEvaluator());
        colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                target.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        colorFade.start();
        target.setImageDrawable(toIcon);
    }

}
