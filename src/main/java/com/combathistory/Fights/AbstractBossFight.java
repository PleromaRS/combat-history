package com.combathistory.Fights;


import com.combathistory.Events.RawEvent;
import com.combathistory.Events.AnimationEvent;
import com.combathistory.Events.HitsplatEvent;
import com.combathistory.Events.ProjectileEvent;
import com.combathistory.Reporting.TickReport;

import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBossFight {

    // ----- STATE -----

    // Tracks all currently alive NPCs (Boss + Minions). Key = NPC Index, Value = NPC ID
    protected Map<Integer, Integer> activeNpcs = new HashMap<>();

    // Buffers raw transient events (hitsplats, animations, projectiles) that happen between game ticks
    protected List<RawEvent> eventBuffer = new ArrayList<>();

    // The report for the current tick, populated by the subclass
     protected TickReport currentTickReport;

    // ----- INPUT HOOKS (Called by main plugin) -----
    // These catch the transient events and dump into the buffer
    public void onAnimationChanged(NPC npc, int animationId) {
        eventBuffer.add(new AnimationEvent(npc, animationId));
    }

    public void onHitsplatApplied(NPC npc, Hitsplat hitsplat) {
        eventBuffer.add(new HitsplatEvent(npc, hitsplat));
    }

    public void onProjectileSpawned(Projectile projectile) {
        eventBuffer.add(new ProjectileEvent(projectile));
    }

    // ----- LIFECYCLE (Called by main plugin each game tick) -----
    public final void onGameTick(int currentTick) {
        // Initialize a new report for this game tick
        // currentTickReport = new TickReport(currentTick);

        // Subclass processes the transient events from the buffer
        processBufferedEvents();

        // Subclass checks persistent state (HP, Location, etc.)
        updatePolledState();

        // Subclass combines everything into the final readable report
        buildTickReport();

        // Clear the buffer for the next tick
        eventBuffer.clear();
    }

    // ----- ABSTRACT METHODS (Implemented by specific boss subclass) -----
    // Look through event buffer, translate IDs using private enums, figure out what transient actions occurred this tick
    protected abstract void processBufferedEvents();

    // Poll the game client for persistent states (HP, location etc.), active minions, and current phase.
    protected abstract void updatePolledState();

    // Take the data gathered from the buffer and the polled state and format it into the currentTickReport.
    protected abstract void buildTickReport();

    // ----- OUTPUT -----
    public TickReport getTickReport() {
        return currentTickReport;
    }

    // ----- BOSS DETECTION -----
    // Returns true if given NPC ID is a boss form. Used by main plugin to detect when to start tracking a fight.
    public abstract boolean isBossNpc(int npcId);

    // Returns the name of the boss for logging purposes
    public abstract String getBossName();

    // Called by the main plugin when the boss NPC spawns. Used to capture the NPC reference for polling and reset internal state.
    public abstract void init(NPC bossNpc);

    // Called to end a fight when the death animation is detected
    public abstract void endFight();
}
