package com.example.trackfield.ui.custom.graph;

import androidx.annotation.Nullable;

public class Borders {

    private final boolean left;
    private final boolean right;
    private final boolean top;
    private final boolean bottom;

    //

    public Borders(boolean left, boolean right, boolean top, boolean bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public static Borders all() {
        return new Borders(true, true, true, true);
    }

    public static Borders none() {
        return new Borders(false, false, false, false);
    }

    public static Borders vertical() {
        return new Borders(true, true, false, false);
    }

    public static Borders horizontal() {
        return new Borders(false, false, true, true);
    }

    // get

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isBottom() {
        return bottom;
    }

    // extends Object

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Borders)) return false;
        Borders other = (Borders) obj;
        return left == other.left && right == other.right && top == other.top && bottom == other.bottom;
    }

}
