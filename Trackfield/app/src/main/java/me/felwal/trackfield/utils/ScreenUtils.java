package me.felwal.trackfield.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.widget.graph.Borders;

public final class ScreenUtils {

    public static float scale;

    //

    private ScreenUtils() {
        // this utility class is not publicly instantiable
    }

    // set

    /**
     * Updates activity theme to chosen theme. Should be called first thing in every activities onCreate.+
     */
    public static void updateTheme(Activity a) {
        // theme
        int nightMode;
        switch (Prefs.getTheme()) {
            case 0: nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
            case 1: nightMode = AppCompatDelegate.MODE_NIGHT_NO; break;
            case 3: nightMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY; break;
            default: nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);

        // color
        try {
            int newTheme = AppConsts.COLORS[Prefs.getColor()];
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
        if (top != null) {
            window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                //ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) top.getLayoutParams();
                int margin = /*params.topMargin +*/ insets.getSystemWindowInsetTop();
                LayoutUtils.setMargin(top, margin, Borders.top());
                return insets;
            });
        }
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

    public static boolean isThemeLight(Context c) {
        int nightModeFlags = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_NO;
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
