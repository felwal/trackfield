package me.felwal.trackfield.data.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import me.felwal.trackfield.BuildConfig;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.map.model.Trail;
import me.felwal.trackfield.utils.AppConsts;
import me.felwal.trackfield.utils.AppLog;
import me.felwal.trackfield.utils.DateUtils;
import me.felwal.trackfield.utils.LayoutUtils;
import me.felwal.trackfield.utils.annotation.Debug;
import me.felwal.trackfield.utils.model.SwitchChain;

public class StravaService {

    // token response json keys
    private static final String JSON_ACCESS_TOKEN = "access_token";
    private static final String JSON_REFRESH_TOKEN = "refresh_token";
    private static final String JSON_EXPIRES_AT = "expires_at";

    // activity response json keys
    public static final String JSON_ID = "id";
    public static final String JSON_EXTERNAL_ID = "external_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_DESCRIPTION = "description";
    public static final String JSON_NOTE = "private_note";
    public static final String JSON_DISTANCE = "distance";
    public static final String JSON_TIME = "elapsed_time";
    public static final String JSON_HEARTRATE_HAS = "has_heartrate";
    public static final String JSON_HEARTRATE_AVG = "average_heartrate";
    public static final String JSON_TYPE = "type";
    public static final String JSON_DATE = "start_date_local";
    public static final String JSON_MAP = "map";
    public static final String JSON_POLYLINE = "summary_polyline";
    public static final String JSON_START = "start_latlng";
    public static final String JSON_END = "end_latlng";
    public static final String JSON_DEVICE = "device_name";
    public static final String JSON_COMMUTE = "commute";

    // api values
    private static final String CLIENT_ID = BuildConfig.STRAVA_CLIENT_ID;
    private static final String CLIENT_SECRET = BuildConfig.STRAVA_CLIENT_SECRET;
    private static final String REDIRECT_URI = "https://felwal.github.io/callback";
    private static final int PER_PAGE = 200; // max = 200

    private static final DateTimeFormatter FORMATTER_STRAVA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static RequestQueue queue;

    private final Activity a;

    //

    public StravaService(Activity a) {
        this.a = a;
        queue = Volley.newRequestQueue(a);
    }

    // authorize

    public void authorizeStrava() {
        Uri uri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all")
            .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        a.startActivity(intent);
    }

