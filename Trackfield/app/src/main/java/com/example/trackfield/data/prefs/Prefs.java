package com.example.trackfield.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.trackfield.R;

import com.example.trackfield.data.network.StravaApi;
import com.example.trackfield.utils.annotations.Unimplemented;
import com.example.trackfield.utils.model.SwitchChain;
import com.example.trackfield.utils.model.SwitchItem;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.annotations.Debug;
import com.example.trackfield.utils.annotations.Unfinished;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Prefs {

    public static final int COLOR_MONO = 0;
    public static final int COLOR_GREEN = 1;

    private static final String NAME_SHARED_PREFERENCES = "sharedPreferences";

    private static final String KEY_APP_VERSION = "appVersion";
    private static final String KEY_DEVELOPER = "developer";
    private static final String KEY_FIRST_LOGIN = "firstLogin";
    private static final String KEY_WEEK_HEADERS = "weekHeaders";
    private static final String KEY_DAILY_CHART = "dailyChart";
    private static final String KEY_COLOR = "color";
    private static final String KEY_THEME = "theme";
    private static final String KEY_MASS = "mass";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_SHOW_HIDDEN_ROUTES = "showHiddenRoutes";
    private static final String KEY_HIDE_SINGLETON_ROUTES = "hideSingletonRoutes";
    @Unimplemented private static final String KEY_INCLCUDE_LONGER = "includeLonger";
    @Unfinished private static final String KEY_INCLCUDE_PACELESS = "includePaceless";
    private static final String KEY_LIMIT_LOWER = "lowerLimit";
    private static final String KEY_LIMIT_UPPER = "upperLimit";
    private static final String KEY_TYPES_EXERCISE = "typesExercise";
    private static final String KEY_TYPES_ROUTE = "typesRoute";
    private static final String KEY_TYPES_DISTANCE = "typesDistance";
    private static final String KEY_SORT_SELECTED_INDICES = "sorterSelectedIndices";
    private static final String KEY_SORT_SELECTED_INVERSIONS = "sorterSelectedInversions";
    private static final String KEY_STRAVA_AUTH = "stravaAuthCode";
    private static final String KEY_STRAVA_REFRESH = "stravaRefreshToken";
    private static final String KEY_STRAVA_ACCESS = "stravaAccessToken";
    private static final String KEY_STRAVA_ACCESS_EXP_DATE = "stravaAccessExpiration";
    private static final String KEY_STRAVA_DEVICE = "stravaDevice";
    private static final String KEY_STRAVA_METHOD = "stravaRecordingMethod";
    private static final String KEY_STRAVA_PULL_POLICY = "stravaPullPolicy";

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
    private static boolean showDailyChart = true;
    private static boolean hideSingletonRoutes = false;

    // look
    private static int color = 1;
    private static boolean theme = false;

    // profile
    private static float mass = 60;
    private static LocalDate birthday = LocalDate.of(2001,1,29);

    // filtering
    private static boolean showHiddenRoutes = true;
    private static boolean includeLonger = false;
    @Unfinished private static boolean includePaceless = true;
    private static int distanceLowerLimit = 630;
    private static int distanceUpperLimit = 999;
    @NonNull private static ArrayList<String> exerciseVisibleTypes = new ArrayList<>();
    @NonNull private static ArrayList<String> routeVisibleTypes = new ArrayList<>();
    @NonNull private static ArrayList<String> distanceVisibleTypes = new ArrayList<>();

    // sorting (using AppConsts.Layout.ordinal() as index)
    private static int[] sorterSelectedIndices = { 0, 0, 0, 0, 0, 0, 0 };
    private static boolean[] sorterSelectedInversions = { false, false, false, false, false, false, false, };

    // Strava API
    private static String authCode = "";
    private static String refreshToken = "";
    private static String accessToken = "";
    private static LocalDateTime accessTokenExpiration = LocalDateTime.MIN;
    private static String defaultRecordingMethod = "GPS";
    private static String defaultDevice = "";
    private static SwitchChain pullPolicy = new SwitchChain(
        new SwitchItem(StravaApi.JSON_EXTERNAL_ID, "External id (e.g. Garmin)", true),
        new SwitchItem(StravaApi.JSON_NAME, "Name (as route)", true),
        new SwitchItem(StravaApi.JSON_DESCRIPTION, "Description (as note)", true),
        new SwitchItem(StravaApi.JSON_DISTANCE, "Distance", true),
        new SwitchItem(StravaApi.JSON_TIME, "Elapsed time", true),
        new SwitchItem(StravaApi.JSON_TYPE, "Type", true),
        new SwitchItem(StravaApi.JSON_DATE, "Datetime", true),
        new SwitchItem(StravaApi.JSON_MAP, "Map", true),
        new SwitchItem(StravaApi.JSON_DEVICE, "Device", false)
    );

    //

    /**
     * Sets up Shared Preferences and loads previously saved fields.
     * If first time loading, this also saves default values to Shared Preferences.
     *
     * <p>Must be called when initializing application.</p>
     *
     * @param c Context
     */
    public static void setUpAndLoad(Context c) {
        sp = c.getSharedPreferences(NAME_SHARED_PREFERENCES, MODE_PRIVATE);
        editor = sp.edit();
        appVersion = c.getString(R.string.app_version);
        load(c);
    }

    @Deprecated
    private static void save() {
        // app
        savePref(appVersion, KEY_APP_VERSION);
        savePref(developer, KEY_DEVELOPER);
        savePref(firstLogin, KEY_FIRST_LOGIN);

        // display
        savePref(showWeekHeaders, KEY_WEEK_HEADERS);
        savePref(showDailyChart, KEY_DAILY_CHART);

        // look
        savePref(color, KEY_COLOR);
        savePref(theme, KEY_THEME);

        // profile
        savePref(mass, KEY_MASS);
        savePref(birthday, KEY_BIRTHDAY);

        // filtering
        savePref(showHiddenRoutes, KEY_SHOW_HIDDEN_ROUTES);
        savePref(hideSingletonRoutes, KEY_HIDE_SINGLETON_ROUTES);
        savePref(includeLonger, KEY_INCLCUDE_LONGER);
        savePref(distanceLowerLimit, KEY_LIMIT_LOWER);
        savePref(distanceUpperLimit, KEY_LIMIT_UPPER);
        savePref(exerciseVisibleTypes, KEY_TYPES_EXERCISE);
        savePref(routeVisibleTypes, KEY_TYPES_ROUTE);
        savePref(distanceVisibleTypes, KEY_TYPES_DISTANCE);

        // sorting
        savePref(sorterSelectedIndices, KEY_SORT_SELECTED_INDICES);
        savePref(sorterSelectedInversions, KEY_SORT_SELECTED_INVERSIONS);

        // strava
        savePref(authCode, KEY_STRAVA_AUTH);
        savePref(refreshToken, KEY_STRAVA_REFRESH);
        savePref(accessToken, KEY_STRAVA_ACCESS);
        savePref(accessTokenExpiration, KEY_STRAVA_ACCESS_EXP_DATE);
        savePref(defaultDevice, KEY_STRAVA_DEVICE);
        savePref(defaultRecordingMethod, KEY_STRAVA_METHOD);
        savePref(pullPolicy.getChecked(), KEY_STRAVA_PULL_POLICY);
    }

    private static void load(Context c) {
        // type tokens used more than once
        TypeToken<Boolean> bool = new TypeToken<Boolean>(){};
        TypeToken<String> str = new TypeToken<String>(){};
        TypeToken<Integer> in = new TypeToken<Integer>(){};
        TypeToken<ArrayList<String>> strList = new TypeToken<ArrayList<String>>(){};
        TypeToken<boolean[]> boolArr = new TypeToken<boolean[]>(){};

        // version
        appVersion = loadPref(str, KEY_APP_VERSION);
        String targetVersion = c.getString(R.string.app_version);
        if (!appVersion.equals(targetVersion)) {
            // do upgrades here; resolve conflicts or save new default values
            appVersion = targetVersion;
            savePref(appVersion, KEY_APP_VERSION);
        }

        // app
        developer = loadPref(bool, KEY_DEVELOPER);
        firstLogin = loadPref(bool, KEY_FIRST_LOGIN);

        // display
        showWeekHeaders = loadPref(bool, KEY_WEEK_HEADERS);
        showDailyChart = loadPref(bool, KEY_DAILY_CHART);

        // look
        color = loadPref(new TypeToken<Integer>(){}, KEY_COLOR);
        theme = loadPref(bool, KEY_THEME);

        // profile
        mass = loadPref(new TypeToken<Float>(){}, KEY_MASS);
        birthday = loadPref(new TypeToken<LocalDate>(){}, KEY_BIRTHDAY);

        // filtering
        showHiddenRoutes = loadPref(bool, KEY_SHOW_HIDDEN_ROUTES);
        hideSingletonRoutes = loadPref(bool, KEY_HIDE_SINGLETON_ROUTES);
        includeLonger = loadPref(bool, KEY_INCLCUDE_LONGER);
        distanceLowerLimit = loadPref(in, KEY_LIMIT_LOWER);
        distanceUpperLimit = loadPref(in, KEY_LIMIT_UPPER);
        exerciseVisibleTypes = loadPref(strList, KEY_TYPES_EXERCISE);
        routeVisibleTypes = loadPref(strList, KEY_TYPES_ROUTE);
        distanceVisibleTypes = loadPref(strList, KEY_TYPES_DISTANCE);

        // sorting
        sorterSelectedIndices = loadPref(new TypeToken<int[]>(){}, KEY_SORT_SELECTED_INDICES);
        sorterSelectedInversions = loadPref(boolArr, KEY_SORT_SELECTED_INVERSIONS);

        // strava
        authCode = loadPref(str, KEY_STRAVA_AUTH);
        refreshToken = loadPref(str, KEY_STRAVA_REFRESH);
        accessToken = loadPref(str, KEY_STRAVA_ACCESS);
        accessTokenExpiration = loadPref(new TypeToken<LocalDateTime>(){}, KEY_STRAVA_ACCESS_EXP_DATE);
        defaultDevice = loadPref(str, KEY_STRAVA_DEVICE);
        defaultRecordingMethod = loadPref(str, KEY_STRAVA_METHOD);
        pullPolicy.setChecked(loadPref(boolArr, KEY_STRAVA_PULL_POLICY));
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
            case KEY_APP_VERSION: return appVersion;
            case KEY_DEVELOPER: return developer;
            case KEY_FIRST_LOGIN: return firstLogin;
            case KEY_WEEK_HEADERS: return showWeekHeaders;
            case KEY_DAILY_CHART: return showDailyChart;
            case KEY_COLOR: return color;
            case KEY_THEME: return theme;
            case KEY_MASS: return mass;
            case KEY_BIRTHDAY: return birthday;
            case KEY_SHOW_HIDDEN_ROUTES: return showHiddenRoutes;
            case KEY_HIDE_SINGLETON_ROUTES: return hideSingletonRoutes;
            case KEY_INCLCUDE_LONGER: return includeLonger;
            case KEY_LIMIT_LOWER: return distanceLowerLimit;
            case KEY_LIMIT_UPPER: return distanceUpperLimit;
            case KEY_TYPES_EXERCISE: return exerciseVisibleTypes;
            case KEY_TYPES_ROUTE: return routeVisibleTypes;
            case KEY_TYPES_DISTANCE: return distanceVisibleTypes;
            case KEY_SORT_SELECTED_INDICES: return sorterSelectedIndices;
            case KEY_SORT_SELECTED_INVERSIONS: return sorterSelectedInversions;
            case KEY_STRAVA_AUTH: return authCode;
            case KEY_STRAVA_REFRESH: return refreshToken;
            case KEY_STRAVA_ACCESS: return accessToken;
            case KEY_STRAVA_ACCESS_EXP_DATE: return accessTokenExpiration;
            case KEY_STRAVA_DEVICE: return defaultDevice;
            case KEY_STRAVA_METHOD: return defaultRecordingMethod;
            case KEY_STRAVA_PULL_POLICY: return pullPolicy.getChecked();
            default: return new Object();
        }
    }

    // set

    @Debug
    public static void setDeveloper(boolean developer) {
        Prefs.developer = developer;
        savePref(developer, KEY_DEVELOPER);
    }

    public static void setFirstLogin(boolean firstLogin) {
        Prefs.firstLogin = firstLogin;
        savePref(firstLogin, KEY_FIRST_LOGIN);
    }

    public static void showWeekHeaders(boolean show) {
        showWeekHeaders = show;
        savePref(show, KEY_WEEK_HEADERS);
    }

    public static void showDailyChart(boolean show) {
        showDailyChart = show;
        savePref(show, KEY_DAILY_CHART);
    }

    public static void setColor(int colorConst) {
        color = colorConst;
        savePref(colorConst, KEY_COLOR);
    }

    public static void setTheme(boolean light) {
        theme = light;
        savePref(light, KEY_THEME);
    }

    public static void setMass(float kilos) {
        mass = kilos;
        savePref(kilos, KEY_MASS);
    }

    public static void setBirthday(LocalDate date) {
        birthday = date;
        savePref(date, KEY_BIRTHDAY);
    }

    public static void showHiddenRoutes(boolean show) {
        showHiddenRoutes = show;
        savePref(show, KEY_SHOW_HIDDEN_ROUTES);
    }

    public static void hideSingletonRoutes(boolean hide) {
        hideSingletonRoutes = hide;
        savePref(hide, KEY_HIDE_SINGLETON_ROUTES);
    }

    @Unfinished
    public static void includeLonger(boolean include) {
        includeLonger = include;
        savePref(include, KEY_INCLCUDE_LONGER);
    }

    @Unfinished
    public static void setDistanceLowerLimit(int distanceLowerLimit) {
        Prefs.distanceLowerLimit = distanceLowerLimit;
        savePref(distanceLowerLimit, KEY_LIMIT_LOWER);
    }

    @Unfinished
    public static void setDistanceUpperLimit(int distanceUpperLimit) {
        Prefs.distanceUpperLimit = distanceUpperLimit;
        savePref(distanceUpperLimit, KEY_LIMIT_UPPER);
    }

    public static void setExerciseVisibleTypes(@NonNull ArrayList<String> types) {
        exerciseVisibleTypes = types;
        savePref(types, KEY_TYPES_EXERCISE);
    }

    public static void setRouteVisibleTypes(@NonNull ArrayList<String> types) {
        routeVisibleTypes = types;
        savePref(types, KEY_TYPES_ROUTE);
    }

    public static void setDistanceVisibleTypes(@NonNull ArrayList<String> types) {
        distanceVisibleTypes = types;
        savePref(types, KEY_TYPES_DISTANCE);
    }

    public static void setSorter(AppConsts.Layout layout, int selectedIndex, boolean orderInverted) {
        sorterSelectedIndices[layout.ordinal()] = selectedIndex;
        sorterSelectedInversions[layout.ordinal()] = orderInverted;
        savePref(sorterSelectedIndices, KEY_SORT_SELECTED_INDICES);
        savePref(sorterSelectedInversions, KEY_SORT_SELECTED_INVERSIONS);
    }

    public static void setAuthCode(String authCode) {
        Prefs.authCode = authCode;
        savePref(authCode, KEY_STRAVA_AUTH);
    }

    public static void setRefreshToken(String refreshToken) {
        Prefs.refreshToken = refreshToken;
        savePref(refreshToken, KEY_STRAVA_REFRESH);
    }

    public static void setAccessToken(String accessToken) {
        Prefs.accessToken = accessToken;
        savePref(accessToken, KEY_STRAVA_ACCESS);
    }

    public static void setAccessTokenExpiration(LocalDateTime accessTokenExpiration) {
        Prefs.accessTokenExpiration = accessTokenExpiration;
        savePref(accessTokenExpiration, KEY_STRAVA_ACCESS_EXP_DATE);
    }

    public static void setDefaultDevice(String defaultDevice) {
        Prefs.defaultDevice = defaultDevice;
        savePref(defaultDevice, KEY_STRAVA_DEVICE);
    }

    public static void setDefaultRecordingMethod(String defaultRecordingMethod) {
        Prefs.defaultRecordingMethod = defaultRecordingMethod;
        savePref(defaultRecordingMethod, KEY_STRAVA_METHOD);
    }

    public static void setPullSettings(boolean[] checked) {
        Prefs.pullPolicy.setChecked(checked);
        savePref(pullPolicy.getChecked(), KEY_STRAVA_PULL_POLICY);
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

    @Unfinished
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
    public static ArrayList<String> getExerciseVisibleTypes() {
        return exerciseVisibleTypes;
    }

    @NonNull
    public static ArrayList<String> getRouteVisibleTypes() {
        return routeVisibleTypes;
    }

    @NonNull
    public static ArrayList<String> getDistanceVisibleTypes() {
        return distanceVisibleTypes;
    }

    public static int getSorterIndex(AppConsts.Layout layout) {
        return sorterSelectedIndices[layout.ordinal()];
    }

    public static boolean getSorterInversion(AppConsts.Layout layout) {
        return sorterSelectedInversions[layout.ordinal()];
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

    public static String getDefaultDevice() {
        return defaultDevice;
    }

    public static String getDefaultRecordingMethod() {
        return defaultRecordingMethod;
    }

    public static SwitchChain getPullPolicy() {
        return pullPolicy;
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

    public static MapStyleOptions getMapStyle(Context c) {
        return new MapStyleOptions(c.getResources().getString(theme ? R.string.mapstyle_retro_json : R.string.mapstyle_mono_json));// C.MAP_STYLES[M.heaviside(theme)]));
    }

}
