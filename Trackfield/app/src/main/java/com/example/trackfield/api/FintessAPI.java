package com.example.trackfield.api;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public abstract class FintessAPI extends AppCompatActivity {

    private Activity a;

    private static FitnessOptions fitnessOptions;
    private SimpleDateFormat formatterStart = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.ENGLISH);
    private SimpleDateFormat formatterEnd = new SimpleDateFormat("hh:mm", Locale.ENGLISH);

    // request codes
    private static final int REQUEST_CODE_PERMISSIONS_GOOGLE_FIT = 1;

    ////

    protected void connectAPI() {

        a = this;

        // Google Fit
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
        if (hasPermissionsElseRequest()) accessGoogleFit();

    }

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

    // authorization flow result
    private boolean hasPermissionsElseRequest() {
        // Check if the user has previously granted the necessary data access, and if not, initiate the authorization flow:

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) return true;
        GoogleSignIn.requestPermissions(this, REQUEST_CODE_PERMISSIONS_GOOGLE_FIT, account, fitnessOptions);
        return false;
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PERMISSIONS_GOOGLE_FIT) accessGoogleFit();
        }
    }

}
