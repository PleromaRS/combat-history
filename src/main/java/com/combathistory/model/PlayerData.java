package com.combathistory.model;

import net.runelite.api.*;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private final Player localPlayer;
    private Actor targeting = null;
    private final List<NPC> targetedBy = new ArrayList<>();

    public PlayerData(Player localPlayer) {
        this.localPlayer = localPlayer;
    }

    public void updateTargets(Client client) {
        updatePlayerTarget();
        updateTargetedBy(client);
    }

    public Player getLocalPlayer() { return this.localPlayer; }

    public Actor getTargeting() { return this.targeting; }

    public String getTargetingString() {
        if (this.targeting == null) { return "None"; }
        return this.targeting.getName();
    }

    public List<NPC> getTargetedBy() { return this.targetedBy; }

    public String getTargetedByString() {
        if (this.targetedBy.isEmpty()) return "[]";

        List<String> npcNames = new ArrayList<>();
        for (NPC npc : this.targetedBy) {
            npcNames.add(npc.getName());
        }
        return npcNames.toString();
    }

    private void updatePlayerTarget() {
        this.targeting = localPlayer.getInteracting();
    }

    private void updateTargetedBy(Client client) {
        WorldView worldView = client.getTopLevelWorldView();
        IndexedObjectSet<? extends NPC> allNpcs = worldView.npcs();
        this.targetedBy.clear();
        for (NPC npc : allNpcs) {
            if (npc.getInteracting() == localPlayer) {
                targetedBy.add(npc);
            }
        }
    }
}
