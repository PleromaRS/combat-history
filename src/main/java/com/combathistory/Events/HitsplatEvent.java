package com.combathistory.Events;

import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;

public class HitsplatEvent extends RawEvent {
    private final NPC npc;
    private final Hitsplat hitsplat;

    public HitsplatEvent(NPC npc, Hitsplat hitsplat) {
        this.npc = npc;
        this.hitsplat = hitsplat;
    }

    public NPC getNpc() {
        return npc;
    }

    public Hitsplat getHitsplat() {
        return hitsplat;
    }
}