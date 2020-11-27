package com.example.trackfield.toolbox;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.trackfield.objects.Exercise;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class Prefs {

    // file
    //static SharedPreferences sp;
    //static SharedPreferences.Editor editor;
    //static Gson gson = new Gson();

    // display options
    private boolean showWeekHeaders = true;
    private boolean weekDistance = true;
    private boolean showWeekChart = true;
    private boolean showDailyChart = true;

    // look
    private int color = 1;
    private boolean theme = false;

    // profile
    private float mass = 60;
    private LocalDate birthday = LocalDate.of(2020,1,1);

    // filtering
    private boolean showLesserRoutes = true;
    private boolean includeLonger = false;
    private ArrayList<Integer> exerciseVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN, Exercise.TYPE_INTERVALS, Exercise.TYPE_WALK));
    private ArrayList<Integer> routeVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN, Exercise.TYPE_INTERVALS));
    private ArrayList<Integer> distanceVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN));

    public static final int COLOR_MONO = 0;
    public static final int COLOR_GREEN = 1;

    ////

    public void setUpAutoSave(Context c) {
        //sp = c.getSharedPreferences(Toolbox.F.SP_SHARED_PREFERENCES, MODE_PRIVATE);
        //editor = sp.edit();
    }

    // set
    public void setShowWeekHeaders(boolean showWeekHeaders) {
        this.showWeekHeaders = showWeekHeaders;
        //savePref(showWeekHeaders, WEEK_HEADERS);
    }
    public void setWeekDistance(boolean weekDistance) {
        this.weekDistance = weekDistance;
    }
    public void setShowWeekChart(boolean showWeekChart) {
        this.showWeekChart = showWeekChart;
    }
    public void setShowDailyChart(boolean showDailyChart) {
        this.showDailyChart = showDailyChart;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public void setTheme(boolean theme) {
        this.theme = theme;
    }
    public void setMass(float mass) {
        this.mass = mass;
    }
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    public void setShowLesserRoutes(boolean showLesserRoutes) {
        this.showLesserRoutes = showLesserRoutes;
    }
    public void setIncludeLonger(boolean includeLonger) {
        this.includeLonger = includeLonger;
    }
    public void setExerciseVisibleTypes(ArrayList<Integer> exerciseVisibleTypes) {
        this.exerciseVisibleTypes = exerciseVisibleTypes;
    }
    public void setRouteVisibleTypes(ArrayList<Integer> routeVisibleTypes) {
        this.routeVisibleTypes = routeVisibleTypes;
    }
    public void setDistanceVisibleTypes(ArrayList<Integer> distanceVisibleTypes) {
        this.distanceVisibleTypes = distanceVisibleTypes;
    }

    public void setColorGreen() {
        color = COLOR_GREEN;
    }
    public void setColorMono() {
        color = COLOR_MONO;
    }

    // get
    public boolean showWeekHeaders() {
        return showWeekHeaders;
    }
    public boolean showWeekDistance() {
        return weekDistance;
    }
    public boolean showWeekChart() {
        return showWeekChart;
    }
    public boolean showDailyChart() {
        return showDailyChart;
    }
    public int getColor() {
        return color;
    }
    public boolean isThemeLight() {
        return theme;
    }
    public float getMass() {
        return mass;
    }
    public LocalDate getBirthday() {
        return birthday;
    }
    public boolean showLesserRoutes() {
        return showLesserRoutes;
    }
    public boolean includeLonger() {
        return includeLonger;
    }
    public ArrayList<Integer> getExerciseVisibleTypes() {
        return exerciseVisibleTypes;
    }
    public ArrayList<Integer> getRouteVisibleTypes() {
        return routeVisibleTypes;
    }
    public ArrayList<Integer> getDistanceVisibleTypes() {
        return distanceVisibleTypes;
    }

    // get driven
    public boolean isColorGreen() {
        return color == COLOR_GREEN;
    }
    public boolean isColorMono() {
        return color == COLOR_MONO;
    }

    // save data
    private void savePref(Object var, String tag) {
        //String json = gson.toJson(var);
        //editor.putString(tag, json);
        //editor.apply();
    }
    private <T> T loadPref(TypeToken token, String tag, SharedPreferences sp, Gson gson) {
        String json = sp.getString(tag, null);
        Type type = token.getType();
        return gson.fromJson(json, type);
    }

    private static final String LESSER_ROUTES = "lesserRoutes";
    private static final String SMALLEST_FIRST = "smallestFirst";
    private static final String SORT_MODE = "sortMode";
    private static final String WEEK_AMOUNT = "weekAmount";
    private static final String WEEK_DISTANCE = "weekDistance";
    private static final String WEEK_CHART = "weekChart";
    private static final String LOOK_COLOR = "color";
    private static final String LOOK_THEME = "theme";
    private static final String MASS = "mass";
    private static final String BIRTHDAY = "birthday";
    private static final String WEEK_HEADERS = "weekHeaders";
    private static final String TYPES_EXERCISE = "typesExercise";
    private static final String TYPES_ROUTE = "typesRoute";
    private static final String TYPES_DISTANCE = "typesDistance";

}
