package com.combathistory.model;

import net.runelite.api.Actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the state of the game on a single tick.
 * Will be expanded later to include active prayers, equipment, attack animations etc.
 */
public class TickRecord {
    private final int tickNumber;
    private final List<HitsplatData> hitsplats = new ArrayList<>();
    private final List<Actor> targetingPlayer = new ArrayList<>();

    public TickRecord(int tickNumber) {
        this.tickNumber = tickNumber;
    }

    public void addHitsplat(HitsplatData hitsplat) {
        this.hitsplats.add(hitsplat);
    }

    public int getTickNumber() { return tickNumber; }

    public List<HitsplatData> getHitsplats() {
        return Collections.unmodifiableList(hitsplats);
    }

    public boolean hasAction() {
        return !hitsplats.isEmpty();
    }
}
