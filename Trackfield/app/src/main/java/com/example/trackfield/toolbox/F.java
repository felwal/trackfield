package com.example.trackfield.toolbox;

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
import com.example.trackfield.database.Reader;
import com.example.trackfield.database.Writer;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Route;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.objects.interfaces.JSONObjectable;
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
public class F {

    // file keys
    private static final String FILENAME_E = "exercises.txt";
    private static final String FILENAME_S = "subs.txt";
    private static final String FILENAME_R = "routes.txt";
    private static final String FILENAME_D = "distances.txt";
    private static final String FILENAME_EJ = "exercises.json";
    private static final String FILENAME_RJ = "routes.json";
    private static final String FILENAME_DJ = "distances.json";
    private static final String FOLDER = "Trackfield";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER + "/";
    private static final char DIV_READ = '•';
    private static final char DIV_WRITE = '•';

    // prefs keys
    public static final String SP_SHARED_PREFERENCES = "shared preferences";
    private static final String SP_PREFS = "prefs";

    ////

    // general tools

    private static void writeFile(String pathname, String content, Context c) {

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
        }
        catch (IOException e) {
            L.handleError(e, c);
            e.printStackTrace();
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

    private static void writeJSONObjectList(String pathname, ArrayList<? extends JSONObjectable> objs, Context c) {

        JSONArray array = new JSONArray();

        // fill array
        for (JSONObjectable obj : objs) {
            array.put(obj.toJSONObject(c));
        }

        // export
        try {
            String jsonStr = array.toString(2);
            writeFile(pathname, jsonStr, c);

            L.toast(c.getString(R.string.toast_file_exported), c);
        }
        catch (JSONException e) {
            L.handleError(e, c);
        }

    }

    private static List<JSONObject> readJSONObjectList(String pathname, Context c) {

        List<JSONObject> objs = new ArrayList<>();

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

            // to array
            String response = builder.toString();
            JSONArray array = new JSONArray(response);

            // get objs
            for (int i = 0; i < array.length(); i++) {
                // to object
                try {
                    objs.add(array.getJSONObject(i));
                }
                catch (JSONException e) {
                    L.handleError(e, c);
                }
            }

            //L.toast("Imported", c);
        }
        catch (IOException | JSONException e) {
            L.handleError(e, c);
        }

        return objs;
    }

    // json

    public static void exportJson(Context c) {
        writeJSONObjectList(PATH + FILENAME_EJ, Reader.get(c).getExercises(), c);
        writeJSONObjectList(PATH + FILENAME_RJ, Reader.get(c).getRoutes(C.SortMode.DATE, true, true), c);
        writeJSONObjectList(PATH + FILENAME_DJ, Reader.get(c).getDistances(Distance.SortMode.DISTANCE, true), c);
    }

    public static void importJson(Context c) {
        Writer.get(c).recreate();
        importRoutesJson(c);
        importDistancesJson(c);
        importExercisesJson(c);
    }

