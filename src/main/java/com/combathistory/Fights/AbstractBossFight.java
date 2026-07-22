package com.combathistory.Fights;


import com.combathistory.Events.*;
import com.combathistory.Reporting.TickReport;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;

import java.util.*;

public abstract class AbstractBossFight {

    // ----- STATE -----
    // Has the fight ended?
    protected boolean fightEnded = false;

    // Tracks all currently alive NPCs (Boss + Minions). Key = NPC Index, Value = NPC ID
    protected Map<Integer, Integer> activeNpcs = new HashMap<>();

    // Buffers raw transient events (hitsplats, animations, projectiles) that happen between game ticks
    protected List<RawEvent> eventBuffer = new ArrayList<>();

    // The report for the current tick, populated by the subclass
    protected TickReport currentTickReport;

    // Track currently active minions
    protected Set<NPC> activeMinions = new HashSet<>();

    // Track seen projectiles to prevent duplicate logging
    protected Set<Projectile> seenProjectiles = new HashSet<>();

    // Track currently present objects
    protected Set<GameObject> activeObjects = new HashSet<>();

    // Track current tick of the fight
    protected int currentTick;

    // ----- INPUT HOOKS (Called by main plugin) -----
    // These catch the transient events and dump into the buffer
    public void onAnimationChanged(NPC npc, int animationId) {
        if (isRelevantNpc(npc.getId())) {
            eventBuffer.add(new AnimationEvent(npc, animationId));
        }
    }

    public void onHitsplatApplied(NPC npc, Hitsplat hitsplat) {
        if (isRelevantNpc(npc.getId())) {
            eventBuffer.add(new HitsplatEvent(npc, hitsplat));
        }
    }

    public void onProjectileMoved(Projectile projectile) {
        if (isRelevantProjectile(projectile.getId()) && seenProjectiles.add(projectile)) {
            eventBuffer.add(new ProjectileEvent(projectile));
        }
    }

    public void onGameObjectSpawned(GameObject gameObject) {
        if (isRelevantObject(gameObject.getId()) && activeObjects.add(gameObject)) {
            eventBuffer.add(new ObjectSpawnEvent(gameObject));
        }
    }

    public void onGameObjectDespawned(GameObject gameObject) {
        if (isRelevantObject(gameObject.getId()) && activeObjects.remove(gameObject)) {
            eventBuffer.add(new ObjectDespawnEvent(gameObject));
        }
    }

    public void onNpcSpawned(NPC npc) {
        if (isRelevantNpc(npc.getId())) {
            if (!isBossNpc(npc.getId())) {
                activeMinions.add((npc));
            }
            eventBuffer.add(new NPCSpawnEvent(npc));
        }
    }

    public void onNpcDespawned(NPC npc) {
        if (isRelevantNpc(npc.getId())) {
            if (!isBossNpc(npc.getId())) {
                activeMinions.remove((npc));
            }
            eventBuffer.add(new NPCDespawnEvent(npc));
        }
    }

    public void onChatMessage(ChatMessageType type, String name, String message) {
        eventBuffer.add(new ChatMessageEvent(type, name, message));
    }

    // ----- LIFECYCLE -----
    public final void onGameTick(int currentTick, Client client) {
        // todo: Initialize a new report for this game tick
        this.currentTick = currentTick;
        processBufferedEvents();
        updatePolledState();
        buildTickReport();
        eventBuffer.clear();
    }

    // ----- RESETS -----
    protected void resetProjectileTracking() {
        seenProjectiles.clear();
    }

    protected void resetObjectTrackings() {
        activeObjects.clear();
    }

    protected void resetActiveMinions() {
        activeMinions.clear();
    }

    // ----- ABSTRACT METHODS (Implemented by specific boss subclass) -----
    protected abstract void processBufferedEvents();
    protected abstract void updatePolledState();
    protected abstract void buildTickReport();
    protected abstract boolean isRelevantNpc(int npcId);
    protected abstract boolean isRelevantProjectile(int projectileId);
    protected abstract  boolean isRelevantObject(int objectId);

    // ----- OUTPUT -----
    public TickReport getTickReport() {
        return currentTickReport;
    }

    // ----- BOSS DETECTION -----
    public abstract boolean isBossNpc(int npcId);
    public abstract String getBossName();

    public abstract boolean checkStartConditions(Client client);
    public abstract void init(Client client);
    public abstract void endFight(String durationMessage);

    public boolean isFightEnded() {
        return fightEnded;
    }
}
