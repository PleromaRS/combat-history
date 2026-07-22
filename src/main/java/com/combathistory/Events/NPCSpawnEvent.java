package com.combathistory.Events;

import net.runelite.api.NPC;

public class NPCSpawnEvent extends RawEvent {
    private final NPC npc;

    public NPCSpawnEvent(NPC npc) {
        this.npc = npc;
    }

    public NPC getNpc() { return npc; }
    public int getNpcId() { return npc.getId(); }
}