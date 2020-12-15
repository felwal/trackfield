package com.example.trackfield.toolbox;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.trackfield.objects.Exercise;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class Prefs {

    // file
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static Gson gson = new Gson();

    private static boolean firstLogin = true;

    // display options
    private static boolean showWeekHeaders = true;
    private static boolean weekDistance = true;
    private static boolean showWeekChart = true;
    private static boolean showDailyChart = true;

    // look
    private static int color = 1;
    private static boolean theme = false;

    // profile
    private static float mass = 60;
    private static LocalDate birthday = LocalDate.of(2001,1,29);

    // filtering
    private static boolean showHiddenRoutes = true;
    private static boolean includeLonger = false;
    public static int distanceLowerLimit = 500;
    public static int distanceUpperLimit = 999;
    private static ArrayList<Integer> exerciseVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN, Exercise.TYPE_INTERVALS, Exercise.TYPE_WALK));
    private static ArrayList<Integer> routeVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN, Exercise.TYPE_INTERVALS));
    private static ArrayList<Integer> distanceVisibleTypes = new ArrayList<>(Arrays.asList(Exercise.TYPE_RUN));

    // sorting
    private static Toolbox.C.SortMode[] sortModePrefs = { Toolbox.C.SortMode.DATE, Toolbox.C.SortMode.DISTANCE, Toolbox.C.SortMode.DATE, Toolbox.C.SortMode.DATE, Toolbox.C.SortMode.DATE };
    private static boolean[] smallestFirstPrefs = { false, true, false, false, false };

    // Strava API
    private static String authCode = "";
    private static String refreshToken = "";
    private static String accessToken = "";
    private static LocalDateTime accessTokenExpiration = LocalDateTime.MIN;// = LocalDateTime.ofEpochSecond(0,0, ZoneOffset.UTC);

    public static final int COLOR_MONO = 0;
    public static final int COLOR_GREEN = 1;

    // tags
    public static final String SHARED_PREFERENCES = "shared preferences";
    private static final String FIRST_LOGIN = "firstLogin";
    private static final String WEEK_HEADERS = "weekHeaders";
    private static final String WEEK_DISTANCE = "weekDistance";
    private static final String WEEK_CHART = "weekChart";
    private static final String DAILY_CHART = "dailyChart";
    private static final String COLOR = "color";
    private static final String THEME = "theme";
    private static final String MASS = "mass";
    private static final String BIRTHDAY = "birthday";
    private static final String LESSER_ROUTES = "lesserRoutes";
    private static final String INCLCUDE_LONGER = "includeLonger";
    private static final String TYPES_EXERCISE = "typesExercise";
    private static final String TYPES_ROUTE = "typesRoute";
    private static final String TYPES_DISTANCE = "typesDistance";
    private static final String SORT_MODE = "sortModes";
    private static final String SORT_SMALLEST_FIRST = "smallestFirsts";
    private static final String STRAVA_AUTH = "stravaAuthCode";
    private static final String STRAVA_REFRESH = "stravaRefreshToken";
    private static final String STRAVA_ACCESS = "stravaAccessToken";
    private static final String STRAVA_ACCESS_EXP = "stravaAccessExpiration";

    ////

    public static void SetUpAndLoad(Context c) {
        sp = c.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = sp.edit();
        load();
    }
    private static void load() {

        TypeToken<Boolean> bool = new TypeToken<Boolean>(){};
        TypeToken<String> str = new TypeToken<String>(){};
        TypeToken<ArrayList<Integer>> intArr = new TypeToken<ArrayList<Integer>>(){};

        firstLogin = loadPref(bool, FIRST_LOGIN);

        showWeekHeaders = loadPref(bool, WEEK_HEADERS);
        weekDistance = loadPref(bool, WEEK_DISTANCE);
        showWeekChart = loadPref(bool, WEEK_CHART);
        showDailyChart = loadPref(bool, DAILY_CHART);

        color = loadPref(new TypeToken<Integer>(){}, COLOR);
        theme = loadPref(bool, THEME);

        mass = loadPref(new TypeToken<Float>(){}, MASS);
        birthday = loadPref(new TypeToken<LocalDate>(){}, BIRTHDAY);

        showHiddenRoutes = loadPref(bool, LESSER_ROUTES);
        includeLonger = loadPref(bool, INCLCUDE_LONGER);
        exerciseVisibleTypes = loadPref(intArr, TYPES_EXERCISE);
        routeVisibleTypes = loadPref(intArr, TYPES_ROUTE);
        distanceVisibleTypes = loadPref(intArr, TYPES_DISTANCE);

        sortModePrefs = loadPref(new TypeToken<Toolbox.C.SortMode[]>(){}, SORT_MODE);
        smallestFirstPrefs = loadPref(new TypeToken<boolean[]>(){}, SORT_SMALLEST_FIRST);

        authCode = loadPref(str, STRAVA_AUTH);
        refreshToken = loadPref(str, STRAVA_REFRESH);
        accessToken = loadPref(str, STRAVA_ACCESS);
        //accessTokenExpiration = loadPref(new TypeToken<LocalDateTime>(){}, STRAVA_ACCESS_EXP);
    }

    // tools
    private static void savePref(Object var, String tag) {
        String json = gson.toJson(var);
        editor.putString(tag, json);
        editor.apply();
    }
    private static <T> T loadPref(TypeToken token, String tag) {

        if (!sp.contains(tag)) savePref(ofTag(tag), tag);

        String json = sp.getString(tag, null);
        Type type = token.getType();
        return gson.fromJson(json, type);
    }
    private static Object ofTag(String tag) {
        switch (tag) {
            case FIRST_LOGIN: return firstLogin;
            case WEEK_HEADERS: return showWeekHeaders;
            case WEEK_DISTANCE: return weekDistance;
            case WEEK_CHART: return showWeekChart;
            case DAILY_CHART: return showDailyChart;
            case COLOR: return color;
            case THEME: return theme;
            case MASS: return mass;
            case BIRTHDAY: return birthday;
            case LESSER_ROUTES: return showHiddenRoutes;
            case INCLCUDE_LONGER: return includeLonger;
            case TYPES_EXERCISE: return exerciseVisibleTypes;
            case TYPES_ROUTE: return routeVisibleTypes;
            case TYPES_DISTANCE: return distanceVisibleTypes;
            case SORT_MODE: return sortModePrefs;
            case SORT_SMALLEST_FIRST: return smallestFirstPrefs;
            case STRAVA_AUTH: return authCode;
            case STRAVA_REFRESH: return refreshToken;
            case STRAVA_ACCESS: return accessToken;
            case STRAVA_ACCESS_EXP: return accessTokenExpiration;
            default: return new Object();
        }
    }

    // set
    public static void setFirstLogin(boolean firstLogin) {
        Prefs.firstLogin = firstLogin;
        savePref(firstLogin, FIRST_LOGIN);
    }
    public static void showWeekHeaders(boolean show) {
        showWeekHeaders = show;
        savePref(show, WEEK_HEADERS);
    }
    public static void showWeekDistance(boolean show) {
        weekDistance = show;
        savePref(show, WEEK_DISTANCE);
    }
    public static void showWeekChart(boolean show) {
        showWeekChart = show;
        savePref(show, WEEK_CHART);
    }
    public static void showDailyChart(boolean show) {
        showDailyChart = show;
        savePref(show, DAILY_CHART);
    }
    public static void setColor(int colorConst) {
        color = colorConst;
        savePref(colorConst, COLOR);
    }
    public static void setTheme(boolean light) {
        theme = light;
        savePref(light, THEME);
    }
    public static void setMass(float kilos) {
        mass = kilos;
        savePref(kilos, MASS);
    }
    public static void setBirthday(LocalDate date) {
        birthday = date;
        savePref(date, BIRTHDAY);
    }
    public static void showHiddenRoutes(boolean show) {
        showHiddenRoutes = show;
        savePref(show, LESSER_ROUTES);
    }
    public static void includeLonger(boolean include) {
        includeLonger = include;
        savePref(include, INCLCUDE_LONGER);
    }
    public static void setExerciseVisibleTypes(ArrayList<Integer> types) {
        exerciseVisibleTypes = types;
        savePref(types, TYPES_EXERCISE);
    }
    public static void setRouteVisibleTypes(ArrayList<Integer> types) {
        routeVisibleTypes = types;
        savePref(types, TYPES_ROUTE);
    }
    public static void setDistanceVisibleTypes(ArrayList<Integer> types) {
        distanceVisibleTypes = types;
        savePref(types, TYPES_DISTANCE);
    }
    public static void setSortModePref(Toolbox.C.Layout layout, Toolbox.C.SortMode sortMode) {
        sortModePrefs[layout.ordinal()] = sortMode;
        savePref(sortModePrefs, SORT_MODE);
    }
    public static void setSmallestFirstPref(Toolbox.C.Layout layout, boolean smallestFirst) {
        smallestFirstPrefs[layout.ordinal()] = smallestFirst;
        savePref(smallestFirstPrefs, SORT_SMALLEST_FIRST);
    }
    public static void setAuthCode(String authCode) {
        Prefs.authCode = authCode;
        savePref(authCode, STRAVA_AUTH);
    }
    public static void setRefreshToken(String refreshToken) {
        Prefs.refreshToken = refreshToken;
        savePref(refreshToken, STRAVA_REFRESH);
    }
    public static void setAccessToken(String accessToken) {
        Prefs.accessToken = accessToken;
        savePref(accessToken, STRAVA_ACCESS);
    }
    public static void setAccessTokenExpiration(LocalDateTime accessTokenExpiration) {
        Prefs.accessTokenExpiration = accessTokenExpiration;
        savePref(accessTokenExpiration, STRAVA_ACCESS_EXP);
    }

    public static void setColorGreen() {
        setColor(COLOR_GREEN);
    }
    public static void setColorMono() {
        setColor(COLOR_MONO);
    }

    // get
    public static boolean isFirstLogin() {
        return firstLogin;
    }
    public static boolean isWeekHeadersShown() {
        return showWeekHeaders;
    }
    public static boolean isWeekDistanceShown() {
        return weekDistance;
    }
    public static boolean isWeekChartShown() {
        return showWeekChart;
    }
    public static boolean isDailyChartShown() {
        return showDailyChart;
    }
    public static int getColor() {
        return color;
    }
    public static boolean isThemeLight() {
        return theme;
    }
    public static float getMass() {
        return mass;
    }
    public static LocalDate getBirthday() {
        return birthday;
    }
    public static boolean areHiddenRoutesShown() {
        return showHiddenRoutes;
    }
    public static boolean includeLonger() {
        return includeLonger;
    }
    public static ArrayList<Integer> getExerciseVisibleTypes() {
        return exerciseVisibleTypes;
    }
    public static ArrayList<Integer> getRouteVisibleTypes() {
        return routeVisibleTypes;
    }
    public static ArrayList<Integer> getDistanceVisibleTypes() {
        return distanceVisibleTypes;
    }
    public static Toolbox.C.SortMode getSortModePref(Toolbox.C.Layout layout) {
        return sortModePrefs[layout.ordinal()];
    }
    public static boolean getSmallestFirstPref(Toolbox.C.Layout layout) {
        return smallestFirstPrefs[layout.ordinal()];
    }
    public static String getAuthCode() {
        return authCode;
    }
    public static String getRefreshToken() {
        return refreshToken;
    }
    public static String getAccessToken() {
        return accessToken;
    }
    public static LocalDateTime getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // get driven
    public static boolean isColorGreen() {
        return color == COLOR_GREEN;
    }
    public static boolean isColorMono() {
        return color == COLOR_MONO;
    }
    public static boolean isAccessTokenCurrent() {
        boolean b = accessTokenExpiration != null;
        LocalDateTime now = LocalDateTime.now();
        return b && now.isBefore(accessTokenExpiration);
    }
    public static boolean isRefreshTokenCurrent() {
        return refreshToken != null && !refreshToken.equals("");
    }

}
