package com.example.trackfield.api;

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
import com.example.trackfield.R;
import com.example.trackfield.database.Reader;
import com.example.trackfield.database.Writer;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class StravaApi {

    private Activity a;

    private static RequestQueue queue;
    private final DateTimeFormatter FORMATTER_STRAVA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 2020-11-04T11:16:08Z

    private static final String CLIENT_ID = "***REMOVED***";
    private static final String CLIENT_SECRET = "***REMOVED***";
    private static final String REDIRECT_URI = "https://felwal.github.io/Trackfield_web/callback";
    private static final int PER_PAGE = 200; // max = 200

    // request codes
    public static final int REQUEST_CODE_PERMISSIONS_STRAVA = 2;

    // json
    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_TIME = "elapsed_time";
    private static final String JSON_TYPE = "type";
    private static final String JSON_DATE = "start_date_local";
    private static final String JSON_MAP = "map";
    private static final String JSON_POLYLINE = "summary_polyline";
    private static final String JSON_START = "start_latlng";
    private static final String JSON_END = "end_latlng";

    ////

    public StravaApi(Activity a) {
        this.a = a;
    }

    public void connectStrava() {
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

    private void finishAuthorization(Uri appLinkData) {
        Prefs.setAuthCode(appLinkData.getQueryParameter("code"));
        L.toast(a.getString(R.string.toast_api_auth_successful), a);
        //L.toast("Authorization successful; authCode: " + Prefs.getAuthCode(), a);
    }

    public void handleIntent(Intent appLinkIntent) {
        //String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) finishAuthorization(appLinkData);
    }

    // request activities

    public void requestActivity(final long id) {
        ((TokenRequester) accessToken -> {

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getActivityURL(id), null,
                    response -> {
                        updateExistingManually(convertToExercise(response));

                        Log.i("Strava", "response: " + response.toString());
                    },
                    e -> L.handleError(a.getString(R.string.toast_err_strava_response), e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void requestActivity(final int index) {
        ((TokenRequester) accessToken -> {

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(1), null,
                    response -> {
                        try {
                            JSONObject obj = response.getJSONObject(index);
                            mergeWithExisting(convertToExercise(obj));

                            Log.i("Strava API", "response: " + obj.toString());
                            //L.toast("response: " + obj.toString(), a);
                            L.toast(a.getString(R.string.toast_api_request_successful), a);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError(a.getString(R.string.toast_err_parse_jsonobj), e, a);
                        }
                    },
                    e -> L.handleError(a.getString(R.string.toast_err_strava_response), e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    private void requestActivities(final int page) {
        ((TokenRequester) accessToken -> {
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(page), null,
                    response -> {
                        for (int index = 0; index < response.length(); index++) {
                            try {
                                JSONObject obj = response.getJSONObject(index);
                                mergeWithExisting(convertToExercise(obj));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                L.handleError(a.getString(R.string.toast_err_parse_jsonobj), e, a);
                            }
                        }
                        if (response.length() == PER_PAGE) requestActivities(page + 1);

                        L.toast(a.getString(R.string.toast_api_request_successful), a);
                    },
                    e -> L.handleError(a.getString(R.string.toast_err_strava_response), e, a));

            queue.add(request);
        }).requestAccessToken(a);
    }

    public void requestLastActivity() {
        requestActivity(0);
    }

    public void requestLastActivities(int count) {
        for (int i = 0; i < count; i++) {
            requestActivity(i);
        }
    }

    public void requestAllActivities() {
        requestActivities(1);
    }

    // convert

    private Exercise convertToExercise(JSONObject obj) {
        if (obj == null) return null;

        try {
            Log.i("response", obj.toString());
            long stravaId = obj.getLong(JSON_ID);
            String name = obj.getString(JSON_NAME);
            int distance = (int) obj.getDouble(JSON_DISTANCE);
            int time = obj.getInt(JSON_TIME);
            String stravaType = obj.getString(JSON_TYPE);
            String date = obj.getString(JSON_DATE);

            JSONObject map = obj.getJSONObject(JSON_MAP);
            String polyline = map.getString(JSON_POLYLINE);
            LatLng start = null;
            LatLng end = null;
            try {
                JSONArray startLatLng = obj.getJSONArray(JSON_START);
                JSONArray endLatLng = obj.getJSONArray(JSON_END);
                start = new LatLng(startLatLng.getDouble(0), startLatLng.getDouble(1));
                end = new LatLng(endLatLng.getDouble(0), endLatLng.getDouble(1));
            }
            catch (Exception e) {
            }

            // TODO: import
            String device = "Garmin Forerunner 745";
            String method = "GPS + Galileo";

            // convert
            int type = Exercise.typeFromStravaType(stravaType);
            int routeId = Reader.get(a).getRouteIdOrCreate(name, a);
            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER_STRAVA);
            Trail trail = polyline == null || polyline.equals("null") || polyline.equals("") ? null : new Trail(polyline, start, end);

            return new Exercise(-1, stravaId, type, dateTime, routeId, name, "", "", "",
                    device, method, distance, time, null, trail);
        }
        catch (JSONException e) {
            e.printStackTrace();
            L.handleError("Failed to convert jsonobj to exercise, returning null", e, a);
            return null;
        }
    }

    private void updateExistingManually(Exercise strava) {
        if (strava == null) return;

        Exercise existing = Reader.get(a).getExercise(strava.getExternalId());
        if (existing == null) {
            Writer.get(a).addExercise(strava, a);
            L.toast("Manual update resulted in import on " + strava.getDate().format(C.FORMATTER_SQL_DATE), a);
            Log.i("Strava", "Manual update resulted in import on " + strava.getDate().format(C.FORMATTER_SQL_DATE));
        }
        else {
            existing.updateWithStravaActivity(strava);
            Writer.get(a).updateExercise(existing, a);
            L.toast("Exercise updated manually", a);
        }
    }

    private void mergeWithExisting(Exercise strava) {
        if (strava == null) return;

        ArrayList<Exercise> matching = Reader.get(a).getExercisesForMerge(strava.getDateTime(), strava.getType());

        if (matching.size() == 1) {
            Exercise x = matching.get(0);
            Exercise merged = new Exercise(x.get_id(), strava.getExternalId(), x.getType(), strava.getDateTime(), x.getRouteId(), x.getRoute(), x.getRouteVar(), x.getInterval(),
                    x.getNote(), x.getDataSource(), x.getRecordingMethod(), strava.getDistancePrimary(), strava.getTimePrimary(), x.getSubs(), strava.getTrail());
            //x.setExternalId(fromStrava.getExternalId());
            //x.setDateTime(fromStrava.getDateTime());

            Writer.get(a).updateExercise(merged, a);
        }
        else if (matching.size() == 0) {
            Writer.get(a).addExercise(strava, a);
            Log.i("Strava", "Import on " + strava.getDate().format(C.FORMATTER_SQL_DATE));
            //L.toast("Import on " + fromStrava.getDate().format(C.FORMATTER_SQL_DATE), a);
        }
        else {
            Log.i("Strava", "Multiple choice on " + strava.getDateTime().format(C.FORMATTER_SQL_DATE));
            //L.toast("Multiple choice on " + fromStrava.getDateTime().format(C.FORMATTER_SQL_DATE), a);
        }
    }

    // get url:s

    private static String getRefreshTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + Prefs.getAuthCode() + "&grant_type=authorization_code";
    }

    private static String getAccessTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&refresh_token=" + Prefs.getRefreshToken() + "&grant_type=refresh_token";
    }

    private static String getActivitiesURL(int page) {
        return "https://www.strava.com/api/v3/athlete/activities?per_page=" + PER_PAGE + "&access_token=" + Prefs.getAccessToken() + "&page=" + page;
    }

    private static String getActivityURL(long id) {
        return "https://www.strava.com/api/v3/activities/" + id + "?include_all_efforts=false" + "&access_token=" + Prefs.getAccessToken();
    }

    // launch

    public static void launchActivity(long stravaId, Activity a) {
        Uri uri = Uri.parse("https://www.strava.com/activities/" + stravaId)
                .buildUpon().build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        a.startActivity(intent);
    }

    // interface

    interface TokenRequester {

        void onTokenReady(String token);

        default void requestAccessToken(Context c) {
            if (Prefs.isAccessTokenCurrent()) {
                onTokenReady(Prefs.getAccessToken());
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getAccessTokenURL(), null,
                    response -> {
                        try {
                            Prefs.setAccessToken(response.getString("access_token"));
                            Prefs.setAccessTokenExpiration(M.ofEpoch(Integer.parseInt(response.getString("expires_at"))));

                            //L.toast("accessToken: " + Prefs.getAccessToken(), c);
                            Log.i("response accessToken: ", Prefs.getAccessToken());
                            onTokenReady(Prefs.getAccessToken());
                        }
                        catch (JSONException e) {
                            //e.printStackTrace();
                            L.handleError("failed to parse accessToken from Strava", e, c);
                        }
                    },
                    e -> {
                        L.handleError("failed to request new accessToken, requesting new refreshToken...", e, c);

                        // request refreshToken
                        ((TokenRequester) refreshToken -> ((TokenRequester) this).requestAccessToken(c)).requestRefreshToken(true, c);
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
                            Prefs.setRefreshToken(response.getString("refresh_token"));

                            //L.toast("refreshToken: " + Prefs.getRefreshToken(), c);
                            Log.i("response refreshToken: ", Prefs.getRefreshToken());
                            onTokenReady(Prefs.getRefreshToken());
                        }
                        catch (JSONException e) {
                            //e.printStackTrace();
                            L.handleError("failed to parse refreshToken", e, c);
                        }
                    },
                    e -> L.handleError("failed to request refreshToken, please redo authorizaition", e, c)); // TODO: auto-prompt

            queue.add(request);
        }

    }

}