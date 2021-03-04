package com.example.trackfield.toolbox;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.TreeMap;

// Math
public class M {

    public static final double GOLDEN_RATIO = 1.618033988749894;

    // real maths
    public static double sqr(double d) {
        return d * d;
    }

    public static int heaviside(float x) {
        return x < 0 ? 0 : 1;
    }

    public static int heaviside(boolean one) {
        return one ? 1 : 0;
    }

    public static float nonNegative(float f) {
        return f * heaviside(f);
    }

    public static int signum(float x) {
        return x > 0 ? 1 : (x < 0 ? -1 : 0);
    }

    public static int signum(boolean positive) {
        return positive ? 1 : -1;
    }

    public static float round(float f, int decimals) {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static double round(double d, int decimals) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    // ratios

    public static int goldenRatioSmall(int of) {
        return (int) (of / (1 + GOLDEN_RATIO));
    }

    public static int goldenRatioLarge(int of) {
        return (int) (of * GOLDEN_RATIO / (1 + GOLDEN_RATIO));
    }

    // strings

    public static String prefix(float value, int decimals, String unit) {

        String prefix = "";
        boolean integer = false;

        if (value >= Math.pow(10, 12)) {
            value /= Math.pow(10, 12);
            prefix = "T";
        }
        else if (value >= Math.pow(10, 9)) {
            value /= Math.pow(10, 9);
            prefix = "G";
        }
        else if (value >= Math.pow(10, 6)) {
            value /= Math.pow(10, 6);
            prefix = "M";
        }
        else if (value >= Math.pow(10, 3)) {
            value /= Math.pow(10, 3);
            prefix = "k";
        }
        else integer = true;

        value = round(value, decimals);
        return (integer ? Integer.toString((int) value) : value) + " " + prefix + unit;
    }

    public static String bigPrefix(float value, int E, String unit) {

        String str = Float.toString(value);
        int decimalIndex = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '.') {
                decimalIndex = i;
                break;
            }
        }
        int size = str.substring(0, decimalIndex).length() + E;

        String prefix;
        if (size <= 3) {
            return value + " ";
        }
        else if (size <= 6) {
            prefix = "k";
        }
        else if (size <= 9) {
            prefix = "M";
        }
        else if (size <= 12) {
            prefix = "G";
        }
        else if (size <= 15) {
            prefix = "T";
        }
        else if (size <= 18) {
            prefix = "P";
        }
        else if (size <= 21) {
            prefix = "E";
        }
        else if (size <= 24) {
            prefix = "Z";
        }
        else if (size <= 27) {
            prefix = "Y";
        }
        else if (size <= 30) {
            prefix = "!";
        }
        else {
            prefix = "?";
        }

        String strTrunc = str.substring(0, decimalIndex) + str.substring(decimalIndex + 1);
        String strLong = strTrunc + "000";
        int preCommaLength = size % 3;
        if (preCommaLength == 0) {
            preCommaLength = 3;
        }
        String preComma = strLong.substring(0, preCommaLength);
        String postComma = strLong.substring(preCommaLength, preCommaLength + 2);

