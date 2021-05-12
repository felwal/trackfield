package com.example.trackfield.ui.common.model;

import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class Exerlite extends RecyclerItem {

    private final int _id;
    private final LocalDate date;
    private final String route;
    private final String interval;
    private final int distance;
    private final float time;
    private final boolean distanceDriven;
    private int top = 0;

    private static final int DISTANCE_DECIMALS = 1;

    //

    public Exerlite(int _id, LocalDate date, String route, String interval, int distance, float time, boolean distanceDriven) {
        this._id = _id;
        this.date = date;
        this.route = route;
        this.interval = interval;
        this.distance = distance;
        this.time = time;
        this.distanceDriven = distanceDriven;
    }

    // set

    public void setTop(int top) {
        this.top = top;
    }

    // get

    public int get_id() {
        return _id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getRoute() {
        return route;
    }

    public String getInterval() {
        return interval;
    }

    public int getDistance() {
        return distance;
    }

    public float getTime() {
        return time;
    }

    // get driven

    public boolean has_id(int _id) {
        return this._id == _id;
    }

    public float getPace() {
        return distance != 0 ? time / distance * 1000 : 0;
    }

    public float getTimeByDistance(int distance) {
        return getPace() * distance / 1000;
    }

    public int getWeek() {
        return date.get(AppConsts.WEEK_OF_YEAR);
    }

    public int getMonthValue() {
        return date.getMonthValue();
    }

    public String getMonth() {
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public boolean isYear(int year) {
        return date.getYear() == year;
    }

    public boolean isTop() {
        return top != 0;
    }

    public boolean isTop(int top) {
        return this.top == top;
    }

    // print

    public String printDate() {
        return LocalDate.now().getYear() == date.getYear() ? date.format(AppConsts.FORMATTER_CAPTION_NOYEAR) : date.format(
            AppConsts.FORMATTER_CAPTION);
    }

    public String printDistance() {
        String print = distance == 0 ? AppConsts.NO_VALUE : MathUtils.round(distance / 1000f, DISTANCE_DECIMALS) + "";
        return distanceDriven ? MathUtils.notateDriven(print) : print;
    }

    public String printTime() {
        return MathUtils.stringTime(time, true);
    }

    public String printPace() {
        return MathUtils.stringTime(getPace(), true);
    }

    public String printTimeByDistance(int distance) {
        String print = MathUtils.stringTime(getTimeByDistance(distance), true);
        return distance <= this.distance ? print : MathUtils.notateDriven(print);
    }

    public String printPrimary() {
        return interval.equals("") ? route : interval;
    }

    public String printDistanceTimePace() {
        return printDistance() + AppConsts.TAB + printTime() + AppConsts.TAB + printPace();
    }

    public String printCaption() {
        return printDate() + (interval.equals("") ? "" : " • " + route);
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Exerlite)) return false;
        Exerlite e = (Exerlite) item;
        return e.has_id(_id);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Exerlite)) return false;
        Exerlite e = (Exerlite) item;
        return e.has_id(_id) && date.isEqual(e.getDate()) && route.equals(e.getRoute()) &&
                distance == e.getDistance() && time == e.getTime() && e.isTop(top);
    }

}
