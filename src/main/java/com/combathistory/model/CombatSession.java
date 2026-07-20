package com.combathistory.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatSession {
    private final int startTick;
    private int endTick;

    private final List<TickRecord> tickRecords = new ArrayList<>();

    public CombatSession(int startTick) {
        this.startTick = startTick;
    }

    public void addTickRecord(TickRecord record) {
        this.tickRecords.add(record);
    }

    public void setEndTick(int endTick) {
        this.endTick = endTick;
    }

    public int getStartTick() { return startTick; }
    public int getEndTick() { return endTick; }
    public int getDurationTicks() { return endTick - startTick - 14; }

    public List<TickRecord> getTickRecords() {
        return Collections.unmodifiableList(tickRecords);
    }
}
