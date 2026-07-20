package com.combathistory.model;

import net.runelite.api.Actor;

public class HitsplatData {
    private final boolean isIncoming;
    private final int amount;
    private final int type;
    private final Actor appliedTo;

    public HitsplatData(boolean isIncoming, int amount, int type, Actor appliedTo) {
        this.isIncoming = isIncoming;
        this.amount = amount;
        this.type = type;
        this.appliedTo = appliedTo;
    }

    public boolean isIncoming() { return isIncoming; }
    public int getAmount() { return amount; }
    public int getType() { return type; }
    public Actor getAppliedTo() { return appliedTo; }

    @Override
    public String toString() {
        String direction = isIncoming ? "Received" : "Dealt";
        return String.format("%s %d (%s)", direction, amount, type);
    }
}