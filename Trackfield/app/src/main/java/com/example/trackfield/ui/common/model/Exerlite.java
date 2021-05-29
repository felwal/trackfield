package com.example.trackfield.ui.common.model;

import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.TypeUtils;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class Exerlite extends RecyclerItem {

    private static final int DISTANCE_DECIMALS = 1;

    private final int id;
    private final String type;
    private final LocalDate date;
    private final String route;
    private final String interval;
    private final int distance;
    private final float time;
    private final boolean distanceDriven;
    private int top = 0;

    //

    public Exerlite(int id, String type, LocalDate date, String route, String interval, int distance, float time,
        boolean distanceDriven) {

        this.id = id;
        this.type = type;
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

    public int getId() {
        return id;
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

    public boolean hasId(int id) {
        return this.id == id;
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

    private float getTimeByDistance(int distance) {
        return getPace() * distance / 1000;
    }

    public float getPace() {
        return distance != 0 ? time / distance * 1000 : 0;
    }

    public boolean isTop() {
        return top != 0;
    }

    public boolean isTop(int top) {
        return this.top == top;
    }

    // print

    private String printDate() {
        return LocalDate.now().getYear() == date.getYear()
            ? date.format(AppConsts.FORMATTER_CAPTION_NOYEAR)
            : date.format(AppConsts.FORMATTER_CAPTION);
    }

    private String printDistance() {
        String print = distance == 0 ? AppConsts.NO_VALUE : MathUtils.round(distance / 1000f, DISTANCE_DECIMALS) + "";
        return distanceDriven ? TypeUtils.notateDriven(print) : print;
    }

    private String printTime() {
        return MathUtils.stringTime(time, true);
    }

    private String printTimeByDistance(int distance) {
        String print = MathUtils.stringTime(getTimeByDistance(distance), true);
        return distance <= this.distance ? print : TypeUtils.notateDriven(print);
    }

    private String printPace() {
        return MathUtils.stringTime(getPace(), true);
    }

    public String printTitle() {
        return !interval.equals("") ? interval : !route.equals("") ? route : type;
    }

    public String printValues() {
        return printDistance() + AppConsts.TAB + printTime() + AppConsts.TAB + printPace();
    }

    public String printValues(int timeByDistance) {
        return printDistance() + AppConsts.TAB + printTimeByDistance(timeByDistance) + AppConsts.TAB + printPace();
    }

    public String printCaption() {
        return printDate() + (interval.equals("") ? "" : " • " + route);
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Exerlite)) return false;
        Exerlite other = (Exerlite) item;
        return other.hasId(id);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Exerlite)) return false;
        Exerlite other = (Exerlite) item;
        return other.hasId(id) && type.equals(other.type) && date.isEqual(other.getDate())
            && interval.equals(other.interval) && route.equals(other.getRoute()) && distance == other.getDistance()
            && time == other.getTime() && other.isTop(top);
    }

}
