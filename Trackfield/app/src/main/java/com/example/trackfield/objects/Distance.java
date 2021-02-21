package com.example.trackfield.objects;

import android.content.Context;

import com.example.trackfield.objects.interfaces.JSONObjectable;
import com.example.trackfield.toolbox.L;

import org.json.JSONException;
import org.json.JSONObject;

public class Distance implements JSONObjectable {

    private final int _id;
    private int distance;
    private float goalPace = NO_GOAL_PACE;

    public enum SortMode {
        DISTANCE,
        AMOUNT,
        BEST_TIME,
        BEST_PACE
    }

    public static final int NO_GOAL_PACE = -1;

    // json
    private static final String JSON_ID = "id";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_GOAL_PACE = "goal_pace";

    ////

    public Distance(int _id, int distance, float goalPace) {
        this._id = _id;
        this.distance = distance;
        this.goalPace = goalPace;
    }

    public Distance(int _id, int distance) {
        this._id = _id;
        this.distance = distance;
    }

    public Distance(JSONObject obj) throws JSONException {
        _id = obj.getInt(JSON_ID);
        distance = obj.getInt(JSON_DISTANCE);
        goalPace = (float) obj.getDouble(JSON_GOAL_PACE);
    }

    // set

    public void setGoalPace(float goalPace) {
        this.goalPace = goalPace;
    }

    public void removeGoalPace() {
        this.goalPace = NO_GOAL_PACE;
    }

    // get

    public int get_id() {
        return _id;
    }

    public int getDistance() {
        return distance;
    }

    public float getGoalPace() {
        return goalPace;
    }

    public boolean hasGoalPace() {
        return goalPace != NO_GOAL_PACE;
    }

    // extends

    @Override public JSONObject toJSONObject(Context c) {

        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, _id);
            obj.put(JSON_DISTANCE, distance);
            obj.put(JSON_GOAL_PACE, goalPace);
        }
        catch (JSONException e) {
            L.handleError(e, c);
        }

        return obj;
    }

}
