package com.example.trackfield.objects;

public class Route {

    private final int _id;
    private String name;
    private float goalPace = NO_GOAL_PACE;
    private boolean hidden = false;

    public enum SortMode {
        RECENT,
        NAME,
        AMOUNT,
        AVG_DISTANCE,
        BEST_PACE
    }

    public static final int NO_GOAL_PACE = -1;

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
    public Route() {
        _id = -1;
        name = "[route not found]";
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

}
