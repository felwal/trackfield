package com.felwal.trackfield.utils;

import com.felwal.trackfield.data.prefs.Prefs;

import java.math.BigDecimal;

// Math
public final class MathUtils {

    public static final double GOLDEN_RATIO = 1.618033988749894;

    //

    private MathUtils() {
        // this utility class is not publicly instantiable
    }

    // real maths

    public static double sqr(double d) {
        return d * d;
    }

    public static int heaviside(float x) {
        return x <= 0 ? 0 : 1;
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

    public static String roundToString(float f, int decimals) {
        float rounded = round(f, decimals);
        return decimals == 0 ? Integer.toString((int) rounded) : Float.toString(rounded);
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

    public static String prefix(float value, int decimals, boolean hideTrailingZeros, String unit) {
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
        integer |= hideTrailingZeros && value % 1f == 0;

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
            return AppConsts.NO_VALUE_TIME;
        }

        float[] parts = getTimeParts(timeToString);
        float seconds = parts[0];
        int minutes = (int) parts[1];
        int hours = (int) parts[2];

        String hh = Integer.toString(hours);
        StringBuilder mm = new StringBuilder(Integer.toString(minutes));
        String ssInt = Integer.toString((int) seconds);
        StringBuilder ss;

        if (round || seconds == (int) seconds) {
            ss = new StringBuilder(Integer.toString((int) seconds));
        }
        else {
            ss = new StringBuilder(Float.toString(round(seconds, 2)));
        }

        if (hours == 0) {
            hh = "";
            if (minutes == 0) {
                mm = new StringBuilder();
                if (seconds == 0) {
                    ss = new StringBuilder("0");
                }
                //else { ss = ss + " s"; }
            }
            else {
                if (ssInt.length() < 2) {
                    ss.insert(0, "0");
                }
                mm.append(":");
            }
        }
        else {
            while (mm.length() < 2) {
                mm.insert(0, "0");
            }
            while (ss.length() < 2) {
                ss.insert(0, "0");
            }
            hh += ":";
            mm.append(":");
        }

        return hh + mm + ss;
    }

    public static String hours(int seconds) {
        return round((float) seconds / 3600, 1) + " h";
    }

    public static float seconds(int hours, int minutes, float seconds) {
        return hours * 3600 + minutes * 60 + seconds;
    }

    // convert / compute

    public static double arrayAvg(double[] arr, double ignoreIfEqualTo) {
        int length = arr.length;

        double total = 0;
        for (double d : arr) {
            if (d == ignoreIfEqualTo) length--;
            else total += d;
        }
        return length == 0 ? 0 : total / length;
    }

    public static float[] getTimeParts(float timeToSplit) {
        float seconds;
        int minutes;
        int hours;

        minutes = (int) (timeToSplit / 60);
        seconds = timeToSplit % 60;
        hours = minutes / 60;
        minutes = minutes % 60;

        return new float[] { seconds, minutes, hours };
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
        return distance > MathUtils.minDistance(fitsInside) && (includeLonger || distance < MathUtils.maxDistance(
            fitsInside));
    }

}
