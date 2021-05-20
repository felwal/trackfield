package com.example.trackfield.utils.model;

public class SwitchItem {

    private String key;
    private String text;
    private boolean checked;

    //

    public SwitchItem(String key, String text, boolean checked) {
        this.key = key;
        this.text = text;
        this.checked = checked;
    }

    // set

    public void setKey(String key) {
        this.key = key;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    // get

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    public boolean isChecked() {
        return checked;
    }

}
