package com.felwal.trackfield.ui.common.model;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.model.SortMode;

import java.util.Arrays;

public class Sorter extends RecyclerItem {

    private static final char[] ARROWS = { '↓', '↑' };

    private final SortMode[] sortModes;
    private int selectedIndex = 0;
    private boolean orderInverted = false;

    //

    public Sorter(@NonNull SortMode... sortModes) throws ArrayIndexOutOfBoundsException {
        if (sortModes.length == 0) throw new ArrayIndexOutOfBoundsException();
        this.sortModes = sortModes;
    }

    /**
     * Create a copy of this. Pass this to recycler itemlists,
     * in order for {@link #sameContentAs(RecyclerItem)} to work.
     *
     * @return A new Sorter
     */
    public Sorter copy() {
        Sorter copy = new Sorter(sortModes);
        copy.setSelection(selectedIndex, orderInverted);
        return copy;
    }

    // set

    /**
     * Sets selected index, and inverts order if already selected.
     * Should be called in {@link com.felwal.trackfield.ui.widget.sheet.SortSheet#onDismiss(DialogInterface)}
     */
    public void select(int index) {
        if (index >= sortModes.length) return;
        if (selectedIndex == index) orderInverted = !orderInverted;
        else {
            selectedIndex = index;
            orderInverted = false;
        }
    }

    public void setSelection(int selectedIndex, boolean orderInverted) {
        this.selectedIndex = selectedIndex;
        this.orderInverted = orderInverted;
    }

    // get

    public String[] getTitles() {
        String[] titles = new String[sortModes.length];
        for (int i = 0; i < sortModes.length; i++) {
            titles[i] = i == selectedIndex ? getTitle() : sortModes[i].getTitle();
        }
        return titles;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isOrderInverted() {
        return orderInverted;
    }

    // get selected

    private SortMode getSortMode() {
        return sortModes[selectedIndex];
    }

    public SortMode.Mode getMode() {
        return getSortMode().getMode();
    }

    public boolean isAscending() {
        return orderInverted ? !getSortMode().isAscendingByDefault() : getSortMode().isAscendingByDefault();
    }

    public String getTitle() {
        return getSortMode().getTitle() + " " + ARROWS[MathUtils.heaviside(isAscending())];
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Sorter)) return false;
        Sorter other = (Sorter) item;
        return Arrays.equals(sortModes, other.sortModes);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Sorter)) return false;
        Sorter other = (Sorter) item;
        return selectedIndex == other.selectedIndex && orderInverted == other.orderInverted;
    }

}
