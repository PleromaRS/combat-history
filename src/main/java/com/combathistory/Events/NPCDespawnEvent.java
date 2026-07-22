package com.combathistory.Events;

import net.runelite.api.NPC;

public class NPCDespawnEvent extends RawEvent {
    private final NPC npc;

    public NPCDespawnEvent(NPC npc) {
        this.npc = npc;
    }

    public NPC getNpc() { return npc; }
    public int getNpcId() { return npc.getId(); }
}