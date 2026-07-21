package com.combathistory.Events;

import net.runelite.api.NPC;

public class AnimationEvent extends RawEvent {
    private final NPC npc;
    private final int animationId;

    public AnimationEvent(NPC npc, int animationId) {
        this.npc = npc;
        this.animationId = animationId;
    }

    public NPC getNpc() {
        return npc;
    }

    public int getAnimationId() {
        return animationId;
    }
}