        return preComma + "." + postComma + " " + prefix + unit;
    }

    public static String kiloPrefix(float value, int decimals, String unit) {
        float rounded = round(value / 1000f, decimals);
        return decimals == 0 ? (int) rounded + " k" + unit : rounded + " k" + unit;
    }

    public static String timePrefix(int seconds) {
        return seconds < 60 ? seconds + " s" : seconds < 3600 ? seconds / 60 + " min" : hours(seconds);
    }

    public static String stringTime(float timeToString, boolean round) {

        if (timeToString == 0) {
            return C.NO_VALUE_TIME;
        }

        float[] parts = getTimeParts(timeToString);
        float seconds = parts[0];
        int minutes = (int) parts[1];
        int hours = (int) parts[2];

        String hh = Integer.toString(hours);
        String mm = Integer.toString(minutes);
        String ssInt = Integer.toString((int) seconds);
        String ss;

        if (round || seconds == (int) seconds) {
            ss = Integer.toString((int) seconds);
        }
        else {
            ss = Float.toString(round(seconds, 2));
        }

        if (hours == 0) {
            hh = "";
            if (minutes == 0) {
                mm = "";
                if (seconds == 0) {
                    ss = "0";
                }
                //else { ss = ss + " s"; }
            }
            else {
                if (ssInt.length() < 2) {
                    ss = "0" + ss;
                }
                mm += ":";
            }
        }
        else {
            while (mm.length() < 2) {
                mm = "0" + mm;
            }
            while (ss.length() < 2) {
                ss = "0" + ss;
            }
            hh += ":";
            mm += ":";
        }

        return hh + mm + ss;
    }

    public static String hours(int seconds) {
        return round((float) seconds / 3600, 1) + " h";
    }

    public static float seconds(int hours, int minutes, float seconds) {
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static String drive(String s) {
        return "( " + s + " )";
    }

    public static String truncateString(String s, int maxLength) {
        return s.length() < maxLength ? s : s.substring(0, maxLength);
    }

    // convert / compute

    public static boolean intToBool(int i) {
        return i != 0;
    }

    public static int shortenInt(int i, int length) {
        if (Integer.toString(i).length() > length) {
            i = Integer.valueOf(Integer.toString(i).substring(0, length - 1));
        }
        return i;
    }

    public static double arrayAvg(double[] arr, double ignoreIfEqualTo) {

        int length = arr.length;

        double total = 0;
        for (double d : arr) {
            if (d == ignoreIfEqualTo) length--;
            else total += d;
        }
        return length == 0 ? 0 : total / length;
    }

    public static double sortValue(String s) {

        double value = 0;
        for (int c = 0; c < s.length(); c++) {
            value += (int) s.toLowerCase().charAt(c) / Math.pow(1000, c);
        }

        return value;
    }

    public static float[] getTimeParts(float timeToSplit) {

        float seconds;
        int minutes;
        int hours;

        minutes = (int) (timeToSplit / 60);
        seconds = timeToSplit % 60;
        hours = minutes / 60;
        minutes = minutes % 60;

        return new float[]{seconds, minutes, hours};
    }

    // lists

    public static boolean treeMapsEquals(TreeMap<Float, Float> map1, TreeMap<Float, Float> map2) {

        for (TreeMap.Entry<Float, Float> entry : map1.entrySet()) {
            float key = entry.getKey();
            if (!map2.containsKey(key) || map2.get(key).floatValue() != entry.getValue().floatValue())
                return false;
        }
        return true;
    }

    @NonNull
    public static ArrayList<Integer> createList(int valueToAdd) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(valueToAdd);
        return list;
    }

    // distance

    public static int minDistance(int distance) {
        return (int) nonNegative(distance - Prefs.getDistanceLowerLimit());
    }

    public static int maxDistance(int distance) {
        return distance + Prefs.getDistanceUpperLimit();
    }

    public static boolean insideLimits(int distance, int fitsInside) {
        return distance > minDistance(fitsInside) && distance < maxDistance(fitsInside);
    }

    public static boolean insideLimits(int distance, int fitsInside, boolean includeLonger) {
        return distance > M.minDistance(fitsInside) && (includeLonger || distance < M.maxDistance(fitsInside));
    }

    // dates

    public static LocalDateTime dateTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(12, 0));
    }

    public static LocalDateTime truncateSecs(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    public static LocalDateTime ofEpoch(long seconds) {
        return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
    }

    public static long epoch(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
    }

    public static LocalDateTime first(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    public static LocalDateTime last(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    public static LocalDateTime atStartOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfWeek(LocalDate date) {
        return date.plusDays(7 - date.getDayOfWeek().getValue()).atTime(23, 59, 59);
    }

    public static LocalDateTime atStartOfMonth(LocalDate date) {
        return date.minusDays(date.getDayOfMonth() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfMonth(LocalDate date) {
        return date.plusDays(date.lengthOfMonth() - date.getDayOfMonth()).atTime(23, 59, 59);
    }

    public static LocalDateTime atStartOfYear(LocalDate date) {
        return date.minusDays(date.getDayOfYear() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfYear(LocalDate date) {
        return date.plusDays(date.lengthOfYear() - date.getDayOfYear()).atTime(23, 59, 59);
    }

    // map

    public static LatLng toLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}
