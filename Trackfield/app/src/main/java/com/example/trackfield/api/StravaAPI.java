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
    private static final int PER_PAGE = 200;

    // request codes
    public static final int REQUEST_CODE_PERMISSIONS_STRAVA = 2;

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
        L.toast("Authorization successful; authCode: " + Prefs.getAuthCode(), a);
    }

    public void handleIntent(Intent appLinkIntent) {
        //String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) finishAuthorization(appLinkData);
    }

    // request activities

    private void requestActivity(final int index) {
        ((TokenRequester) accessToken -> {

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(1), null,
                    response -> {
                        try {
                            JSONObject obj = response.getJSONObject(index);
                            mergeWithExisting(convertToExercise(obj));

                            Log.i("Strava API", "response: " + obj.toString());
                            //L.toast("response: " + obj.toString(), a);
                            L.toast("request successful", a);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError("failed to parse JSONObject", e, a);
                        }
                    },
                    e -> L.handleError("Strava response error", e, a));

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
                                L.handleError("failed to parse JSONObject", e, a);
                            }
                        }
                        if (response.length() == PER_PAGE) requestActivities(page + 1);

                        L.toast("request successful", a);
                    },
                    e -> L.handleError("Strava response error", e, a));

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
            String name = obj.getString("name");
            int distance = (int) obj.getDouble("distance");
            int time = obj.getInt("elapsed_time");
            String stravaType = obj.getString("type");
            String date = obj.getString("start_date_local");

            JSONObject map = obj.getJSONObject("map");
            String polyline = map.getString("summary_polyline");
            LatLng start = null;
            LatLng end = null;
            try {
                JSONArray startLatLng = obj.getJSONArray("start_latlng");
                JSONArray endLatLng = obj.getJSONArray("end_latlng");
                start = new LatLng(startLatLng.getDouble(0), startLatLng.getDouble(1));
                end = new LatLng(endLatLng.getDouble(0), endLatLng.getDouble(1));
            }
            catch (Exception e) {
            }

            // convert
            int type = Exercise.typeFromStravaType(stravaType);
            int routeId = Reader.get(a).getRouteIdOrCreate(name, a);
            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER_STRAVA);
            Trail trail = polyline == null || polyline.equals("null") || polyline.equals("") ? null : new Trail(polyline, start, end);

            return new Exercise(-1, type, dateTime, routeId, name, "", "", "", "Garmin Forerunner 745", "GPS + Galileo", distance, time, null, trail);
        }
        catch (JSONException e) {
            e.printStackTrace();
            L.handleError("failed to convert jsonobj to exercise, returning null", e, a);
            return null;
        }
    }

    private void mergeWithExisting(Exercise fromStrava) {
        if (fromStrava == null) return;

        ArrayList<Exercise> matching = Reader.get(a).getExercisesForMerge(fromStrava.getDateTime(), fromStrava.getType());

        if (matching.size() == 1) {
            Exercise m = matching.get(0);
            Exercise merged = new Exercise(m.get_id(), m.getType(), fromStrava.getDateTime(), m.getRouteId(), m.getRoute(), m.getRouteVar(), m.getInterval(),
                    m.getNote(), m.getDataSource(), m.getRecordingMethod(), fromStrava.getDistancePrimary(), fromStrava.getTimePrimary(), m.getSubs(), fromStrava.getTrail());

            Writer.get(a).updateExercise(merged);
        }
        else if (matching.size() == 0) {
            Writer.get(a).addExercise(fromStrava, a);
            //L.toast("import on " + dateTime.toLocalDate().format(C.FORMATTER_SQL_DATE), a);
        }
        else
            L.toast("multiple choice on " + fromStrava.getDateTime().format(C.FORMATTER_SQL_DATE), a);
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
            if (!requireRequest && Prefs.isRefreshTokenCurrent())
                onTokenReady(Prefs.getRefreshToken());

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
