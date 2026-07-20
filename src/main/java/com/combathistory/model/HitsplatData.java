package com.combathistory.model;

public class HitsplatData {
    private final boolean isIncoming;
    private final int amount;
    private final int type;

    public HitsplatData(boolean isIncoming, int amount, int type) {
        this.isIncoming = isIncoming;
        this.amount = amount;
        this.type = type;
    }

    public boolean isIncoming() { return isIncoming; }
    public int getAmount() { return amount; }
    public int getType() { return type; }

    @Override
    public String toString() {
        String direction = isIncoming ? "Received" : "Dealt";
        return String.format("%s %d (%s)", direction, amount, type);
    }
}
