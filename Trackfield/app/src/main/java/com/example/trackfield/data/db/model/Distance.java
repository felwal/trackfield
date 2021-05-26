package com.example.trackfield.data.db.model;

import android.content.Context;

import com.example.trackfield.utils.LayoutUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Distance implements JSONObjectable {

    public static final int NO_DISTANCE = -1;
    public static final int NO_GOAL_PACE = -1;

    // json keys
    private static final String JSON_ID = "_id";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_GOAL_PACE = "goal_pace";

    private final int id;
    private final int distance;
    private float goalPace = NO_GOAL_PACE;

    //

    public Distance(int id, int distance, float goalPace) {
        this.id = id;
        this.distance = distance;
        this.goalPace = goalPace;
    }

    public Distance(int id, int distance) {
        this.id = id;
        this.distance = distance;
    }

    public Distance(JSONObject obj) throws JSONException {
        id = obj.getInt(JSON_ID);
        distance = obj.getInt(JSON_DISTANCE);
        goalPace = (float) obj.getDouble(JSON_GOAL_PACE);
    }

    // set

    public void removeGoalPace() {
        this.goalPace = NO_GOAL_PACE;
    }

    public int getId() {
        return id;
    }

    // get

    public int getDistance() {
        return distance;
    }

    public float getGoalPace() {
        return goalPace;
    }

    public void setGoalPace(float goalPace) {
        this.goalPace = goalPace;
    }

    public boolean hasGoalPace() {
        return goalPace != NO_GOAL_PACE;
    }

    // implements JSONObjectable

    @Override
    public JSONObject toJSONObject(Context c) {
        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, id);
            obj.put(JSON_DISTANCE, distance);
            obj.put(JSON_GOAL_PACE, goalPace);
        }
        catch (JSONException e) {
            LayoutUtils.handleError(e, c);
        }

        return obj;
    }

}
