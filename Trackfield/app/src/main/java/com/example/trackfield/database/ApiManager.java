package com.example.trackfield.database;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public abstract class ApiManager extends AppCompatActivity {

    private Activity a;

    // google
    private static FitnessOptions fitnessOptions;
    private SimpleDateFormat formatterStart = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.ENGLISH);
    private SimpleDateFormat formatterEnd = new SimpleDateFormat("hh:mm", Locale.ENGLISH);

    // strava
    private RequestQueue queue;
    private final DateTimeFormatter FORMATTER_STRAVA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 2020-11-04T11:16:08Z

    private static final String CLIENT_ID = "***REMOVED***";
    private static final String CLIENT_SECRET = "***REMOVED***";
    private static final String REDIRECT_URI = "http://localhost/callback";
    private static int PER_PAGE = 200;

    private static String authCode = "***REMOVED***";
    private static String refreshToken = "***REMOVED***";
    private static String accessToken = "***REMOVED***";
    private static LocalDateTime accessTokenExpiresAt;

    // request codes
    private static final int REQUEST_CODE_PERMISSIONS_GOOGLE_FIT = 1;
    private static final int REQUEST_CODE_PERMISSIONS_STRAVA = 2;

    ////

    protected void connectAPIs() {

        a = this;
        queue = Volley.newRequestQueue(this);

        // Google Fit
        /*fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
        if (hasPermissionsElseRequest()) accessGoogleFit();*/

        // Strava
        //accessStrava();

    }

    // authorization flow result
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PERMISSIONS_GOOGLE_FIT) accessGoogleFit();
            else if (requestCode == REQUEST_CODE_PERMISSIONS_STRAVA) authenticateStrava();
        }
    }

    //

    // Google Fit

    private void accessGoogleFit() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);

        long startTime = cal.getTimeInMillis();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readSessionsFromAllApps()
                //.read(DataType.TYPE_LOCATION_SAMPLE)
                //.aggregate(DataType.AGGREGATE_DISTANCE_DELTA)
                //.aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                //.bucketBySession(60, TimeUnit.SECONDS)
                .build();

        Fitness.getSessionsClient(this, account).readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override public void onSuccess(SessionReadResponse response) {
                        for (Session session : response.getSessions()) dumpSession(session);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        //L.toast("There was an error reading data from Google Fit:", a);
                        L.handleError(e, a);
                    }
                });

    }

    private void dumpSession(Session s) {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTimeInMillis(s.getStartTime(TimeUnit.MILLISECONDS));
        end.setTimeInMillis(s.getEndTime(TimeUnit.MILLISECONDS));



        L.toast(s.getName() + " (" + s.getActivity() + ")\n" + s.getAppPackageName() + "\n" +
                formatterStart.format(start.getTime()) + "-" + formatterEnd.format(end.getTime()),
                a);
    }
    private void dumpBucket(Bucket b) {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTimeInMillis(b.getStartTime(TimeUnit.MILLISECONDS));
        end.setTimeInMillis(b.getEndTime(TimeUnit.MILLISECONDS));

        String fieldStr = "";

        for (DataSet ds : b.getDataSets()) {
            for (DataPoint dp : ds.getDataPoints()) {
                for (Field field : dp.getDataType().getFields()) {

                    DateFormat dateFormat = getTimeInstance();

                    // fields
                    if (field.getName().equals("duration")) {
                        int minutes = dp.getValue(field).asInt() / 1000 / 60;
                        fieldStr += field.getName() + ": " + minutes + "min ";
                    }
                    else if (field.getName().equals("activity")) {
                        fieldStr += dp.getValue(field).asActivity() + " ";
                    }
                    else if (field.getName().equals("num_segments")) {
                        fieldStr += field.getName() + ": " + dp.getValue(field).asInt() + " ";
                    }
                    else {
                        fieldStr += field.getName() + ": " + dp.getValue(field) + " ";
                    }
                    L.toast(formatterStart.format(start.getTime()) + "-" + formatterEnd.format(end.getTime()) + " " + fieldStr, a);

                }
            }
        }

        L.toast(formatterStart.format(start.getTime()) + "-" + formatterEnd.format(end.getTime()) + " " + fieldStr, a);
    }

    private boolean hasPermissionsElseRequest() {
        // Check if the user has previously granted the necessary data access, and if not, initiate the authorization flow:

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) return true;
        GoogleSignIn.requestPermissions(this, REQUEST_CODE_PERMISSIONS_GOOGLE_FIT, account, fitnessOptions);
        return false;
    }

    //

    // Strava

    protected void authenticateStrava() {

        Uri uri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "auto")
                .appendQueryParameter("scope", "activity:read_all")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

        //authCode = "";
    }

    // request tokens
    private void requestAuthCode() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getAuthCodeURL(), null,
                new Response.Listener<JSONObject>() {
                    @Override public void onResponse(JSONObject response) {
                        try {
                            authCode = response.getString("code");
                            Log.i("response authCode: ", authCode);
                            L.toast(response.toString(),  a);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError("failed to get response element from Strava", e, a);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError e) {
                        L.handleError("Strava response error", e, a);
                    }
                });

        queue.add(request);
    }
    private void requestRefreshToken() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getRefreshTokenURL(), null,
                new Response.Listener<JSONObject>() {
                    @Override public void onResponse(JSONObject response) {
                        try {
                            refreshToken = response.getString("refresh_token");
                            Log.i("response refreshToken: ", refreshToken);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError("failed to get response element from Strava", e, a);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError e) {
                        L.handleError("Strava response error", e, a);
                    }
                });

        queue.add(request);
    }
    private void requestAccessToken() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getAccessTokenURL(), null,
                new Response.Listener<JSONObject>() {
                    @Override public void onResponse(JSONObject response) {
                        try {
                            accessToken = response.getString("access_token");
                            Log.i("response accessToken: ", accessToken);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError("failed to get response element from Strava", e, a);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError e) {
                        L.handleError("Strava response error", e, a);
                    }
                });

        queue.add(request);
    }

    // request activities
    protected void requestActivities(final int page) {

        requestAccessToken();
        //final ArrayList<Exercise> exercises = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(page), null,
                new Response.Listener<JSONArray>() {
                    @Override public void onResponse(JSONArray response) {
                        for (int index = 0; index < response.length(); index++) {
                            try {
                                JSONObject obj = response.getJSONObject(index);
                                mergeWithExisting(convertToExercise(obj));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                L.handleError("failed to get jsonobj from Strava", e, a);
                            }
                        }
                        if (response.length() == PER_PAGE) requestActivities(page + 1);
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError e) {
                        L.handleError("Strava response error", e, a);
                    }
                });

        queue.add(request);
    }
    protected void requestActivity(final int index) {

        requestAccessToken();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getActivitiesURL(1), null,
                new Response.Listener<JSONArray>() {
                    @Override public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(index);
                            mergeWithExisting(convertToExercise(obj));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            L.handleError("failed to get jsonobj from Strava", e, a);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError e) {
                        L.handleError("Strava response error", e, a);
                    }
                });

        queue.add(request);
    }
    protected void requestLastActivity() {
        requestActivity(0);
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
            catch (Exception e) {}

            // convert
            int type = Exercise.typeFromStravaType(stravaType);
            int routeId = Helper.getReader(a).getRouteIdOrCreate(name, a);
            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER_STRAVA);
            Trail trail = polyline == null || polyline.equals("null") || polyline.equals("") ? null : new Trail(polyline, start, end);

            return new Exercise(-1, -1, type, dateTime, routeId, name, "", "", "", "Strava", "GPS", distance, time, null, trail);
        }
        catch (JSONException e) {
            e.printStackTrace();
            L.handleError("failed to convert jsonobj to exercise, returning null", e, a);
            return null;
        }
    }
    private void mergeWithExisting(Exercise fromStrava) {
        if (fromStrava == null || !fromStrava.hasTrail()) return;

        ArrayList<Exercise> matching = Helper.getReader(a).getExercisesForMerge(fromStrava.getDateTime(), fromStrava.getType());

        if (matching.size() == 1) {
            Exercise m = matching.get(0);
            Exercise merged = new Exercise(m.get_id(), m.getId(), m.getType(), fromStrava.getDateTime(), m.getRouteId(), m.getRoute(), m.getRouteVar(), m.getInterval(),
                    m.getNote(), m.getDataSource(), m.getRecordingMethod(), fromStrava.getDistancePrimary(), fromStrava.getTimePrimary(), m.getSubs(), fromStrava.getTrail());

            Helper.getWriter(a).updateExercise(merged);
        }
        else if (matching.size() == 0) {
            Helper.getWriter(a).addExercise(fromStrava, a);
            //L.toast("import on " + dateTime.toLocalDate().format(C.FORMATTER_SQL_DATE), a);
        }
        else L.toast("multiple choice on " + fromStrava.getDateTime().format(C.FORMATTER_SQL_DATE), a);

    }

    // url:s
    private String getAuthCodeURL() {
        return "https://www.strava.com/oauth/authorize?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=activity:read_all";
    }
    private String getRefreshTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + authCode + "&grant_type=authorization_code";
    }
    private String getAccessTokenURL() {
        return "https://www.strava.com/oauth/token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&refresh_token=" + refreshToken + "&grant_type=refresh_token";
    }
    private String getActivitiesURL(int page) {
        return "https://www.strava.com/api/v3/athlete/activities?per_page=" + PER_PAGE + "&access_token=" + accessToken + "&page=" + page;
    }

    // tokens
    private String getAccessToken() {
        if (true || LocalDateTime.now().isAfter(accessTokenExpiresAt)) {
            requestAccessToken();
        }
        return accessToken;
    }

}
