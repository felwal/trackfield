package com.example.trackfield.utils;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.TreeMap;

public class TypeUtils {

    // strings

    public static String notateDriven(String s) {
        return "( " + s + " )";
    }

    public static String truncateString(String s, int maxLength) {
        return s.length() < maxLength ? s : s.substring(0, maxLength);
    }

    public static String toSentenceCase(String s) {
        if (s == null || s.length() == 0) return "";

        String first = s.substring(0, 1).toUpperCase();
        String rest = s.substring(1);
        return first + rest;
    }

    public static String toWordCase(String s) {
        if (s == null || s.length() == 0) return "";

        String[] words = s.split(" ");
        StringBuilder cased = new StringBuilder();

        for (String word : words) {
            cased.append(toSentenceCase(word)).append(" ");
        }

        return cased.toString().trim();
    }

    //

    public static boolean intToBool(int i) {
        return i != 0;
    }

    public static int shortenInt(int i, int length) {
        if (Integer.toString(i).length() > length) {
            i = Integer.parseInt(Integer.toString(i).substring(0, length - 1));
        }
        return i;
    }

    // lists

    public static boolean treeMapsEquals(TreeMap<Float, Float> map1, TreeMap<Float, Float> map2) {
        if (map1.size() != map2.size()) return false;

        for (TreeMap.Entry<Float, Float> entry : map1.entrySet()) {
            float key = entry.getKey();
            if (!map2.containsKey(key) || map2.get(key).floatValue() != entry.getValue().floatValue()) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public static <T> ArrayList<T> createList(T valueToAdd) {
        ArrayList<T> list = new ArrayList<>();
        list.add(valueToAdd);
        return list;
    }

    // primitives / generic

    public static boolean[] castToPrimitive(Boolean[] genericArray) {
        boolean[] primitiveArray = new boolean[genericArray.length];
        for (int i = 0; i < genericArray.length; i++) {
            primitiveArray[i] = genericArray[i];
        }
        return primitiveArray;
    }

    public static Boolean[] castToGeneric(boolean[] primitiveArray) {
        Boolean[] genericArray = new Boolean[primitiveArray.length];
        for (int i = 0; i < primitiveArray.length; i++) {
            genericArray[i] = primitiveArray[i];
        }
        return genericArray;
    }

    // position

    public static LatLng toLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}