    private static void importExercisesJson(Context c) {

        ArrayList<Exercise> exercises = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_EJ, c)) {
            try {
                Reader.get(c);
                Exercise e = new Exercise(obj, c);
                exercises.add(e);
            }
            catch (JSONException e) {
                L.handleError(c.getString(R.string.toast_err_parse_jsonobj), e, c);
            }
        }

        Writer.get(c).addExercises(exercises, c);
        L.toast(c.getString(R.string.toast_file_imported), c);

        /*
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

            // to array
            String response = builder.toString();
            JSONArray array = new JSONArray(response);

            // to exercises
            ArrayList<Exercise> exercises = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                // to object
                try {
                    JSONObject obj = array.getJSONObject(i);
                    Exercise e = new Exercise(obj);
                    exercises.add(e);
                }
                catch (JSONException e) {
                    L.handleError(e, c);
                }
            }

            // to database
            Helper.getWriter(c).addExercises(exercises, c);

            L.toast("Imported", c);
        }
        catch (IOException | JSONException e) {
            L.handleError(e, c);
        }

        */
    }

    private static void importRoutesJson(Context c) {

        ArrayList<Route> routes = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_RJ, c)) {
            try {
                Route r = new Route(obj);
                routes.add(r);
            }
            catch (JSONException e) {
                L.handleError(e, c);
            }
        }

        Writer.get(c).addRoutes(routes, c);
        L.toast(c.getString(R.string.toast_file_imported), c);
    }

    private static void importDistancesJson(Context c) {

        ArrayList<Distance> distances = new ArrayList<>();
        for (JSONObject obj : readJSONObjectList(PATH + FILENAME_DJ, c)) {
            try {
                Distance d = new Distance(obj);
                distances.add(d);
            }
            catch (JSONException e) {
                L.handleError(e, c);
            }
        }

        Writer.get(c).addDistances(distances);
        L.toast(c.getString(R.string.toast_file_imported), c);
    }

    // txt

    @Deprecated public static void exportTxt(Context c) {

        //D.moveExercise();
        //D.trimRoutes();

        try {
            // exercise
            java.io.File eFile = new java.io.File(PATH + FILENAME_E);
            java.io.File sFile = new java.io.File(PATH + FILENAME_S);
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
            eWriter.close(); eFos.flush(); eFos.close();
            sWriter.close(); sFos.flush(); sFos.close();

            // routes
            java.io.File rFile = new java.io.File(PATH + FILENAME_R);
            FileOutputStream rFos = new FileOutputStream(rFile);
            OutputStreamWriter rWriter = new OutputStreamWriter(rFos);
            for (Route r : Reader.get(c).getRoutes(C.SortMode.DATE, true, true)) {
                rWriter.append(r.getName() + "\n");
            }
            rWriter.close(); rFos.flush(); rFos.close();

            // distance
            java.io.File dFile = new java.io.File(PATH + FILENAME_D);
            FileOutputStream dFos = new FileOutputStream(dFile);
            OutputStreamWriter dWriter = new OutputStreamWriter(dFos);
            for (Distance d : Reader.get(c).getDistances(Distance.SortMode.DISTANCE, true)) {
                dWriter.append(d.getDistance() + "\n");
            }
            dWriter.close(); dFos.flush(); dFos.close();

            //Toast.makeText(c,"Done writing to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            L.toast(c.getString(R.string.toast_file_exported), c);
        }
        catch (Exception e) {
            L.handleError(e, c);
        }

    }

    @Deprecated public static void importTxt(Context c) {

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

            java.io.File sFile = new java.io.File(PATH + FILENAME_S);
            if (!sFile.exists()) { return; }
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
                    if (line.charAt(ch) == DIV_READ || ch == line.length()-1) {
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
                                time = M.round(Float.valueOf(temp), 2);
                                break;
                            default: break;
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
            sReader.close(); sFis.close();

            // exercise
            java.io.File eFile = new java.io.File(PATH + FILENAME_E);
            if (!eFile.exists()) { return; }
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
                            case 0: _id = Integer.parseInt(temp); break;
                            case 1: type = Integer.parseInt(temp); break;
                            case 2: date = M.ofEpoch(Long.parseLong(temp));//.parse(temp, C.FORMATTER_FILE); break;
                            case 3: route = temp; break;
                            case 4: routeVar = temp; break;
                            case 5: interval = temp; break;
                            case 6: distance = Integer.parseInt(temp); break;
                            case 7: time = Float.parseFloat(temp); break;
                            case 8: dataSource = temp; break;
                            case 9: recordingMethod = temp; break;
                            case 10: note = temp; break;
                            case 11: startLat = temp; break;
                            case 12: startLng = temp; break;
                            case 13: endLat = temp; break;
                            case 14: endLng = temp; break;
                            case 15: polyline = temp; break;
                            default: break;
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

                    Exercise e = new Exercise(_id, -1, type, date, routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time, getSubsBySuperId(subSets, _id), trail);
                    Writer.get(c).addExercise(e, c);
                    //D.exercises.add(e);
                }

            }
            eReader.close(); eFis.close();

            // route
            java.io.File rFile = new java.io.File(PATH + FILENAME_R);
            FileInputStream rFis = new FileInputStream(rFile);
            BufferedReader rReader = new BufferedReader(new InputStreamReader(rFis));
            while ((line = rReader.readLine()) != null) {
                //D.routes.add(line);
                Writer.get(c).addRouteIfNotAdded(new Route(-1, line), c);
            }
            rReader.close(); rFis.close();

            // distance
            java.io.File dFile = new java.io.File(PATH + FILENAME_D);
            FileInputStream dFis = new FileInputStream(dFile);
            BufferedReader dReader = new BufferedReader(new InputStreamReader(dFis));
            while ((line = dReader.readLine()) != null) {
                //D.distances.add(Integer.valueOf(line));
                Writer.get(c).addDistance(new Distance(-1, Integer.parseInt(line)));
            }
            dReader.close(); dFis.close();

            //Toast.makeText(c,"Done reading to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            L.toast(c.getString(R.string.toast_file_imported), c);
        }
        catch (Exception e) {
            L.handleError(e, c);
        }

        //D.edited();

    }

    // permissions

    public static boolean shouldAskPermissions(Context c) {
        return !permissionToStorage(c) || !permissionToLocation(c);
    }

    @TargetApi(23) public static void askPermissions(Activity a) {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION" };
        int requestCode = 200;
        ActivityCompat.requestPermissions(a, permissions, requestCode);
    }

    public static boolean permissionToStorage(Context c) {
        return ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean permissionToLocation(Context c) {
        return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //

    private static ArrayList<Sub> getSubsBySuperId(ArrayList<ArrayList<Sub>> subSets, int superId) {

        for (ArrayList<Sub> subSet : subSets) {
            if (subSet.get(0).get_superId() == superId) { return subSet; }
        }

        return new ArrayList<>();
    }

}
