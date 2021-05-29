package com.example.trackfield.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.example.trackfield.R;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.graph.Borders;

public final class ScreenUtils {

    public static float scale;
    public static int statusBarHeight;

    //

    private ScreenUtils() {
        // this utility class is not publicly instantiable
    }

    // set

    /**
     * Updates activity theme to chosen theme. Should be called first thing in every activities onCreate.
     */
    public static void updateTheme(Activity a) {
        int newTheme = AppConsts.LOOKS[MathUtils.heaviside(Prefs.isThemeLight())][Prefs.getColor()];
        try {
            // currentTheme är alltid default theme?
            int currentTheme = a.getPackageManager().getActivityInfo(a.getComponentName(), 0).getThemeResource();
            if (currentTheme != newTheme) {
                a.setTheme(newTheme);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setScale(Activity a) {
        try {
            scale = a.getResources().getDisplayMetrics().density;
        }
        catch (Exception e) {
            LayoutUtils.handleError(R.string.toast_err_display_density, e, a);
        }

        // funkar inte / gör status svart
        // set statusBar size
        /*a.getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            statusBarHeight = insets.getSystemWindowInsetTop();
            return insets;
        });*/
    }

    public static void makeStatusBarTransparent(Window window, boolean darkIcons, @Nullable View top) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(darkIcons
            ? View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            : View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        // set optional margins
        window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            if (top != null) {
                // TODO: params margin finns inte?
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) top.getLayoutParams();
                int margin = params.topMargin + insets.getSystemWindowInsetTop();
                LayoutUtils.setMargin(top, margin, Borders.top());
            }
            return insets;
        });
    }

    public static void setWindowFlag(Window window, final int bits, boolean on) {
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) winParams.flags |= bits;
        else winParams.flags &= ~bits;
        window.setAttributes(winParams);
    }

    // get

    public static int px(float dp) {
        return (int) (dp * scale + 0.5f);
    }

    public static boolean isThemeLight() {
        int lightThemeMono = AppConsts.LOOKS[1][0];
        int lightThemeGreen = AppConsts.LOOKS[1][1];

        int currentTheme = AppConsts.LOOKS[MathUtils.heaviside(Prefs.isThemeLight())][Prefs.getColor()];
        return currentTheme == lightThemeMono || currentTheme == lightThemeGreen;
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

}