    /**
     * Handles intent for strava authorization result; checks if intent contains any URI appLinkData and finishes
     * authorization.
     *
     * @param appLinkIntent The intent possibly containing appLinkData
     */
    public void handleIntent(Intent appLinkIntent) {
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) finishAuthorization(appLinkData);
    }

    private void finishAuthorization(Uri appLinkData) {
        String authCode = appLinkData.getQueryParameter("code");
        if (authCode != null) {
            Prefs.setAuthCode(authCode);
            LayoutUtils.toast(R.string.toast_strava_auth_successful, a);
        }
        else {
            LayoutUtils.toast(R.string.toast_strava_auth_err, a);
        }
    }

    // pull activities

    public void pullActivity(final long stravaId, SwitchChain options, ResponseListener listener) {
        ((TokenRequester) accessToken -> {

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getActivityURL(stravaId), null,
                response -> {
                AppLog.i("response: " + response.toString());

                boolean success = handlePull(convertToExercise(response), options);

                listener.onStravaResponse(success);
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void pullActivities(final int page, SwitchChain options, MultiResponseListener listener) {
        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_pull_activities, a);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(page), null,
                response -> {
                AppLog.i("response: " + response);

                int successCount = 0;
                int errorCount = 0;

                for (int index = 0; index < response.length(); index++) {
                    boolean success;
                    try {
                        JSONObject obj = response.getJSONObject(index);
                        Exercise strava = convertToExercise(obj);

                        // only pull existing activities
                        if (!DbReader.get(a).doesStravaIdExist(strava.getStravaId())) continue;

                        success = handlePull(strava, options);
                    }
                    catch (JSONException e) {
                        success = false;
                        e.printStackTrace();
                    }

                    if (success) successCount++;
                    else errorCount++;
                }

                // pull next page
                if (response.length() == PER_PAGE) {
                    pullActivities(page + 1, options, listener);
                }

                listener.onStravaResponse(successCount, errorCount);
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    /**
     * Pulls every exercise with a stravaId, one at a time. As we have a limitation to request amounts, the user
     * should not be able to invoke this method. TODO: find a better way of mass-pulling
     */
    @Deprecated
    @Debug
    public void pullAllActivities(ResponseListener listener) {
        ArrayList<Long> stravaIds = DbReader.get(a).getStravaIds();
        if (stravaIds.size() == 0) return;

        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_pull_activities, a);

            for (long stravaId : stravaIds) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getActivityURL(stravaId), null,
                    response -> {
                    AppLog.i("response: " + response.toString());

                    boolean success = handlePull(convertToExercise(response), Prefs.getPullOptions());

                    listener.onStravaResponse(success);
                }, e -> listener.onStravaResponseError(e, a));

                queue.add(request);
            }
        }).requestAccessToken(a);
    }

    // request activities

    public void requestActivity(long stravaId, ResponseListener listener) {
        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_req_activity, a);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getActivityURL(stravaId), null,
                response -> {
                AppLog.i("response: " + response);

                boolean success = handleRequest(convertToExercise(response));

                listener.onStravaResponse(success);
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void requestActivity(final int index, ResponseListener listener) {
        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_req_activity, a);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(1), null,
                response -> {
                AppLog.i("response: " + response);

                try {
                    JSONObject obj = response.getJSONObject(index);
                    boolean success = handleRequest(convertToExercise(obj));

                    AppLog.i("response obj: " + obj.toString());

                    listener.onStravaResponse(success);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    LayoutUtils.handleError(R.string.toast_err_parse_jsonobj, e, a);
                }
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void requestActivities(final int page, MultiResponseListener listener) {
        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_req_activities, a);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(page), null,
                response -> {
                AppLog.i("response: " + response);

                int successCount = 0;
                int errorCount = 0;

                for (int index = 0; index < response.length(); index++) {
                    boolean success;
                    try {
                        JSONObject obj = response.getJSONObject(index);
                        Exercise strava = convertToExercise(obj);

                        // if stravaId already exists, continue to next; dont override
                        if (DbReader.get(a).doesStravaIdExist(strava.getStravaId())) continue;

                        success = handleRequest(strava);
                    }
                    catch (JSONException e) {
                        success = false;
                        e.printStackTrace();
                        //LayoutUtils.handleError(R.string.toast_err_parse_jsonobj, e, a);
                    }

                    if (success) successCount++;
                    else errorCount++;
                }

                // request next page
                if (response.length() == PER_PAGE) {
                    requestActivities(page + 1, listener);
                }

                listener.onStravaResponse(successCount, errorCount);
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void requestNewActivities(final int page, MultiResponseListener listener) {
        ((TokenRequester) accessToken -> {
            LayoutUtils.toast(R.string.toast_strava_req_activities, a);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(page), null,
                response -> {
                AppLog.i("response: " + response);

                int successCount = 0;
                int errorCount = 0;
                boolean loopBroken = false;

                for (int index = 0; index < response.length(); index++) {
                    boolean success;
                    try {
                        JSONObject obj = response.getJSONObject(index);
                        Exercise strava = convertToExercise(obj);

                        // if stravaId already exists, we are done; all new have been requested
                        if (DbReader.get(a).doesStravaIdExist(strava.getStravaId())) {
                                loopBroken = true;
                                break;
                            }

                        success = handleRequest(strava);
                    }
                    catch (JSONException e) {
                        success = false;
                        e.printStackTrace();
                        //LayoutUtils.handleError(R.string.toast_err_parse_jsonobj, e, a);
                    }

                    if (success) successCount++;
                    else errorCount++;
                }

                // request next page
                if (!loopBroken && response.length() == PER_PAGE) {
                    requestNewActivities(page + 1, listener);
                }

                listener.onStravaResponse(successCount, errorCount);
            }, e -> listener.onStravaResponseError(e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    // request/pull helper methods

    public void pullAllActivities(MultiResponseListener listener) {
        pullActivities(1, Prefs.getPullOptions(), listener);
    }

    public void requestLastActivity(ResponseListener listener) {
        requestActivity(0, listener);
    }

    @Deprecated
    public void requestLastActivities(int count, ResponseListener listener) {
        for (int i = 0; i < count; i++) {
            requestActivity(i, listener);
        }
    }

    public void requestAllActivities(MultiResponseListener listener) {
        requestActivities(1, listener);
    }

    public void requestNewActivities(MultiResponseListener listener) {
        requestNewActivities(1, listener);
    }

    // convert

    private Exercise convertToExercise(JSONObject obj) {
        if (obj == null) return null;

        try {
            AppLog.i("response: " + obj.toString());

            // keys which always exist
            long stravaId = obj.getLong(JSON_ID);
            String name = obj.getString(JSON_NAME);
            int distance = (int) obj.getDouble(JSON_DISTANCE);
            int time = obj.getInt(JSON_TIME);
            boolean hasHeartrate = obj.getBoolean(JSON_HEARTRATE_HAS);
            float avgHeartrate = hasHeartrate ? (float) obj.getDouble(JSON_HEARTRATE_AVG) : Exercise.HEARTRATE_NONE;
            String type = obj.getString(JSON_TYPE);
            String date = obj.getString(JSON_DATE);
            String label = obj.getBoolean(JSON_COMMUTE) ? "Commute" : "";

            // external id
            String externalId;
            long garminId = Exercise.ID_NONE;
            if (obj.has(JSON_EXTERNAL_ID) && !(externalId = obj.getString(JSON_EXTERNAL_ID)).equals("")) {
                if (externalId.contains("garmin")) {
                    // garmin_push_id has been changed to garmin_ping_id.
                    // however, the new id is something else; we can't link
                    // to the exercise in Garmin Connect anymore.
                    //garminId = Long.parseLong(externalId.substring(externalId.lastIndexOf("_") + 1));
                }
                else {
                    garminId = Exercise.ID_UNRELEVANT;
                }
            }

            // trail
            String polyline = null;
            LatLng start = null;
            LatLng end = null;
            try {
                JSONObject map = obj.getJSONObject(JSON_MAP);
                polyline = map.getString(JSON_POLYLINE);
                JSONArray startLatLng = obj.getJSONArray(JSON_START);
                JSONArray endLatLng = obj.getJSONArray(JSON_END);
                start = new LatLng(startLatLng.getDouble(0), startLatLng.getDouble(1));
                end = new LatLng(endLatLng.getDouble(0), endLatLng.getDouble(1));
            }
            catch (Exception e) {
                // no polyline or start or end, leave as null; do nothing
            }

            // only available in pull
            String description = obj.has(JSON_DESCRIPTION) ? obj.getString(JSON_DESCRIPTION) : "";
            String note = obj.has(JSON_NOTE) ? obj.getString(JSON_NOTE) : "";
            String device = obj.has(JSON_DEVICE) ? obj.getString(JSON_DEVICE) :
                polyline != null ? Prefs.getDefaultDevice() : "";

            // for some reason description now comes as "null" instead of not at all
            if (description.equals("null")) description = "";
            if (note.equals("null")) note = "";

            // merge public and private notes
            description = (description + "\n\n" + note).trim();

            // never available; use default
            String method = polyline != null ? Prefs.getDefaultRecordingMethod() : "";

            // convert
            int routeId = DbReader.get(a).getRouteIdOrCreate(name, a);
            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER_STRAVA);
            Trail trail = polyline == null || polyline.equals("null") || polyline.equals("") ? null :
                new Trail(polyline, start, end);

            return new Exercise(Exercise.ID_NONE, stravaId, garminId, type, label, dateTime, routeId, name, "", "",
                description, device, method, distance, time, avgHeartrate, trail, false);
        }
        catch (JSONException e) {
            e.printStackTrace();
            LayoutUtils.handleError("Failed parse JSONObject, returning null", e, a);
            return null;
        }
    }

    private boolean handlePull(Exercise strava, SwitchChain options) {
        if (strava == null) return false;
        boolean success;

        Exercise existing = DbReader.get(a).getExercise(strava.getStravaId());

        // import
        if (existing == null) {
            success = DbWriter.get(a).addExercise(strava, a);
            LayoutUtils.toast("Pull resulted in import on " + strava.getDate().format(AppConsts.FORMATTER_SQL_DATE), a);
            AppLog.i("Pull resulted in import on " + strava.getDate().format(AppConsts.FORMATTER_SQL_DATE));
        }

        // merge
        else {
            // set depending on pull/request options
            if (options.isChecked(JSON_EXTERNAL_ID)) {
                existing.setGarminId(strava.getGarminId());
            }
            if (options.isChecked(JSON_NAME)) {
                existing.setRoute(strava.getRoute());
                existing.setRouteId(strava.getRouteId());
            }
            if (options.isChecked(JSON_TYPE)) {
                existing.setType(strava.getType());
            }
            if (options.isChecked(JSON_COMMUTE)) {
                existing.setLabel(strava.getLabel());
            }
            if (options.isChecked(JSON_DATE)) {
                existing.setDateTime(strava.getDateTime());
            }
            if (options.isChecked(JSON_DEVICE) && !strava.getDevice().equals("")) {
                existing.setDevice(strava.getDevice());
            }
            if (options.isChecked(JSON_DISTANCE)) {
                existing.setDistance(strava.getDistance());
            }
            if (options.isChecked(JSON_TIME)) {
                existing.setTime(strava.getTime());
            }
            if (options.isChecked(JSON_HEARTRATE_AVG)) {
                existing.setAvgHeartrate(strava.getAvgHeartrate());
            }
            if (options.isChecked(JSON_DESCRIPTION) && !strava.getNote().equals("")) {
                // dont override note with nothing
                existing.setNote(strava.getNote());
            }
            if (options.isChecked(JSON_MAP)) {
                existing.setTrail(strava.getTrail());
            }

            success = DbWriter.get(a).updateExercise(existing, a);
        }

        if (a instanceof ExerciseDetailActivity) a.recreate();

        return success;
    }

    /**
     * Imports requested exercise to db or potentially merges with a matching one (same datetime).
     * If the stravaId already exists, the exercise is ignored and does NOT override the existing one.
     */
    private boolean handleRequest(Exercise strava) {
        if (strava == null) return false;
        boolean success = true;

        // dont override already existing (use pull for that)
        Exercise existing = DbReader.get(a).getExercise(strava.getStravaId());
        if (existing != null) return true;

        // merge with matching, i.e. not already linked to strava activity
        ArrayList<Exercise> matching = DbReader.get(a).getExercises(strava.getDateTime());

        // merge
        if (matching.size() == 1) {
            Exercise m = matching.get(0);
            Exercise merged = new Exercise(m.getId(), strava.getStravaId(), strava.getGarminId(), m.getType(),
                strava.getLabel(), strava.getDateTime(), m.getRouteId(), m.getRoute(), m.getRouteVar(), m.getInterval(),
                m.getNote(), m.getDevice(), m.getRecordingMethod(), strava.getDistance(), strava.getTime(),
                strava.getAvgHeartrate(), strava.getTrail(), m.isTrailHidden());

            success = DbWriter.get(a).updateExercise(merged, a);
        }

        // import
        else if (matching.size() == 0) {
            success &= DbWriter.get(a).addExercise(strava, a);
            AppLog.i("Import on " + strava.getDate().format(AppConsts.FORMATTER_SQL_DATE));
            //L.toast("Import on " + fromStrava.getDate().format(C.FORMATTER_SQL_DATE), a);
        }

        // nothing
        else {
            success = false;
            AppLog.i("Multiple choice on " + strava.getDateTime().format(AppConsts.FORMATTER_SQL_DATE));
            //L.toast("Multiple choice on " + fromStrava.getDateTime().format(C.FORMATTER_SQL_DATE), a);
        }

        if (a instanceof MainActivity) ((MainActivity) a).updateFragment();

        // also pull to get data not available to request
        // but only if the user wants data not available to request
        SwitchChain options = Prefs.getRequestOptions();
        if (options.isChecked(JSON_DEVICE) || options.isChecked(JSON_DESCRIPTION)) {
            pullActivity(strava.getStravaId(), options, responseSuccess -> {});
        }

        return success;
    }

    // get url:s

    private static String getRefreshTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
            "&code=" + Prefs.getAuthCode() + "&grant_type=authorization_code";
    }

    private static String getAccessTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
            "&refresh_token=" + Prefs.getRefreshToken() + "&grant_type=refresh_token";
    }

    private static String getActivitiesURL(int page) {
        return "https://www.strava.com/api/v3/athlete/activities?per_page=" + PER_PAGE + "&access_token=" +
            Prefs.getAccessToken() + "&page=" + page;
    }

    private static String getActivityURL(long id) {
        return "https://www.strava.com/api/v3/activities/" + id + "?include_all_efforts=false" + "&access_token=" +
            Prefs.getAccessToken();
    }

    // launch

    public static void launchStravaActivity(long stravaId, Activity a) {
        Uri uri = Uri.parse("https://www.strava.com/activities/" + stravaId)
            .buildUpon()
            .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        a.startActivity(intent);
    }

    public static void launchGarminActivity(long garminId, Activity a) {
        Uri uri = Uri.parse("https://connect.garmin.com/modern/activity/" + garminId)
            .buildUpon()
            .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        a.startActivity(intent);
    }

    // tools

    public static void toastPullResults(int successCount, int errorCount, Context c) {
        if (successCount == 0 && errorCount == 0) {
            LayoutUtils.toast(R.string.toast_strava_pull_activities_none, c);
        }
        else {
            if (successCount != 0) {
                LayoutUtils.toast(R.plurals.toast_strava_pull_activities_successful, successCount, c);
            }
            if (errorCount != 0) {
                LayoutUtils.toast(R.plurals.toast_strava_pull_activities_err, errorCount, c);
            }
        }
    }

    public static void toastRequestResults(int successCount, int errorCount, Context c) {
        if (successCount == 0 && errorCount == 0) {
            LayoutUtils.toast(R.string.toast_strava_req_activities_none, c);
        }
        else {
            if (successCount != 0) {
                LayoutUtils.toast(R.plurals.toast_strava_req_activities_successful, successCount, c);
            }
            if (errorCount != 0) {
                LayoutUtils.toast(R.plurals.toast_strava_req_activities_err, errorCount, c);
            }
        }
    }

    // interface

    public interface ResponseListener {

        void onStravaResponse(boolean success);

        default void onStravaResponseError(Exception e, Context c) {
            LayoutUtils.handleError(R.string.toast_strava_response_err, e, c);
        }

    }

    public interface MultiResponseListener {

        void onStravaResponse(int successCount, int errorCount);

        default void onStravaResponseError(Exception e, Context c) {
            LayoutUtils.handleError(R.string.toast_strava_response_err, e, c);
        }

    }

    private interface TokenRequester {

        void onTokenReady(String token);

        default void requestAccessToken(Context c) {
            if (Prefs.isAccessTokenCurrent()) {
                onTokenReady(Prefs.getAccessToken());
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getAccessTokenURL(), null,
                response -> {
                    try {
                        Prefs.setAccessToken(response.getString(JSON_ACCESS_TOKEN));
                        Prefs.setAccessTokenExpiration(
                            DateUtils.ofEpochSecond(Integer.parseInt(response.getString(JSON_EXPIRES_AT))));

                        //L.toast("accessToken: " + Prefs.getAccessToken(), c);
                        AppLog.i("response accessToken: " + Prefs.getAccessToken());
                        onTokenReady(Prefs.getAccessToken());
                    }
                    catch (JSONException e) {
                        //e.printStackTrace();
                        LayoutUtils.handleError("Failed to parse accessToken from Strava", e, c);
                    }
                }, e -> {
                AppLog.i(c.getString(R.string.toast_strava_req_access_err) + ": " + e.getMessage());
                //LayoutUtils.handleError(R.string.toast_strava_req_access_err, e, c);

                // request refreshToken
                ((TokenRequester) refreshToken -> ((TokenRequester) this).requestAccessToken(c))
                    .requestRefreshToken(true, c);
            });

            queue.add(request);
        }

        default void requestRefreshToken(boolean requireRequest, Context c) {
            if (!requireRequest && Prefs.isRefreshTokenCurrent()) {
                onTokenReady(Prefs.getRefreshToken());
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getRefreshTokenURL(), null,
                response -> {
                    try {
                        Prefs.setRefreshToken(response.getString(JSON_REFRESH_TOKEN));

                        LayoutUtils.toast(R.string.toast_strava_req_refresh_successful, c);
                        AppLog.i("response refreshToken: " + Prefs.getRefreshToken());
                        onTokenReady(Prefs.getRefreshToken());
                    }
                    catch (JSONException e) {
                        //e.printStackTrace();
                        LayoutUtils.handleError("Failed to parse refreshToken", e, c);
                    }
                }, e -> LayoutUtils.handleError(R.string.toast_strava_req_refresh_err, e, c));

            queue.add(request);
        }

    }

}
