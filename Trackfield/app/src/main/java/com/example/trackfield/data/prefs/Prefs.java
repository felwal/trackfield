package com.example.trackfield.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.trackfield.R;

import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.model.PairList;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Prefs {

    // file
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static final Gson gson = new Gson();

    // app
    private static String appVersion;
    private static boolean developer = false;
    private static boolean firstLogin = true;

    // display options
    private static boolean showWeekHeaders = true;
    private static boolean weekDistance = true;
    private static boolean showDailyChart = true;

    // look
    private static int color = 1;
    private static boolean theme = false;

    // profile
    private static float mass = 60;
    private static LocalDate birthday = LocalDate.of(2001,1,29);

    // filtering
    private static boolean showHiddenRoutes = true;
    private static boolean hideSingletonRoutes = false;
    private static boolean includeLonger = false;
    private static boolean includePaceless = true; // TODO
    private static int distanceLowerLimit = 630;
    private static int distanceUpperLimit = 999;
    @NonNull private static ArrayList<Integer> exerciseVisibleTypes = new ArrayList<>();
    @NonNull private static ArrayList<Integer> routeVisibleTypes = new ArrayList<>();
    @NonNull private static ArrayList<Integer> distanceVisibleTypes = new ArrayList<>();

    // sorting
    private static AppConsts.SortMode[] sortModePrefs = { AppConsts.SortMode.DATE, AppConsts.SortMode.DISTANCE, AppConsts.SortMode.DATE,
        AppConsts.SortMode.DATE, AppConsts.SortMode.DATE, AppConsts.SortMode.DATE, AppConsts.SortMode.DATE };
    private static boolean[] smallestFirstPrefs = { false, true, false,
        false, false, false, false };

    // Strava API
    private static String authCode = "";
    private static String refreshToken = "";
    private static String accessToken = "";
    private static LocalDateTime accessTokenExpiration = LocalDateTime.MIN;
    private static String recordingMethod = "GPS";
    private static PairList<String, Boolean> pullSettings = new PairList<>(
        new Pair<>("Route", true),
        new Pair<>("Type", true),
        new Pair<>("Date and time", true),
        new Pair<>("Data source", true),
        new Pair<>("Distance", true),
        new Pair<>("Time", true),
        new Pair<>("Note", true),
        new Pair<>("Trail", true)
    );

    // consts
    public static final int COLOR_MONO = 0;
    public static final int COLOR_GREEN = 1;

    // tags
    private static final String SHARED_PREFERENCES = "shared preferences";
    private static final String APP_VERSION = "appVersion";
    private static final String DEVELOPER = "developer";
    private static final String FIRST_LOGIN = "firstLogin";
    private static final String WEEK_HEADERS = "weekHeaders";
    private static final String WEEK_DISTANCE = "weekDistance";
    private static final String WEEK_CHART = "weekChart";
    private static final String DAILY_CHART = "dailyChart";
    private static final String COLOR = "color";
    private static final String THEME = "theme";
    private static final String MASS = "mass";
    private static final String BIRTHDAY = "birthday";
    private static final String SHOW_HIDDEN_ROUTES = "showHiddenRoutes";
    private static final String HIDE_SINGLETON_ROUTES = "hideSingletonRoutes";
    private static final String INCLCUDE_LONGER = "includeLonger";
    private static final String INCLCUDE_PACELESS = "includePaceless";
    private static final String LIMIT_LOWER = "lowerLimit";
    private static final String LIMIT_UPPER = "upperLimit";
    private static final String TYPES_EXERCISE = "typesExercise";
    private static final String TYPES_ROUTE = "typesRoute";
    private static final String TYPES_DISTANCE = "typesDistance";
    private static final String SORT_MODE = "sortModes";
    private static final String SORT_SMALLEST_FIRST = "smallestFirsts";
    private static final String STRAVA_AUTH = "stravaAuthCode";
    private static final String STRAVA_REFRESH = "stravaRefreshToken";
    private static final String STRAVA_ACCESS = "stravaAccessToken";
    private static final String STRAVA_ACCESS_EXP = "stravaAccessExpiration";
    private static final String STRAVA_METHOD = "stravaRecordingMethod";
    private static final String STRAVA_PULL_SETTINGS = "stravaPullSettings";

    //

    /**
     * Sets up Shared Preferences and loads previously saved fields.
     * If first time loading, this also saves default values to Shared Preferences.
     *
     * <p>Must be called when initializing application.</p>
     *
     * @param c Context
     */
    public static void SetUpAndLoad(Context c) {
        sp = c.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = sp.edit();
        appVersion = c.getString(R.string.app_version);
        load(c);
    }

    private static void save() {
        // app
        savePref(appVersion, APP_VERSION);
        savePref(developer, DEVELOPER);
        savePref(firstLogin, FIRST_LOGIN); //

        // display
        savePref(showWeekHeaders, WEEK_HEADERS);
        savePref(weekDistance, WEEK_DISTANCE);
        savePref(showDailyChart, DAILY_CHART);

        // look
        savePref(color, COLOR);
        savePref(theme, THEME);

        // profile
        savePref(mass, MASS);
        savePref(birthday, BIRTHDAY);

        // filtering
        savePref(showHiddenRoutes, SHOW_HIDDEN_ROUTES);
        savePref(hideSingletonRoutes, HIDE_SINGLETON_ROUTES);
        savePref(includeLonger, INCLCUDE_LONGER);
        savePref(distanceLowerLimit, LIMIT_LOWER);
        savePref(distanceUpperLimit, LIMIT_UPPER);
        savePref(exerciseVisibleTypes, TYPES_EXERCISE);
        savePref(routeVisibleTypes, TYPES_ROUTE);
        savePref(distanceVisibleTypes, TYPES_DISTANCE);

        // sorting
        savePref(sortModePrefs, SORT_MODE);
        savePref(smallestFirstPrefs, SORT_SMALLEST_FIRST);

        // strava
        savePref(authCode, STRAVA_AUTH);
        savePref(refreshToken, STRAVA_REFRESH);
        savePref(accessToken, STRAVA_ACCESS);
        savePref(accessTokenExpiration, STRAVA_ACCESS_EXP);
        savePref(recordingMethod, STRAVA_METHOD);
        savePref(pullSettings, STRAVA_PULL_SETTINGS);
    }

    private static void load(Context c) {
        // type tokens used more than once
        TypeToken<Boolean> bool = new TypeToken<Boolean>(){};
        TypeToken<String> str = new TypeToken<String>(){};
        TypeToken<Integer> in = new TypeToken<Integer>(){};
        TypeToken<ArrayList<Integer>> intArr = new TypeToken<ArrayList<Integer>>(){};

        // version
        appVersion = loadPref(str, APP_VERSION);
        String targetVersion = c.getString(R.string.app_version);
        if (!appVersion.equals(targetVersion)) {
            // do upgrades here; resolve conflicts or save new default values
            appVersion = targetVersion;
            savePref(appVersion, APP_VERSION);
        }

        // app
        developer = loadPref(bool, DEVELOPER);
        firstLogin = loadPref(bool, FIRST_LOGIN);

        // display
        showWeekHeaders = loadPref(bool, WEEK_HEADERS);
        weekDistance = loadPref(bool, WEEK_DISTANCE);
        showDailyChart = loadPref(bool, DAILY_CHART);

        // look
        color = loadPref(new TypeToken<Integer>(){}, COLOR);
        theme = loadPref(bool, THEME);

        // profile
        mass = loadPref(new TypeToken<Float>(){}, MASS);
        birthday = loadPref(new TypeToken<LocalDate>(){}, BIRTHDAY);

        // filtering
        showHiddenRoutes = loadPref(bool, SHOW_HIDDEN_ROUTES);
        hideSingletonRoutes = loadPref(bool, HIDE_SINGLETON_ROUTES);
        includeLonger = loadPref(bool, INCLCUDE_LONGER);
        distanceLowerLimit = loadPref(in, LIMIT_LOWER);
        distanceUpperLimit = loadPref(in, LIMIT_UPPER);
        exerciseVisibleTypes = loadPref(intArr, TYPES_EXERCISE);
        routeVisibleTypes = loadPref(intArr, TYPES_ROUTE);
        distanceVisibleTypes = loadPref(intArr, TYPES_DISTANCE);

        // sorting
        sortModePrefs = loadPref(new TypeToken<AppConsts.SortMode[]>(){}, SORT_MODE);
        smallestFirstPrefs = loadPref(new TypeToken<boolean[]>(){}, SORT_SMALLEST_FIRST);

        // strava
        authCode = loadPref(str, STRAVA_AUTH);
        refreshToken = loadPref(str, STRAVA_REFRESH);
        accessToken = loadPref(str, STRAVA_ACCESS);
        accessTokenExpiration = loadPref(new TypeToken<LocalDateTime>(){}, STRAVA_ACCESS_EXP);
        recordingMethod = loadPref(str, STRAVA_METHOD);
        pullSettings = loadPref(new TypeToken<PairList<String, Boolean>>(){}, STRAVA_PULL_SETTINGS);
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
            case APP_VERSION: return appVersion;
            case DEVELOPER: return developer;
            case FIRST_LOGIN: return firstLogin;
            case WEEK_HEADERS: return showWeekHeaders;
            case WEEK_DISTANCE: return weekDistance;
            case DAILY_CHART: return showDailyChart;
            case COLOR: return color;
            case THEME: return theme;
            case MASS: return mass;
            case BIRTHDAY: return birthday;
            case SHOW_HIDDEN_ROUTES: return showHiddenRoutes;
            case HIDE_SINGLETON_ROUTES: return hideSingletonRoutes;
            case INCLCUDE_LONGER: return includeLonger;
            case LIMIT_LOWER: return distanceLowerLimit;
            case LIMIT_UPPER: return distanceUpperLimit;
            case TYPES_EXERCISE: return exerciseVisibleTypes;
            case TYPES_ROUTE: return routeVisibleTypes;
            case TYPES_DISTANCE: return distanceVisibleTypes;
            case SORT_MODE: return sortModePrefs;
            case SORT_SMALLEST_FIRST: return smallestFirstPrefs;
            case STRAVA_AUTH: return authCode;
            case STRAVA_REFRESH: return refreshToken;
            case STRAVA_ACCESS: return accessToken;
            case STRAVA_ACCESS_EXP: return accessTokenExpiration;
            case STRAVA_METHOD: return recordingMethod;
            case STRAVA_PULL_SETTINGS: return pullSettings;
            default: return new Object();
        }
    }

    // set

    public static void setDeveloper(boolean developer) {
        Prefs.developer = developer;
        savePref(developer, DEVELOPER);
    }

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
        savePref(show, SHOW_HIDDEN_ROUTES);
    }

    public static void hideSingletonRoutes(boolean hide) {
        hideSingletonRoutes = hide;
        savePref(hide, HIDE_SINGLETON_ROUTES);
    }

    public static void includeLonger(boolean include) {
        includeLonger = include;
        savePref(include, INCLCUDE_LONGER);
    }

    public static void setDistanceLowerLimit(int distanceLowerLimit) {
        Prefs.distanceLowerLimit = distanceLowerLimit;
        savePref(distanceLowerLimit, LIMIT_LOWER);
    }

    public static void setDistanceUpperLimit(int distanceUpperLimit) {
        Prefs.distanceUpperLimit = distanceUpperLimit;
        savePref(distanceUpperLimit, LIMIT_UPPER);
    }

    public static void setExerciseVisibleTypes(@NonNull ArrayList<Integer> types) {
        exerciseVisibleTypes = types;
        savePref(types, TYPES_EXERCISE);
    }

    public static void setRouteVisibleTypes(@NonNull ArrayList<Integer> types) {
        routeVisibleTypes = types;
        savePref(types, TYPES_ROUTE);
    }

    public static void setDistanceVisibleTypes(@NonNull ArrayList<Integer> types) {
        distanceVisibleTypes = types;
        savePref(types, TYPES_DISTANCE);
    }

    public static void setSortModePref(AppConsts.Layout layout, AppConsts.SortMode sortMode) {
        sortModePrefs[layout.ordinal()] = sortMode;
        savePref(sortModePrefs, SORT_MODE);
    }

    public static void setSmallestFirstPref(AppConsts.Layout layout, boolean smallestFirst) {
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

    public static void setRecordingMethod(String recordingMethod) {
        Prefs.recordingMethod = recordingMethod;
        savePref(recordingMethod, STRAVA_METHOD);
    }

    public static void setPullSettings(PairList<String, Boolean> pullSettings) {
        Prefs.pullSettings = pullSettings;
        savePref(pullSettings, STRAVA_PULL_SETTINGS);
    }

    public static void setColorGreen() {
        setColor(COLOR_GREEN);
    }

    public static void setColorMono() {
        setColor(COLOR_MONO);
    }

    // get

    public static boolean isDeveloper() {
        return developer;
    }

    public static boolean isFirstLogin() {
        return firstLogin;
    }

    public static boolean isWeekHeadersShown() {
        return showWeekHeaders;
    }

    public static boolean isWeekDistanceShown() {
        return weekDistance;
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

    public static boolean areSingletonRoutesHidden() {
        return hideSingletonRoutes;
    }

    public static boolean includeLonger() {
        return includeLonger;
    }

    public static int getDistanceLowerLimit() {
        return distanceLowerLimit;
    }

    public static int getDistanceUpperLimit() {
        return distanceUpperLimit;
    }

    @NonNull
    public static ArrayList<Integer> getExerciseVisibleTypes() {
        return exerciseVisibleTypes;
    }

    @NonNull
    public static ArrayList<Integer> getRouteVisibleTypes() {
        return routeVisibleTypes;
    }

    @NonNull
    public static ArrayList<Integer> getDistanceVisibleTypes() {
        return distanceVisibleTypes;
    }

    public static AppConsts.SortMode getSortModePref(AppConsts.Layout layout) {
        return sortModePrefs[layout.ordinal()];
    }

    public static boolean getSmallestFirstPref(AppConsts.Layout layout) {
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

    public static String getRecordingMethod() {
        return recordingMethod;
    }

    public static PairList<String, Boolean> getPullSettings() {
        return pullSettings;
    }

    // get driven

    public static String printTheme() {
        return isThemeLight() ? "Light" : "Dark";
    }

    public static String printColor() {
        return getColor() == 0 ? "Mono" : "Green";
    }

    public static boolean isColorGreen() {
        return color == COLOR_GREEN;
    }

    public static boolean isColorMono() {
        return color == COLOR_MONO;
    }

    public static int getThemeInt() {
        return theme ? 1 : 0;
    }

    public static boolean isAccessTokenCurrent() {
        boolean notNull = accessTokenExpiration != null && accessTokenExpiration.toLocalDate() != null && accessTokenExpiration.toLocalTime() != null;
        LocalDateTime now = LocalDateTime.now();
        return notNull && now.isBefore(accessTokenExpiration);
    }

    public static boolean isRefreshTokenCurrent() {
        return refreshToken != null && !refreshToken.equals("");
    }

    public static String getMapId(Context c) {
        return c.getResources().getString(theme ? R.string.map_id_light : R.string.map_id_dark);
    }

    public static MapStyleOptions getMapStyle(Context c) {
        return new MapStyleOptions(c.getResources().getString(theme ? R.string.mapstyle_retro_json : R.string.mapstyle_mono_json));// C.MAP_STYLES[M.heaviside(theme)]));
    }

}
