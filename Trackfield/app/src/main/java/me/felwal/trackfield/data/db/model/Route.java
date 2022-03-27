package me.felwal.trackfield.data.db.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import me.felwal.trackfield.utils.LayoutUtils;

public class Route implements JSONObjectable {

    private static final String JSON_ID = "_id";
    private static final String JSON_NAME = "name";
    private static final String JSON_GOAL_PACE = "goal_pace";
    private static final String JSON_HIDDEN = "hidden";

    public static final String NAME_NONE = "[Route not found]";
    public static final int GOAL_PACE_NONE = -1;
    public static final int ID_NON_EXISTANT = -1;

    private final int id;
    private String name;
    private float goalPace = GOAL_PACE_NONE;
    private boolean hidden = false;

    //

    public Route(int id, String name, float goalPace, boolean hidden) {
        this.id = id;
        this.name = name;
        this.goalPace = goalPace;
        this.hidden = hidden;
    }

    public Route(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Route(String name) {
        id = ID_NON_EXISTANT;
        this.name = name;
    }

    public Route() {
        id = ID_NON_EXISTANT;
        name = NAME_NONE;
    }

    public Route(JSONObject obj) throws JSONException {
        id = obj.getInt(JSON_ID);
        name = obj.getString(JSON_NAME);
        goalPace = (float) obj.getDouble(JSON_GOAL_PACE);
        hidden = obj.getBoolean(JSON_HIDDEN);
    }

    // set

    public void setName(String name) {
        this.name = name;
    }

    public void setGoalPace(float goalPace) {
        this.goalPace = goalPace;
    }

    public void removeGoalPace() {
        this.goalPace = GOAL_PACE_NONE;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void invertHidden() {
        hidden = !hidden;
    }

    // get

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getGoalPace() {
        return goalPace;
    }

    public boolean hasGoalPace() {
        return goalPace != GOAL_PACE_NONE;
    }

    public boolean isHidden() {
        return hidden;
    }

    // extends

    @Override
    public JSONObject toJSONObject(Context c) {
        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, id);
            obj.put(JSON_NAME, name);
            obj.put(JSON_GOAL_PACE, goalPace);
            obj.put(JSON_HIDDEN, hidden);
        }
        catch (JSONException e) {
            LayoutUtils.handleError(e, c);
        }

        return obj;
    }

}
