package com.combathistory.model;

import net.runelite.api.*;

public class PlayerData {
    private final Player localPlayer;
    private Actor targeting = null;

    public PlayerData(Player localPlayer) {
        this.localPlayer = localPlayer;
    }

    public void update() {
        updatePlayerTarget();
    }

    public Player getLocalPlayer() { return this.localPlayer; }

    public Actor getTargeting() { return this.targeting; }

    private void updatePlayerTarget() {
        this.targeting = localPlayer.getInteracting();
    }

}
