package com.combathistory.model;

import net.runelite.api.*;

import java.util.ArrayList;
import java.util.List;

public class NpcData {
    private final WorldView worldView;
    private List<NPC> targetingPlayer = new ArrayList<>();

    public NpcData(Client client) {
        this.worldView = client.getTopLevelWorldView();
        update(client);
    }

    public void update(Client client) {
        IndexedObjectSet<? extends NPC> allNpcs = worldView.npcs();
        if (!targetingPlayer.isEmpty()) targetingPlayer.clear();
        for (NPC npc : allNpcs) {
            if (npc.getInteracting() == client.getLocalPlayer()) {
                targetingPlayer.add(npc);
            }
        }
    }

    public List<NPC> getTargetingPlayer() { return targetingPlayer; }
}
