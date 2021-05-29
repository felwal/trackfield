package com.felwal.trackfield.utils.model;

import android.util.Log;

public class SwitchChain {

    private final SwitchItem[] items;

    //

    public SwitchChain(SwitchItem... items) {
        this.items = items;
    }

    // set

    public void setChecked(boolean[] checked) {
        for (int i = 0; i < items.length && i < checked.length; i++) {
            items[i].setChecked(checked[i]);
        }
    }

    // get

    public String[] getKeys() {
        String[] titles = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            titles[i] = items[i].getKey();
        }
        return titles;
    }

    public String[] getTexts() {
        String[] titles = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            titles[i] = items[i].getText();
        }
        return titles;
    }

    public boolean[] getChecked() {
        boolean[] titles = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            titles[i] = items[i].isChecked();
        }
        return titles;
    }

    // get item

    private SwitchItem getItem(String key) {
        for (SwitchItem item : items) {
            if (item.getKey().equals(key)) return item;
        }
        Log.w("SwitchChain", "item not found");
        return null;
    }

    public boolean isChecked(String key) {
        SwitchItem item = getItem(key);
        return item != null && item.isChecked();
    }

}
