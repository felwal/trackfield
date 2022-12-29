package me.felwal.trackfield.data.storage

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import me.felwal.trackfield.data.db.DbHelper
import me.felwal.trackfield.data.db.DbReader
import me.felwal.trackfield.data.db.DbWriter
import me.felwal.trackfield.data.db.model.Distance
import me.felwal.trackfield.data.db.model.Exercise
import me.felwal.trackfield.data.db.model.Place
import me.felwal.trackfield.data.db.model.Route
import me.felwal.trackfield.data.prefs.Prefs
import org.json.JSONException
import org.json.JSONObject

class BackupManager(applicationContext: Context) : SafManager(applicationContext) {

    // filenames
    private val FILENAME_E = "exercises.json"
    private val FILENAME_R = "routes.json"
    private val FILENAME_D = "distances.json"
    private val FILENAME_P = "places.json"
    private val FILENAME_VER = "version.json"

    // json keys
    private val JSON_DB_VERSION = "db_version"

    // read

    fun readBackup(): Boolean {
        val docs = readTree(Prefs.getFileLocation().toUri())
        val versionUri = docs.find { it.name == FILENAME_VER }?.uri
        val routesUri = docs.find { it.name == FILENAME_R }?.uri
        val distancesUri = docs.find { it.name == FILENAME_D }?.uri
        val placesUri = docs.find { it.name == FILENAME_P }?.uri
        val exercisesUri = docs.find { it.name == FILENAME_E }?.uri

        var dbVersion = versionUri?.let { readVersionBackup(versionUri) } ?: -1
        if (dbVersion == -1) dbVersion = DbHelper.DATABASE_TARGET_VERSION
        DbWriter.get(applicationContext).recreate(dbVersion)

        var success = routesUri?.let { readRoutesBackup(routesUri) } ?: false
        success = distancesUri?.let { readDistancesBackup(distancesUri) } ?: false && success
        success = placesUri?.let { readPlacesBackup(placesUri) } ?: false && success
        success = exercisesUri?.let { readExercisesBackup(exercisesUri) } ?: false && success

        return success
    }

    private fun readVersionBackup(uri: Uri): Int {
        val dbVersion: Int = try {
            val response = readFile(uri)
            val obj = JSONObject(response)
            obj.getInt(JSON_DB_VERSION)
        }
        catch (e: JSONException) {
            -1
        }
        catch (e: NullPointerException) {
            -1
        }

        return dbVersion
    }

    private fun readExercisesBackup(uri: Uri): Boolean {
        val objs = readJSONObjectList(uri)
        val exercises = arrayListOf<Exercise>()
        var success = true;

        for (obj in objs) {
            try {
                val e = Exercise(obj, applicationContext)
                exercises.add(e)
            }
            catch (e: JSONException) {
                success = false
            }
        }

        if (exercises.size == 0) return false

        return DbWriter.get(applicationContext).addExercises(exercises, applicationContext) && success
    }

    private fun readRoutesBackup(uri: Uri): Boolean {
        val objs = readJSONObjectList(uri)
        val routes = arrayListOf<Route>()
        var success = true;

        for (obj in objs) {
            try {
                val r = Route(obj)
                routes.add(r)
            }
            catch (e: JSONException) {
                success = false
            }
        }

        if (routes.size == 0) return false
        DbWriter.get(applicationContext).addRoutes(routes, applicationContext)
        return success
    }

    private fun readDistancesBackup(uri: Uri): Boolean {
        val objs = readJSONObjectList(uri)
        val distances = arrayListOf<Distance>()
        var success = true;

        for (obj in objs) {
            try {
                val d = Distance(obj)
                distances.add(d)
            }
            catch (e: JSONException) {
                success = false
            }
        }

        if (distances.size == 0) return false

        return DbWriter.get(applicationContext).addDistances(distances) && success
    }

    private fun readPlacesBackup(uri: Uri): Boolean {
        val objs = readJSONObjectList(uri)
        val places = arrayListOf<Place>()
        var success = true;

        for (obj in objs) {
            try {
                val p = Place(obj)
                places.add(p)
            }
            catch (e: JSONException) {
                success = false
            }
        }

        if (places.size == 0) return false

        return DbWriter.get(applicationContext).addPlaces(places) && success
    }

    // write

    fun writeBackup(): Boolean {
        val docs = readTree(Prefs.getFileLocation().toUri())
        val versionUri = docs.find { it.name == FILENAME_VER }?.uri
        val routesUri = docs.find { it.name == FILENAME_R }?.uri
        val distancesUri = docs.find { it.name == FILENAME_D }?.uri
        val placesUri = docs.find { it.name == FILENAME_P }?.uri
        val exercisesUri = docs.find { it.name == FILENAME_E }?.uri

        var success = true

        // version.json
        try {
            val obj = JSONObject()
            obj.put(JSON_DB_VERSION, DbReader.get(applicationContext).version)
            val jsonStr = obj.toString(2)

            // TODO: create if not exists
            versionUri?.let { writeFile(versionUri, jsonStr) }
        }
        catch (e: JSONException) {
            e.printStackTrace()
            success = false
        }

        // jsonarrays

        // TODO: create if not exists
        success = exercisesUri?.let { writeJSONObjectList(exercisesUri, DbReader.get(applicationContext).exercises) }
            ?: false && success
        success = routesUri?.let { writeJSONObjectList(routesUri, DbReader.get(applicationContext).getRoutes(true)) }
            ?: false && success
        success = distancesUri?.let { writeJSONObjectList(distancesUri, DbReader.get(applicationContext).distances) }
            ?: false && success
        success = placesUri?.let { writeJSONObjectList(placesUri, DbReader.get(applicationContext).places) }
            ?: false && success

        return success
    }
}
