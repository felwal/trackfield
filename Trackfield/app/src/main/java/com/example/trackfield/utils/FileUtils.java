package com.example.trackfield.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trackfield.data.db.DbHelper;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.data.db.model.JSONObjectable;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.data.db.model.Sub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

// File
public final class FileUtils {

    // filena,es
    private static final String FILENAME_E = "exercises.json";
    private static final String FILENAME_R = "routes.json";
    private static final String FILENAME_D = "distances.json";
    private static final String FILENAME_VER = "version.json";
    private static final String FOLDER = "Trackfield";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER + "/";

    // json keys
    private static final String JSON_DB_VERSION = "db_version";

    //

    private FileUtils() {
        // this utility class is not publicly instantiable
    }

    // general file tools

    private static boolean writeFile(String pathname, String content, Context c) {
        // export
        try {
            // open
            java.io.File file = new java.io.File(pathname);
            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream);

            // write
            writer.append(content);

            // close
            writer.close();
            stream.flush();
            stream.close();

            return true;
        }
        catch (IOException e) {
            //L.handleError(e, c);
            e.printStackTrace();
            return false;
        }
    }

    private static List<String> readFile(String pathname, Context c) {
        List<String> lines = new ArrayList<>();

        try {
            // open
            java.io.File file = new java.io.File(pathname);
            if (!file.exists()) return lines;
            FileInputStream stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            // add lines
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            // close
            reader.close();
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    // general json tools

    private static boolean writeJSONObjectList(String pathname, ArrayList<? extends JSONObjectable> objs, Context c) {
        JSONArray array = new JSONArray();

        // fill array
        for (JSONObjectable obj : objs) {
            array.put(obj.toJSONObject(c));
        }

        // export
        try {
            String jsonStr = array.toString(2);
            return writeFile(pathname, jsonStr, c);

            //L.toast(c.getString(R.string.toast_file_exported), c);
        }
        catch (JSONException e) {
            //L.handleError(e, c);
            return false;
        }
    }

    private static String readJson(String pathname, Context c) {
        String response = "";
        try {
            // open
            java.io.File file = new java.io.File(pathname);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder builder = new StringBuilder();

            // lines
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();

            response = builder.toString();
        }
        catch (IOException e) {
            LayoutUtils.handleError(e, c);
        }

        return response;
    }

    private static List<JSONObject> readJSONObjectList(String pathname, Context c) {
        List<JSONObject> objs = new ArrayList<>();

        try {
            String response = readJson(pathname, c);
            JSONArray array = new JSONArray(response);

            // get objs
            for (int i = 0; i < array.length(); i++) {
                // to object
                try {
                    objs.add(array.getJSONObject(i));
                }
                catch (JSONException e) {
                    LayoutUtils.handleError(e, c);
                }
            }

            //L.toast("Imported", c);
        }
        catch (JSONException e) {
            LayoutUtils.handleError(e, c);
        }

        return objs;
    }

    // json

    public static boolean exportJson(Context c) {
        boolean success = true;

        // version.json
        JSONObject obj = new JSONObject();
        try {
            obj.put(JSON_DB_VERSION, DbReader.get(c).getVersion());
            String jsonStr = obj.toString(2);
            success &= writeFile(PATH + FILENAME_VER, jsonStr, c);
        }
        catch (JSONException e) {
            e.printStackTrace();
            success = false;
        }

        // jsonarrays
        success &= writeJSONObjectList(PATH + FILENAME_E, DbReader.get(c).getExercises(), c);
        success &= writeJSONObjectList(PATH + FILENAME_R, DbReader.get(c).getRoutes(true), c);
        success &= writeJSONObjectList(PATH + FILENAME_D, DbReader.get(c).getDistances(), c);

        return success;
    }

    public static boolean importJson(Context c) {
        int dbVersion = importVersionJson(c);
        if (dbVersion == -1) dbVersion = DbHelper.DATABASE_TARGET_VERSION;
        DbWriter.get(c).recreate(dbVersion);

        boolean success;

        success = importRoutesJson(c);
        success &= importDistancesJson(c);
        success &= importExercisesJson(c);

        DbWriter.get(c).upgradeToTargetVersion(dbVersion);

        return success;
    }

    private static int importVersionJson(Context c) {
        int dbVersion;
        String pathname = PATH + FILENAME_VER;

        try {
            String response = readJson(pathname, c);
            JSONObject obj = new JSONObject(response);
            dbVersion = obj.getInt(JSON_DB_VERSION);
        }
        catch (JSONException e) {
            //L.handleError("Failed to find database version in json file", e, c);
            dbVersion = -1;
        }

        return dbVersion;
    }

    private static boolean importExercisesJson(Context c) {
        boolean success = true;

        ArrayList<Exercise> exercises = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_E, c)) {
            try {
                DbReader.get(c);
                Exercise e = new Exercise(obj, c);
                exercises.add(e);
            }
            catch (JSONException e) {
                //L.handleError(c.getString(R.string.toast_err_parse_jsonobj), e, c);
                success = false;
            }
        }

        DbWriter.get(c).addExercises(exercises, c);
        //L.toast(c.getString(R.string.toast_file_imported), c);
        return success;
    }

    private static boolean importRoutesJson(Context c) {
        boolean success = true;

        ArrayList<Route> routes = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_R, c)) {
            try {
                Route r = new Route(obj);
                routes.add(r);
            }
            catch (JSONException e) {
                //L.handleError(e, c);
                success = false;
            }
        }

        DbWriter.get(c).addRoutes(routes, c);
        //L.toast(c.getString(R.string.toast_file_imported), c);
        return success;
    }

    private static boolean importDistancesJson(Context c) {
        boolean success = true;

        ArrayList<Distance> distances = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_D, c)) {
            try {
                Distance d = new Distance(obj);
                distances.add(d);
            }
            catch (JSONException e) {
                //L.handleError(e, c);
                success = false;
            }
        }

        DbWriter.get(c).addDistances(distances);
        //L.toast(c.getString(R.string.toast_file_imported), c);
        return success;
    }

    // permissions

    public static boolean shouldAskPermissions(Context c) {
        return !permissionToStorage(c) || !permissionToLocation(c);
    }

    @TargetApi(23)
    public static void askPermissions(Activity a) {
        String[] permissions = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION" };
        int requestCode = 200;
        ActivityCompat.requestPermissions(a, permissions, requestCode);
    }

    public static boolean permissionToStorage(Context c) {
        return ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
            && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean permissionToLocation(Context c) {
        return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED;
    }

}
