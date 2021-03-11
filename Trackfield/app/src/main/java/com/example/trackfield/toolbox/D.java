package com.example.trackfield.toolbox;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.example.trackfield.items.Exerlite;
import com.example.trackfield.objects.Exercise;

import java.time.LocalDate;
import java.util.ArrayList;

// Data
public class D {

    @Deprecated
    public static ArrayList<Exercise> exercises = new ArrayList<>();
    @Deprecated
    public static ArrayList<Integer> distances = new ArrayList<>();
    @Deprecated
    public static ArrayList<String> routes = new ArrayList<>();

    // stats & more
    @Deprecated
    public static int totalDistance = 0;
    @Deprecated
    public static float totalTime = 0;
    @Deprecated
    public static boolean gameOn = false;

    // bort
    @Deprecated
    public static int[] weeks;
    @Deprecated
    public static int weekAmount = 12;
    @Deprecated
    public static int distanceTopX = 3;

    // theme

    public static boolean updateTheme(Activity a) {
        int newTheme = C.LOOKS[M.heaviside(Prefs.isThemeLight())][Prefs.getColor()];
        try {
            int currentTheme = a.getPackageManager().getActivityInfo(a.getComponentName(), 0).getThemeResource();
            if (currentTheme != newTheme) {
                a.setTheme(newTheme);
                return true;
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    // sort

    public static ArrayList<Exercise> sortExercises(ArrayList<Exercise> listToSort, boolean smallestFirst, C.SortMode sortBy) {

        ArrayList<Exercise> sorted = new ArrayList<>(listToSort);
        for (int i = sorted.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // get values
                float compareValue1 = 0;
                float compareValue2 = 0;
                switch (sortBy) {
                    case DATE: // date
                        compareValue1 = M.heaviside(sorted.get(j).getDate().isAfter(sorted.get(j + 1).getDate()));
                        compareValue2 = M.heaviside(sorted.get(j + 1).getDate().isAfter(sorted.get(j).getDate()));
                        break;
                    case DISTANCE:// distance
                        compareValue1 = sorted.get(j).distance();
                        compareValue2 = sorted.get(j + 1).distance();
                        break;
                    case TIME: // time
                        compareValue1 = sorted.get(j).time();
                        compareValue2 = sorted.get(j + 1).time();
                        break;
                    case PACE: // pace
                        compareValue1 = sorted.get(j).pace();
                        compareValue2 = sorted.get(j + 1).pace();
                        break;
                    default:
                        break;
                }

                // compare values
                boolean moveBack;
                if (smallestFirst) {
                    moveBack = compareValue1 > compareValue2;
                }
                else {
                    moveBack = compareValue1 < compareValue2;
                }

                // move back
                if (moveBack) {
                    Exercise temp = sorted.get(j + 1);
                    sorted.set(j + 1, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    public static ArrayList<Exercise> sortExercisesByDistance(ArrayList<Exercise> listToSort, boolean smallestFirst, C.SortMode sortBy) {

        ArrayList<Exercise> sorted = new ArrayList<>(listToSort);
        for (int i = sorted.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // get values
                float compareValue1 = 0;
                float compareValue2 = 0;
                switch (sortBy) {
                    case DATE: // date
                        compareValue1 = M.heaviside(sorted.get(j).getDate().isAfter(sorted.get(j + 1).getDate()));
                        compareValue2 = M.heaviside(sorted.get(j + 1).getDate().isAfter(sorted.get(j).getDate()));
                        break;
                    case DISTANCE: // full distance
                        compareValue1 = sorted.get(j).distance();
                        compareValue2 = sorted.get(j + 1).distance();
                        break;
                    case PACE: // time & pace (by distance)
                        //compareValue1 = sorted.get(j).getTimeByDistance(distance);
                        //compareValue2 = sorted.get(j+1).getTimeByDistance(distance);
                        compareValue1 = sorted.get(j).pace();
                        compareValue2 = sorted.get(j + 1).pace();
                        break;
                    default:
                        break;
                }

                // compare values
                boolean moveBack;
                if (smallestFirst) {
                    moveBack = compareValue1 > compareValue2;
                }
                else {
                    moveBack = compareValue1 < compareValue2;
                }

                // move back
                if (moveBack) {
                    Exercise temp = sorted.get(j + 1);
                    sorted.set(j + 1, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    public static ArrayList<String> sortRoutes(ArrayList<String> listToSort, boolean smallestFirst, C.SortMode sortBy, boolean alwaysIncludeLesser) {

        ArrayList<String> sorted = new ArrayList<>(listToSort);
        for (int i = sorted.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                if (!alwaysIncludeLesser && !Prefs.areHiddenRoutesShown() && filterByRoute(sorted.get(j)).size() <= 1) {
                    sorted.remove(j);
                    j--;
                    i--; // räcker inte
                    continue;
                }

                // get values
                float compareValue1 = 0;
                float compareValue2 = 0;
                switch (sortBy) {
                    case NAME: // name
                        compareValue1 = (float) M.sortValue(sorted.get(j));
                        compareValue2 = (float) M.sortValue(sorted.get(j + 1));
                        break;
                    case AMOUNT: // amount
                        compareValue1 = filterByRoute(sorted.get(j)).size();
                        compareValue2 = filterByRoute(sorted.get(j + 1)).size();
                        break;
                    case PACE: // best pace
                        compareValue1 = fastestPace(filterByRoute(sorted.get(j))).pace();
                        compareValue2 = fastestPace(filterByRoute(sorted.get(j + 1))).pace();
                        break;
                    case DISTANCE: // average distance
                        compareValue1 = averageDistance(filterByRoute(sorted.get(j)));
                        compareValue2 = averageDistance(filterByRoute(sorted.get(j + 1)));
                        break;
                    case DATE: // recent
                        boolean jBigger = sortExercises(filterByRoute(sorted.get(j)), false, C.SortMode.DATE).get(0).getDate().
                                isAfter(sortExercises(filterByRoute(sorted.get(j + 1)), false, C.SortMode.DATE).get(0).getDate());
                        compareValue1 = M.heaviside(jBigger);
                        compareValue2 = M.heaviside(!jBigger);
                        break;
                    default:
                        break;
                }

                // compare values
                boolean moveBack;
                if (smallestFirst) {
                    moveBack = compareValue1 > compareValue2;
                }
                else {
                    moveBack = compareValue1 < compareValue2;
                }

                // move back
                if (moveBack) {
                    String temp = sorted.get(j + 1);
                    sorted.set(j + 1, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    public static ArrayList<Integer> sortDistances(ArrayList<Integer> listToSort, boolean smallestFirst, C.SortMode sortBy) {

        ArrayList<Integer> sorted = new ArrayList<>(listToSort);
        for (int i = sorted.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // get values
                float compareValue1 = 0;
                float compareValue2 = 0;
                switch (sortBy) {
                    case DISTANCE: // distance
                        compareValue1 = sorted.get(j);
                        compareValue2 = sorted.get(j + 1);
                        break;
                    case AMOUNT: // amount
                        compareValue1 = filterByDistance(sorted.get(j)).size();
                        compareValue2 = filterByDistance(sorted.get(j + 1)).size();
                        break;
                    case TIME: // best time
                        compareValue1 = shortestTime(filterByDistance(sorted.get(j)), sorted.get(j)).timeByDistance(sorted.get(j));
                        compareValue2 = shortestTime(filterByDistance(sorted.get(j + 1)), sorted.get(j + 1)).timeByDistance(sorted.get(j + 1));
                        break;
                    case PACE: // best pace
                        compareValue1 = fastestPace(filterByDistance(sorted.get(j))).pace();
                        compareValue2 = fastestPace(filterByDistance(sorted.get(j + 1))).pace();
                        break;
                    default:
                        break;
                }

                // compare values
                boolean moveBack;
                if (smallestFirst) {
                    moveBack = compareValue1 > compareValue2;
                }
                else {
                    moveBack = compareValue1 < compareValue2;
                }

                // move back
                if (moveBack) {
                    int temp = sorted.get(j + 1);
                    sorted.set(j + 1, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    public static void sortExerlites(ArrayList<Exerlite> listToSort, C.SortMode sortMode, boolean smallestFirst) {

        for (int i = listToSort.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // get values
                float compareValue1 = 0;
                float compareValue2 = 0;
                switch (sortMode) {
                    case DATE: // date
                        compareValue1 = M.heaviside(listToSort.get(j).getDate().isAfter(listToSort.get(j + 1).getDate()));
                        compareValue2 = M.heaviside(listToSort.get(j + 1).getDate().isAfter(listToSort.get(j).getDate()));
                        break;
                    case DISTANCE:// distance
                        compareValue1 = listToSort.get(j).getDistance();
                        compareValue2 = listToSort.get(j + 1).getDistance();
                        break;
                    case TIME: // time
                        compareValue1 = listToSort.get(j).getTime();
                        compareValue2 = listToSort.get(j + 1).getTime();
                        break;
                    case PACE: // pace
                        compareValue1 = listToSort.get(j).getPace();
                        compareValue2 = listToSort.get(j + 1).getPace();
                        break;
                    default:
                        break;
                }

                // move back
                boolean moveBack = smallestFirst ? compareValue1 > compareValue2 : compareValue1 < compareValue2;
                ;
                if ((sortMode == C.SortMode.PACE || sortMode == C.SortMode.TIME)) {
                    if (compareValue2 == 0) moveBack = false;
                    else if (compareValue1 == 0) moveBack = true;
                }
                if (moveBack) {
                    Exerlite temp = listToSort.get(j + 1);
                    listToSort.set(j + 1, listToSort.get(j));
                    listToSort.set(j, temp);
                }
            }
        }
    }

    // filter

    @Deprecated
    public static ArrayList<Exercise> filterByRoute(String route) {

        ArrayList<Exercise> filtered = new ArrayList<>();
        for (Exercise e : exercises) {
            if (e.getRoute().equalsIgnoreCase(route)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByRoute(String route, String routeVar) {

        ArrayList<Exercise> filtered = new ArrayList<>();
        for (Exercise e : exercises) {
            if (e.getRoute().equalsIgnoreCase(route) && e.getRouteVar().equalsIgnoreCase(routeVar)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByInterval(String interval) {

        ArrayList<Exercise> filtered = new ArrayList<>();
        for (Exercise e : exercises) {
            if (e.getInterval().equalsIgnoreCase(interval)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByDistance(int distance) {

        ArrayList<Exercise> filteredByMin = filterByMinDistance(distance);
        ArrayList<Exercise> top10 = new ArrayList<>(sortExercises(filteredByMin, true, C.SortMode.PACE).subList(0, Math.min(distanceTopX, filteredByMin.size())));
        ArrayList<Exercise> filtered = new ArrayList<>(top10);
        for (Exercise e : exercises) {
            int eDistance = e.distance();
            if (M.insideLimits(eDistance, distance) && !filtered.contains(e)) filtered.add(e);
        }

        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByDistance(int distance, int type) {

        ArrayList<Exercise> filteredByMin = filterByMinDistance(distance);
        ArrayList<Exercise> top10 = filterByType(new ArrayList<>(sortExercises(filteredByMin, true, C.SortMode.PACE).subList(0, Math.min(distanceTopX, filteredByMin.size()))), type);
        ArrayList<Exercise> filtered = new ArrayList<>(top10);
        for (Exercise e : exercises) {
            int eDistance = e.distance();
            if (M.insideLimits(eDistance, distance) && !filtered.contains(e) && e.isType(type))
                filtered.add(e);
        }

        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByMinDistance(int distance) {

        ArrayList<Exercise> filtered = new ArrayList<>();
        for (Exercise e : exercises) {
            if (e.distance() >= distance) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterByType(ArrayList<Exercise> list, int type) {

        ArrayList<Exercise> filtered = new ArrayList<>();
        for (Exercise e : list) {
            if (e.getType() == type) {
                filtered.add(e);
            }
        }

        return filtered;
    }

    @Deprecated
    public static ArrayList<Exercise> filterBySearch(String search) {

        search = search.toLowerCase();
        ArrayList<Exercise> filtered = new ArrayList<>();

        for (Exercise e : exercises) {
            if (e.getRoute().toLowerCase().contains(search) || e.getRouteVar().toLowerCase().contains(search) || e.getNote().toLowerCase().contains(search) ||
                    Exercise.TYPES[e.getType()].toLowerCase().contains(search) || e.getDataSource().toLowerCase().contains(search) || e.getRecordingMethod().toLowerCase().contains(search) ||
                    e.getDate().format(C.FORMATTERS_SEARCH[0]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[1]).toLowerCase().contains(search) ||
                    e.getDate().format(C.FORMATTERS_SEARCH[2]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[3]).toLowerCase().contains(search) ||
                    e.getDate().format(C.FORMATTERS_SEARCH[4]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[5]).toLowerCase().contains(search) ||
                    e.getDate().format(C.FORMATTERS_SEARCH[6]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[7]).toLowerCase().contains(search)) {
                filtered.add(e);
            }
        }

        return filtered;
    }

    // compare

    @Deprecated
    public static Exercise shortestTime(ArrayList<Exercise> list, int distance) {

        Exercise shortest = list.get(0);
        for (Exercise e : list) {
            if (e.timeByDistance(distance) < shortest.timeByDistance(distance) && e.timeByDistance(distance) != 0) {
                shortest = e;
            }
        }
        return shortest;
    }

    @Deprecated
    public static Exercise fastestPace(ArrayList<Exercise> list) {

        Exercise fastest = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).pace() != 0) {
                fastest = list.get(i);
            }
        }

        for (Exercise e : list) {
            if (e.pace() < fastest.pace() && e.pace() != 0) {
                fastest = e;
            }
        }
        return fastest;
    }

    @Deprecated
    public static int averageDistance(ArrayList<Exercise> list) {

        int totalDistance = 0;
        int count = 0;
        for (Exercise e : list) {
            if (e.isDistanceDriven()) continue;
            totalDistance += e.distance();
            count++;
        }
        if (count == 0) {
            return 0;
        }
        return totalDistance / count;
    }

    @Deprecated
    public static int longestDistance(ArrayList<Exercise> list) {

        int longestDistance = 0;
        for (Exercise e : list) {
            int distance = e.distance();
            if (distance > longestDistance) {
                longestDistance = distance;
            }
        }

        return longestDistance;
    }

    // tools

    /**
     * TODO: inline in getter functions of list
     */
    @Deprecated
    public static void markTop(ArrayList<Exerlite> list) {
        if (list.size() == 0) return;

        // get top
        int[] top = {-1, -1, -1};
        for (int i = 0; i < list.size(); i++) {
            Exerlite e = list.get(i);
            float pace = e.getPace();
            if (pace == 0) continue;

            if (top[0] == -1 || pace < list.get(top[0]).getPace()) {
                top[2] = top[1];
                top[1] = top[0];
                top[0] = i;
            }
            else if (top[1] == -1 || pace < list.get(top[1]).getPace()) {
                top[2] = top[1];
                top[1] = i;
            }
            else if (top[2] == -1 || pace < list.get(top[2]).getPace()) {
                top[2] = i;
            }
        }

        // mark
        if (top[2] != -1) list.get(top[2]).setTop(3);
        if (top[1] != -1) list.get(top[1]).setTop(2);
        if (top[0] != -1) list.get(top[0]).setTop(1);
    }

    /**
     * Removes all exercises longer than maxDist, except those classifying in top three
     *
     * @param list List to filter
     * @param maxDist Inclusive limit
     */
    @Deprecated
    public static void removeLonger(ArrayList<Exerlite> list, int maxDist) {

        // separate
        ArrayList<Exerlite> longer = new ArrayList<>();
        for (Exerlite e : list) if (e.getDistance() > maxDist) longer.add(e);
        list.removeAll(longer);

        // sort
        sortExerlites(list, C.SortMode.PACE, true);
        sortExerlites(longer, C.SortMode.PACE, true);

        // get top
        Exerlite[] top = {null, null, null};
        for (int i = 0; i < 3 && i < list.size(); i++) {
            Exerlite e = list.get(i);
            if (e.getPace() == 0) continue;
            top[i] = e;
        }
        for (int i = 0; i < 3 && i < longer.size(); i++) {
            Exerlite e = longer.get(i);
            if (e.getPace() == 0) continue;

            if (top[0] == null || e.getPace() < top[0].getPace()) {
                top[2] = top[1];
                top[1] = top[0];
                top[0] = e;
            }
            else if (top[1] == null || e.getPace() < top[1].getPace()) {
                top[2] = top[1];
                top[1] = e;
            }
            else if (top[2] == null || e.getPace() < top[2].getPace()) {
                top[2] = e;
            }
        }

        // readd
        if (top[0] != null && !list.contains(top[0])) list.add(top[0]);
        if (top[1] != null && !list.contains(top[1])) list.add(top[1]);
        if (top[2] != null && !list.contains(top[2])) list.add(top[2]);
    }

    // update data

    @Deprecated
    public static void addDistance(int d) {
        if (!distances.contains(d)) {
            distances.add(d);
            sortDistancesData();
        }
    }

    @Deprecated
    public static void sortRoutesData() {

        //trimRoutes();

        // bubblesort
        for (int i = routes.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // consider length
                int charIndex = 0;
                int shortestLength = routes.get(j).length();
                if (routes.get(j + 1).length() < routes.get(j).length()) {
                    shortestLength = routes.get(j + 1).length();
                }
                while (charIndex < shortestLength - 1 && routes.get(j).charAt(charIndex) == routes.get(j + 1).charAt(charIndex)) {
                    charIndex++;
                }

                // move back j
                if (routes.get(j).charAt(charIndex) > routes.get(j + 1).charAt(charIndex) || (charIndex == shortestLength - 1 && routes.get(j).length() > routes.get(j + 1).length())) {
                    String temp = routes.get(j + 1);
                    routes.set(j + 1, routes.get(j));
                    routes.set(j, temp);
                }
            }
        }
    }

    @Deprecated
    public static void sortDistancesData() {

        for (int i = distances.size() - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // move back
                if (distances.get(j) > distances.get(j + 1)) {
                    int temp = distances.get(j + 1);
                    distances.set(j + 1, distances.get(j));
                    distances.set(j, temp);
                }
            }
        }
    }

    @Deprecated
    public static void importRoutes() {

        routes.clear();
        for (Exercise e : exercises) {
            if (e.getType() == 0 && !routes.contains(e.getRoute())) {
                routes.add(e.getRoute());
            }
        }
    }

    // charts
    
    @Deprecated
    public static float[] weekDailyDistance() {

        float[] distances = {0, 0, 0, 0, 0, 0, 0};
        LocalDate now = LocalDate.now();
        int week = now.get(C.WEEK_OF_YEAR);

        for (Exercise e : sortExercises(exercises, false, C.SortMode.DATE)) {
            if (e.getWeek() != week) {
                break;
            }
            distances[e.getDate().get(C.DAY_OF_WEEK) - 1] += e.distance();
        }
        return distances;
    }

    @Deprecated
    public static float[] yearMonthlyDistance(int year) {

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year + 1, 1, 1).minusDays(1);

        float[] distances = new float[12];

        for (Exercise e : sortExercises(exercises, true, C.SortMode.DATE)) {
            LocalDate date = e.getDate();
            if (date.isBefore(startDate)) {
                continue;
            }
            if (date.isAfter(endDate)) {
                break;
            }
            distances[date.getMonthValue() - 1] += e.distance();
        }
        return distances;
    }

}
