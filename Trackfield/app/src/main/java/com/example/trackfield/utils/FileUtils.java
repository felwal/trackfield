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

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.JSONObjectable;
import com.example.trackfield.data.db.Helper;
import com.example.trackfield.data.db.Reader;
import com.example.trackfield.data.db.Writer;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.data.db.model.Sub;
import com.example.trackfield.ui.map.model.Trail;
import com.google.android.gms.maps.model.LatLng;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// File
public final class FileUtils {

    // file keys
    private static final String FILENAME_E_TXT = "exercises.txt";
    private static final String FILENAME_S_TXT = "subs.txt";
    private static final String FILENAME_R_TXT = "routes.txt";
    private static final String FILENAME_D_TXT = "distances.txt";
    private static final String FILENAME_E = "exercises.json";
    private static final String FILENAME_R = "routes.json";
    private static final String FILENAME_D = "distances.json";
    private static final String FILENAME_VER = "version.json";
    private static final String FOLDER = "Trackfield";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER + "/";
    private static final char DIV_READ = '•';
    private static final char DIV_WRITE = '•';

    private static final String JSON_DB_VERSION = "database_version";

    // prefs keys
    public static final String SP_SHARED_PREFERENCES = "shared preferences";
    private static final String SP_PREFS = "prefs";

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
            obj.put(JSON_DB_VERSION, Reader.get(c).getVersion());
            String jsonStr = obj.toString(2);
            success &= writeFile(PATH + FILENAME_VER, jsonStr, c);
        }
        catch (JSONException e) {
            e.printStackTrace();
            success = false;
        }

        // jsonarrays
        success &= writeJSONObjectList(PATH + FILENAME_E, Reader.get(c).getExercises(), c);
        success &= writeJSONObjectList(PATH + FILENAME_R, Reader.get(c).getRoutes(true), c);
        success &= writeJSONObjectList(PATH + FILENAME_D, Reader.get(c).getDistances(), c);

        return success;
    }

    public static boolean importJson(Context c) {
        int dbVersion = importVersionJson(c);
        if (dbVersion == -1) dbVersion = Helper.DATABASE_TARGET_VERSION;
        Writer.get(c).recreate(dbVersion);

        boolean success;

        success = importRoutesJson(c);
        success &= importDistancesJson(c);
        success &= importExercisesJson(c);

        Writer.get(c).upgradeToTargetVersion(dbVersion);

        return success;
    }

    private static int importVersionJson(Context c) {
        int dbVersion = Helper.DATABASE_TARGET_VERSION;
        String pathname = PATH + FILENAME_VER;

        try {
            String response = readJson(pathname, c);
            JSONObject obj = new JSONObject(response);
            dbVersion = obj.getInt(JSON_DB_VERSION);
        }
        catch (JSONException e) {
            //L.handleError("Failed to find database version in json file, using target version instead", e, c);
            dbVersion = -1;
        }

        return dbVersion;
    }

    private static boolean importExercisesJson(Context c) {
        boolean success = true;

        ArrayList<Exercise> exercises = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_E, c)) {
            try {
                Reader.get(c);
                Exercise e = new Exercise(obj, c);
                exercises.add(e);
            }
            catch (JSONException e) {
                //L.handleError(c.getString(R.string.toast_err_parse_jsonobj), e, c);
                success = false;
            }
        }

        Writer.get(c).addExercises(exercises, c);
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

        Writer.get(c).addRoutes(routes, c);
        //L.toast(c.getString(R.string.toast_file_imported), c);s
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

        Writer.get(c).addDistances(distances);
        //L.toast(c.getString(R.string.toast_file_imported), c);
        return success;
    }

    // txt

    @Deprecated
    public static void exportTxt(Context c) {

        //D.moveExercise();
        //D.trimRoutes();

        try {
            // exercise
            java.io.File eFile = new java.io.File(PATH + FILENAME_E_TXT);
            java.io.File sFile = new java.io.File(PATH + FILENAME_S_TXT);
            FileOutputStream eFos = new FileOutputStream(eFile);
            FileOutputStream sFos = new FileOutputStream(sFile);
            OutputStreamWriter eWriter = new OutputStreamWriter(eFos);
            OutputStreamWriter sWriter = new OutputStreamWriter(sFos);
            for (Exercise e : Reader.get(c).getExercises()) {
                eWriter.append(e.extractToFile(DIV_WRITE) + "\n");
                for (int index = 0; index < e.getSubs().size(); index++) {
                    sWriter.append(e.getSub(index).extractToFile(DIV_WRITE, e.get_id(), index) + "\n");
                }
            }
            eWriter.close();
            eFos.flush();
            eFos.close();
            sWriter.close();
            sFos.flush();
            sFos.close();

            // routes
            java.io.File rFile = new java.io.File(PATH + FILENAME_R_TXT);
            FileOutputStream rFos = new FileOutputStream(rFile);
            OutputStreamWriter rWriter = new OutputStreamWriter(rFos);
            for (Route r : Reader.get(c).getRoutes(true)) {
                rWriter.append(r.getName() + "\n");
            }
            rWriter.close();
            rFos.flush();
            rFos.close();

            // distance
            java.io.File dFile = new java.io.File(PATH + FILENAME_D_TXT);
            FileOutputStream dFos = new FileOutputStream(dFile);
            OutputStreamWriter dWriter = new OutputStreamWriter(dFos);
            for (Distance d : Reader.get(c).getDistances()) {
                dWriter.append(d.getDistance() + "\n");
            }
            dWriter.close();
            dFos.flush();
            dFos.close();

            //Toast.makeText(c,"Done writing to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            LayoutUtils.toast(R.string.toast_json_export_successful, c);
        }
        catch (Exception e) {
            LayoutUtils.handleError(e, c);
        }
    }

    @Deprecated
    public static void importTxt(Context c) {

        // are you sure?

        //D.exercises.clear();
        //D.routes.clear();
        //D.distances.clear();
        //Helper.getWriter(c).deleteAllExercises();
        Writer.get(c).recreate();

        try {
            // sub
            ArrayList<ArrayList<Sub>> subSets = new ArrayList<>();
            ArrayList<Sub> subs = new ArrayList<>();

            java.io.File sFile = new java.io.File(PATH + FILENAME_S_TXT);
            if (!sFile.exists()) {
                return;
            }
            FileInputStream sFis = new FileInputStream(sFile);
            BufferedReader sReader = new BufferedReader(new InputStreamReader(sFis));
            String previousLine = null;
            String line = sReader.readLine();
            String nextLine = sReader.readLine();
            while (line != null) {

                // values
                int superId = -1;
                int index = 0;
                int distance = 0;
                float time = 0;

                // get values
                int section = 0;
                String temp = "";
                for (int ch = 0; ch < line.length(); ch++) {
                    if (line.charAt(ch) != DIV_READ) {
                        temp += line.charAt(ch);
                    }
                    if (line.charAt(ch) == DIV_READ || ch == line.length() - 1) {
                        switch (section) {
                            case 0: // superId
                                superId = Integer.valueOf(temp);
                                break;
                            case 1: // index
                                index = Integer.valueOf(temp);
                                break;
                            case 2: // distance
                                distance = Integer.valueOf(temp);
                                break;
                            case 3: // time
                                time = MathUtils.round(Float.valueOf(temp), 2);
                                break;
                            default:
                                break;
                        }
                        temp = "";
                        section++;
                    }
                }

                // add exercise
                if (superId != -1) {
                    if (index == 0 && previousLine != null) {
                        subSets.add(subs);
                        subs = new ArrayList<>();
                    }
                    subs.add(new Sub(-1, superId, distance, time));
                    if (nextLine == null) {
                        subSets.add(subs);
                    }
                }

                previousLine = line;
                line = nextLine;
                nextLine = sReader.readLine();
            }
            sReader.close();
            sFis.close();

            // exercise
            java.io.File eFile = new java.io.File(PATH + FILENAME_E_TXT);
            if (!eFile.exists()) {
                return;
            }
            FileInputStream eFis = new FileInputStream(eFile);
            BufferedReader eReader = new BufferedReader(new InputStreamReader(eFis));
            while ((line = eReader.readLine()) != null) {

                // values
                int _id = -1;
                int type = 0;
                LocalDateTime date = LocalDateTime.now();//.parse("0001/01/01", C.FORMATTER_FILE);
                String route = "";
                String routeVar = "";
                String interval = "";
                int distance = 0;
                float time = 0;
                String dataSource = "";
                String recordingMethod = "";
                String note = "";
                String startLat = "";
                String startLng = "";
                String endLat = "";
                String endLng = "";
                String polyline = "";

                // get values
                int section = 0;
                String temp = "";
                for (int ch = 0; ch < line.length(); ch++) {
                    if (line.charAt(ch) != DIV_READ) {
                        temp += line.charAt(ch);
                    }
                    if (line.charAt(ch) == DIV_READ || ch == line.length() - 1) {
                        switch (section) {
                            case 0:
                                _id = Integer.parseInt(temp);
                                break;
                            case 1:
                                type = Integer.parseInt(temp);
                                break;
                            case 2:
                                date = MathUtils.ofEpochSecond(Long.parseLong(temp));//.parse(temp, C.FORMATTER_FILE); break;
                            case 3:
                                route = temp;
                                break;
                            case 4:
                                routeVar = temp;
                                break;
                            case 5:
                                interval = temp;
                                break;
                            case 6:
                                distance = Integer.parseInt(temp);
                                break;
                            case 7:
                                time = Float.parseFloat(temp);
                                break;
                            case 8:
                                dataSource = temp;
                                break;
                            case 9:
                                recordingMethod = temp;
                                break;
                            case 10:
                                note = temp;
                                break;
                            case 11:
                                startLat = temp;
                                break;
                            case 12:
                                startLng = temp;
                                break;
                            case 13:
                                endLat = temp;
                                break;
                            case 14:
                                endLng = temp;
                                break;
                            case 15:
                                polyline = temp;
                                break;
                            default:
                                break;
                        }
                        temp = "";
                        section++;
                    }
                }

                // add exercise
                if (_id != -1) {
                    int routeId = Reader.get(c).getRouteId(route);
                    Trail trail = null;
                    if (!polyline.equals("")) {
                        if (!startLat.equals("") && !startLng.equals("") && !endLat.equals("") && !endLng.equals("")) {
                            LatLng start = new LatLng(Double.parseDouble(startLat), Double.parseDouble(startLng));
                            LatLng end = new LatLng(Double.parseDouble(endLat), Double.parseDouble(endLng));
                            trail = new Trail(polyline, start, end);
                        }
                        else trail = new Trail(polyline);
                    }

                    Exercise e = new Exercise(_id, -1, type, date, routeId, route, routeVar, interval, note, dataSource,
                        recordingMethod, distance, time, getSubsBySuperId(subSets, _id), trail);
                    Writer.get(c).addExercise(e, c);
                    //D.exercises.add(e);
                }
            }
            eReader.close();
            eFis.close();

            // route
            java.io.File rFile = new java.io.File(PATH + FILENAME_R_TXT);
            FileInputStream rFis = new FileInputStream(rFile);
            BufferedReader rReader = new BufferedReader(new InputStreamReader(rFis));
            while ((line = rReader.readLine()) != null) {
                //D.routes.add(line);
                Writer.get(c).addRoute(new Route(line), c);
            }
            rReader.close();
            rFis.close();

            // distance
            java.io.File dFile = new java.io.File(PATH + FILENAME_D_TXT);
            FileInputStream dFis = new FileInputStream(dFile);
            BufferedReader dReader = new BufferedReader(new InputStreamReader(dFis));
            while ((line = dReader.readLine()) != null) {
                //D.distances.add(Integer.valueOf(line));
                Writer.get(c).addDistance(new Distance(-1, Integer.parseInt(line)));
            }
            dReader.close();
            dFis.close();

            //Toast.makeText(c,"Done reading to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            LayoutUtils.toast(R.string.toast_json_import_successful, c);
        }
        catch (Exception e) {
            LayoutUtils.handleError(e, c);
        }

        //D.edited();

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

    //

    private static ArrayList<Sub> getSubsBySuperId(ArrayList<ArrayList<Sub>> subSets, int superId) {

        for (ArrayList<Sub> subSet : subSets) {
            if (subSet.get(0).get_superId() == superId) {
                return subSet;
            }
        }

        return new ArrayList<>();
    }

}
