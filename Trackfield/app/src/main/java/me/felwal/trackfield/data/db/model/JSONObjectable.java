package me.felwal.trackfield.data.db.model;

import android.content.Context;

import org.json.JSONObject;

public interface JSONObjectable {

    JSONObject toJSONObject(Context c);

}
