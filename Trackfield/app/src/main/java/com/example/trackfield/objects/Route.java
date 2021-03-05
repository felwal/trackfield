package com.example.trackfield.objects;

import android.content.Context;

import com.example.trackfield.objects.interfaces.JSONObjectable;
import com.example.trackfield.toolbox.L;

import org.json.JSONException;
import org.json.JSONObject;

public class Route implements JSONObjectable {

    private final int _id;
    private String name;
    private float goalPace = NO_GOAL_PACE;
    private boolean hidden = false;

    public enum SortMode { RECENT, NAME, AMOUNT, AVG_DISTANCE, BEST_PACE }

    public static final String NO_NAME = "[Route not found]";
    public static final int NO_GOAL_PACE = -1;
    public static final int ID_NON_EXISTANT = -1;

    // json
    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_GOAL_PACE = "goal_pace";
    private static final String JSON_HIDDEN = "hidden";

    ////

    public Route(int _id, String name, float goalPace, boolean hidden) {
        this._id = _id;
        this.name = name;
        this.goalPace = goalPace;
        this.hidden = hidden;
    }

    public Route(int _id, String name) {
        this._id = _id;
        this.name = name;
    }

    public Route(JSONObject obj) throws JSONException {
        _id = obj.getInt(JSON_ID);
        name = obj.getString(JSON_NAME);
        goalPace = (float) obj.getDouble(JSON_GOAL_PACE);
        hidden = obj.getBoolean(JSON_HIDDEN);
    }

    public Route() {
        _id = -1;
        name = NO_NAME;
    }

    // set

    public void setName(String name) {
        this.name = name;
    }

    public void setGoalPace(float goalPace) {
        this.goalPace = goalPace;
    }

    public void removeGoalPace() {
        this.goalPace = NO_GOAL_PACE;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void invertHidden() {
        hidden = !hidden;
    }

    // get

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public float getGoalPace() {
        return goalPace;
    }

    public boolean hasGoalPace() {
        return goalPace != NO_GOAL_PACE;
    }

    public boolean isHidden() {
        return hidden;
    }

    // extends

    @Override public JSONObject toJSONObject(Context c) {

        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, _id);
            obj.put(JSON_NAME, name);
            obj.put(JSON_GOAL_PACE, goalPace);
            obj.put(JSON_HIDDEN, hidden);
        }
        catch (JSONException e) {
            L.handleError(e, c);
        }

        return obj;
    }

}
