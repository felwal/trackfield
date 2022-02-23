package me.felwal.trackfield.ui.common.model;

public class HeaderValue {

    private final String unit;
    private final int decimals;
    private float value = 0;

    //

    public HeaderValue(String unit, int decimals) {
        this.unit = unit;
        this.decimals = decimals;
    }

    // set

    public void addValue(float value) {
        this.value += value;
    }

    // get

    public String getUnit() {
        return unit;
    }

    public int getDecimals() {
        return decimals;
    }

    public float getValue() {
        return value;
    }

    // extends Object

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof HeaderValue)) return false;
        HeaderValue other = (HeaderValue) o;
        return unit.equals(other.unit) && decimals == other.decimals && value == other.value;
    }

}
