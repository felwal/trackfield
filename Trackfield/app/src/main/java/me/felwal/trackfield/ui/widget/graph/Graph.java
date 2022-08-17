package me.felwal.trackfield.ui.widget.graph;

import java.util.ArrayList;
import java.util.Arrays;

import me.felwal.trackfield.ui.common.model.RecyclerItem;

public class Graph extends RecyclerItem {

    private final ArrayList<Axis> axes = new ArrayList<>();

    private float start = 0f;
    private float end = 0f;

    private final Borders borders;
    private final boolean[] grids = new boolean[2];

    private final boolean widthFixed;

    //

    public Graph(boolean xGrid, Borders borders, boolean widthFixed) {
        grids[0] = xGrid;
        this.borders = borders;
        this.widthFixed = widthFixed;
    }

    // set

    public void addAxis(Axis axis) {
        updateDomain(axis);
        axes.add(0, axis);
    }

    private void updateDomain(Axis newAxis) {
        if (!axes.isEmpty()) {
            start = Math.min(start, newAxis.getStart());
            end = Math.max(end, newAxis.getEnd());
        }
        else {
            start = newAxis.getStart();
            end = newAxis.getEnd();
        }
    }

    // get data

    public ArrayList<Axis> getAxes() {
        return axes;
    }

    public boolean hasData() {
        for (Axis axis : axes) {
            if (axis.getHasData()) return true;
        }
        return false;
    }

    public boolean hasMoreThanOnePoint() {
        for (Axis axis : axes) {
            if (axis.getHasMoreThanOnePoint()) return true;
        }
        return false;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    public float getDomainSize() {
        return end - start;
    }

    // get canvas properties

    public boolean isWidthFixed() {
        return widthFixed;
    }

    public boolean isXGridShown() {
        return grids[0];
    }

    public boolean isYGridShown() {
        return grids[1];
    }

    public Borders getBorders() {
        return borders;
    }

    // compare

    private boolean sameArgsAs(Graph graph) {
        if (axes.size() != graph.axes.size()) return false;

        for (int i = 0; i < axes.size(); i++) {
            if (!axes.get(i).sameArgsAs(graph.axes.get(i))) {
                return false;
            }
        }

        return Arrays.equals(grids, graph.grids) && borders.equals(graph.borders) && widthFixed == graph.widthFixed;
    }

    private boolean sameDataAs(Graph graph) {
        if (axes.size() != graph.axes.size()) return false;

        for (int i = 0; i < axes.size(); i++) {
            if (!axes.get(i).sameDataAs(graph.axes.get(i))) {
                return false;
            }
        }

        return true;
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Graph)) return false;
        return sameArgsAs((Graph) item) && item.hasTag(tag);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        return sameItemAs(item) && sameDataAs((Graph) item);
    }

}
