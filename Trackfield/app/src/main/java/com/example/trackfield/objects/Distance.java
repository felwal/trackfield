package com.example.trackfield.objects;

public class Distance {

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

}
