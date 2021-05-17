package com.example.trackfield.utils.model;

import androidx.core.util.Pair;

import java.util.ArrayList;

public class PairList<F, S> {

    private final Pair<F, S>[] pairs;

    //

    public PairList(Pair<F, S>... pairs) {
        this.pairs = pairs;
    }

    // get

    public Pair<F, S>[] getPairs() {
        return pairs;
    }

    public Pair<F, S> getPair(int index) {
        return index < pairs.length ? pairs[index] : null;
    }

    public S getSecond(F first) {
        for (Pair<F, S> pair : pairs) {
            if (pair.first.equals(first)) return pair.second;
        }
        return null;
    }

    public F getFirst(S second) {
        for (Pair<F, S> pair : pairs) {
            if (pair.second.equals(second)) return pair.first;
        }
        return null;
    }

    public ArrayList<F> getFirsts() {
        ArrayList<F> firsts = new ArrayList<>();
        for (Pair<F, S> pair : pairs) {
            firsts.add(pair.first);
        }
        return firsts;
    }

    public ArrayList<S> getSeconds() {
        ArrayList<S> seconds = new ArrayList<>();
        for (Pair<F, S> pair : pairs) {
            seconds.add(pair.second);
        }
        return seconds;
    }

    // set

    public void setFirsts(ArrayList<F> firsts) {
        if (firsts.size() != pairs.length) return;
        for (int i = 0; i < pairs.length; i++) {
            Pair<F, S> newPair = new Pair<>(firsts.get(i), pairs[i].second);
            pairs[i] = newPair;
        }
    }

    public void setSeconds(ArrayList<S> seconds) {
        if (seconds.size() != pairs.length) return;
        for (int i = 0; i < pairs.length; i++) {
            Pair<F, S> newPair = new Pair<>(pairs[i].first, seconds.get(i));
            pairs[i] = newPair;
        }
    }

}
