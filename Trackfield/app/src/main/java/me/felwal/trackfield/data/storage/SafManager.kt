package me.felwal.trackfield.data.storage

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import me.felwal.trackfield.data.db.model.JSONObjectable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader

open class SafManager(protected val applicationContext: Context) {

    private val resolver = applicationContext.contentResolver

    // json

    fun readJSONObjectList(uri: Uri): List<JSONObject> {
        val objs: MutableList<JSONObject> = mutableListOf()

        try {
            val content = readFile(uri)
            val array = JSONArray(content)

            for (i in 0 until array.length()) {
                try {
                    objs.add(array.getJSONObject(i))
                }
                catch (e: JSONException) {
                }
            }
        }
        catch (e: JSONException) {
            return listOf()
        }
        catch (e: NullPointerException) {
            return listOf()
        }
        return objs
    }

    fun writeJSONObjectList(uri: Uri, objs: ArrayList<out JSONObjectable>): Boolean {
        val array = objs.toJSONArray(applicationContext)

        return try {
            val jsonStr = array.toString(2)
            writeFile(uri, jsonStr)
        }
        catch (e: JSONException) {
            false
        }
    }

    // tree

    fun readTree(uri: Uri): List<DocumentFile> {
        val docDir = DocumentFile.fromTreeUri(applicationContext, uri)
        return readDocumentFile(docDir).onEach { persistPermissions(it.uri) }
    }

    /**
     * Recursively gets all files in a documentFile directory
     */
    private fun readDocumentFile(document: DocumentFile?): List<DocumentFile> {
        if (document == null || isFileHidden(document.uri)) return listOf()
        if (!document.isDirectory) return listOf(document)

        val docs = mutableListOf<List<DocumentFile>>()

        for (doc in document.listFiles()) {
            // go one dir deeper
            docs.add(readDocumentFile(doc))
        }

        return docs.flatten()
    }

    // file

    fun readFile(uri: Uri): String? {
        if (hasReadPermission(uri)) {
            return null
        }

        try {
            var content = ""
            try {
                resolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        content = reader.readText()
                    }
                }
            }
            catch (e: FileNotFoundException) {
                return null
            }

            return content
        }
        catch (e: SecurityException) {
        }
        catch (e: IllegalArgumentException) {
        }

        return null
    }

    fun writeFile(uri: Uri, content: String): Boolean {
        /*if (!hasWritePermission(uri)) {
            return false
        }*/

        try {
            resolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(content.toByteArray())
                }
            }
        }
        catch (e: SecurityException) {
            return false
        }
        catch (e: FileNotFoundException) {
            return false
        }
        catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    // permission

    private fun persistPermissions(uri: Uri) {
        try {
            val permissions = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            return resolver.takePersistableUriPermission(uri, permissions)
        }
        catch (e: SecurityException) {
        }
    }

    private fun getPersistedPermission(uri: Uri): UriPermission? {
        val permissions = resolver.persistedUriPermissions

        val index = permissions.map { it.uri.toString() }.indexOf(uri.toString())
        return if (index != -1) permissions[index] else null
    }

    private fun hasReadPermission(uri: Uri): Boolean =
        getPersistedPermission(uri)?.isReadPermission ?: false

    private fun hasWritePermission(uri: Uri): Boolean =
        getPersistedPermission(uri)?.isWritePermission ?: false

    // tools

    private fun List<JSONObject>.toJSONArray(): JSONArray {
        val array = JSONArray()
        forEach { array.put(it) }
        return array
    }

    private fun List<JSONObjectable>.toJSONArray(c: Context): JSONArray {
        val array = JSONArray()
        forEach { array.put(it.toJSONObject(c)) }
        return array
    }

    private fun isFileHidden(uri: Uri): Boolean =
        resolver.getDisplayName(uri).substring(0, 1) == "."

    @SuppressLint("Range")
    private fun ContentResolver.getDisplayName(uri: Uri): String {
        val cursor = query(uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return ""
    }

}
