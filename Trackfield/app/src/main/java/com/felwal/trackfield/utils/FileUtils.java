package com.felwal.trackfield.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbHelper;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.db.model.Distance;
import com.felwal.trackfield.data.db.model.Exercise;
import com.felwal.trackfield.data.db.model.JSONObjectable;
import com.felwal.trackfield.data.db.model.Place;
import com.felwal.trackfield.data.db.model.Route;
import com.felwal.trackfield.data.prefs.Prefs;

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

    // filenames
    private static final String FILENAME_E = "exercises.json";
    private static final String FILENAME_R = "routes.json";
    private static final String FILENAME_D = "distances.json";
    private static final String FILENAME_P = "places.json";
    private static final String FILENAME_VER = "version.json";

    // json keys
    private static final String JSON_DB_VERSION = "db_version";

    //

    private FileUtils() {
        // this utility class is not publicly instantiable
    }

    //

    private static String getPath(String filename) {
        return Environment.getExternalStorageDirectory().getPath() + "/" + Prefs.getFileLocation() + "/" + filename;
    }

    // general file tools

    /**
     * @return Success
     */
    private static boolean writeFile(String pathname, String content, Context c) {
        if (hasNotPermissionToStorage(c)) return false;

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

    @Nullable
    private static List<String> readFile(String pathname, Context c) {
        if (hasNotPermissionToStorage(c)) return null;

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

    /**
     * @return Success
     */
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
        }
        catch (JSONException e) {
            //L.handleError(e, c);
            return false;
        }
    }

    @Nullable
    private static String readJson(String pathname, Context c) {
        if (hasNotPermissionToStorage(c)) return null;

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
        }
        catch (JSONException | NullPointerException e) {
            return new ArrayList<>();
        }

        return objs;
    }

    // project json

    /**
     * @return Success
     */
    public static boolean exportJson(Context c) {
        boolean success;

        // version.json
        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_DB_VERSION, DbReader.get(c).getVersion());
            String jsonStr = obj.toString(2);
            success = writeFile(getPath(FILENAME_VER), jsonStr, c);
        }
        catch (JSONException e) {
            e.printStackTrace();
            success = false;
        }

        // jsonarrays
        success &= writeJSONObjectList(getPath(FILENAME_E), DbReader.get(c).getExercises(), c);
        success &= writeJSONObjectList(getPath(FILENAME_R), DbReader.get(c).getRoutes(true), c);
        success &= writeJSONObjectList(getPath(FILENAME_D), DbReader.get(c).getDistances(), c);
        success &= writeJSONObjectList(getPath(FILENAME_P), DbReader.get(c).getPlaces(), c);

        return success;
    }

    /**
     * @return Success
     */
    public static boolean importJson(Context c) {
        int dbVersion = importVersionJson(c);
        if (dbVersion == -1) dbVersion = DbHelper.DATABASE_TARGET_VERSION;
        DbWriter.get(c).recreate(dbVersion);

        boolean success;

        success = importRoutesJson(c);
        success &= importDistancesJson(c);
        success &= importPlacesJson(c);
        success &= importExercisesJson(c);

        DbWriter.get(c).upgradeToTargetVersion(dbVersion);

        return success;
    }

    /**
     * @return Database version
     */
    private static int importVersionJson(Context c) {
        int dbVersion;
        String pathname = getPath(FILENAME_VER);

        try {
            String response = readJson(pathname, c);
            JSONObject obj = new JSONObject(response);
            dbVersion = obj.getInt(JSON_DB_VERSION);
        }
        catch (JSONException | NullPointerException e) {
            dbVersion = -1;
        }

        return dbVersion;
    }

    /**
     * @return Success
     */
    private static boolean importExercisesJson(Context c) {
        boolean success = true;

        ArrayList<Exercise> exercises = new ArrayList<>();

        for (JSONObject obj : readJSONObjectList(getPath(FILENAME_E), c)) {
            try {
                DbReader.get(c);
                Exercise e = new Exercise(obj, c);
                exercises.add(e);
            }
            catch (JSONException e) {
                success = false;
            }
        }

        if (exercises.size() == 0) return false;
        DbWriter.get(c).addExercises(exercises, c);
        return success;
    }

    /**
     * @return Success
     */
    private static boolean importRoutesJson(Context c) {
        boolean success = true;

        ArrayList<Route> routes = new ArrayList<>();

        for (JSONObject obj : readJSONObjectList(getPath(FILENAME_R), c)) {
            try {
                Route r = new Route(obj);
                routes.add(r);
            }
            catch (JSONException e) {
                success = false;
            }
        }

        if (routes.size() == 0) return false;
        DbWriter.get(c).addRoutes(routes, c);
        return success;
    }

    /**
     * @return Success
     */
    private static boolean importDistancesJson(Context c) {
        boolean success = true;

        ArrayList<Distance> distances = new ArrayList<>();

        for (JSONObject obj : readJSONObjectList(getPath(FILENAME_D), c)) {
            try {
                Distance d = new Distance(obj);
                distances.add(d);
            }
            catch (JSONException e) {
                success = false;
            }
        }

        if (distances.size() == 0) return false;
        DbWriter.get(c).addDistances(distances);
        return success;
    }

    /**
     * @return Success
     */
    private static boolean importPlacesJson(Context c) {
        boolean success = true;

        ArrayList<Place> places = new ArrayList<>();

        for (JSONObject obj : readJSONObjectList(getPath(FILENAME_P), c)) {
            try {
                Place p = new Place(obj);
                places.add(p);
            }
            catch (JSONException e) {
                success = false;
            }
        }

        if (places.size() == 0) return false;
        DbWriter.get(c).addPlaces(places);
        return success;
    }

    // permissions

    public static boolean shouldAskPermissions(Context c) {
        return hasNotPermissionToStorage(c) || hasNotPermissionToLocation(c);
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

    public static boolean hasNotPermissionToStorage(Context c) {
        return !(ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasNotPermissionToLocation(Context c) {
        return !(ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED);
    }

}
