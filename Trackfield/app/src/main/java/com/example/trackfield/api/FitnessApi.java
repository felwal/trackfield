package com.example.trackfield.api;

import android.app.Activity;

import com.example.trackfield.toolbox.L;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class FitnessApi {

    private Activity a;

    private static FitnessOptions fitnessOptions;
    private final SimpleDateFormat formatterStart = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.ENGLISH);
    private final SimpleDateFormat formatterEnd = new SimpleDateFormat("hh:mm", Locale.ENGLISH);

    // request codes
    public static final int REQUEST_CODE_PERMISSIONS_GOOGLE_FIT = 1;

    ////

    public FitnessApi(Activity a) {
        this.a = a;
        connectFitness();
    }

    // connect

    private void connectFitness() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
        //if (hasPermissionsElseRequest()) requestActivities();
    }

    public void requestActivities() {
        if (!hasPermissionsElseRequest()) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);

        long startTime = cal.getTimeInMillis();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(a, fitnessOptions);
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readSessionsFromAllApps()
                //.read(DataType.TYPE_LOCATION_SAMPLE)
                //.aggregate(DataType.AGGREGATE_DISTANCE_DELTA)
                //.aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                //.bucketBySession(60, TimeUnit.SECONDS)
                .build();

        Fitness.getSessionsClient(a, account).readSession(readRequest)
                .addOnSuccessListener(response -> {
                    for (Session session : response.getSessions()) dumpSession(session);
                })
                .addOnFailureListener(e -> {
                    //L.toast("There was an error reading data from Google Fit:", a);
                    L.handleError(e, a);
                });
    }

    // permissions / authorization

    public boolean hasPermissionsElseRequest() {
        // Check if the user has previously granted the necessary data access, and if not, initiate the authorization flow:

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(a, fitnessOptions);

        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) return true;
        GoogleSignIn.requestPermissions(a, REQUEST_CODE_PERMISSIONS_GOOGLE_FIT, account, fitnessOptions);
        return false;
    }

    public void permissionsGained() {
        //requestActivities();
    }

    // dump

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

}
