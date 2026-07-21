package com.combathistory.tracker;

import com.combathistory.events.CombatEndedEvent;
import com.combathistory.events.CombatStartedEvent;
import com.combathistory.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

@Singleton
@Slf4j
public class CombatTracker {

    private static final int COMBAT_TIMEOUT_TICKS = 16;

    private final Client client;
    private final EventBus eventBus;

    private CombatSession currentSession;

    private PlayerData playerData = null;
    private NpcData npcData = null;

    private int ticksSinceLastAction = 0;

    private final List<HitsplatData> currentTickHitsplats = new ArrayList<>();

    @Inject
    public CombatTracker(Client client, EventBus eventBus) {
        this.client = client;
        this.eventBus = eventBus;
    }

    public void reset() {
        currentSession = null;
        ticksSinceLastAction = 0;
        currentTickHitsplats.clear();
        playerData = null;
        npcData = null;
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (client.getLocalPlayer() == null) { return; }
        if (this.playerData == null) { this.playerData = new PlayerData(client.getLocalPlayer()); }

        Actor actor = event.getActor();
        Hitsplat hitsplat = event.getHitsplat();

        boolean isIncoming = (actor == playerData.getLocalPlayer());
        boolean isOutgoing = (actor != playerData.getLocalPlayer()) && (hitsplat.isMine());

        if (isIncoming || isOutgoing) {
            if (currentSession == null) {
                registerCombatAction();
            }

            HitsplatData data = new HitsplatData(
                    isIncoming,
                    hitsplat.getAmount(),
                    hitsplat.getHitsplatType(),
                    event.getActor()
            );

            currentTickHitsplats.add(data);
            ticksSinceLastAction = 0;
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null) { reset(); return; }

        if (this.playerData == null) { this.playerData = new PlayerData(localPlayer); }
        if (this.npcData == null) { this.npcData = new NpcData(client); }

        playerData.update();
        npcData.update(client);

        Actor myTarget = playerData.getTargeting();
        boolean iAmAttacking = (myTarget != null && myTarget.getHealthRatio() != -1);

        boolean iAmBeingAttacked = false;

        for (NPC npc: npcData.getTargetingPlayer()) {
            if (npc.getHealthRatio() != -1) {
                iAmBeingAttacked = true;
                break;
            }
        }

        boolean isActivelyInCombat = iAmAttacking || iAmBeingAttacked;

        if (currentSession == null && isActivelyInCombat) {
            registerCombatAction();
        }

        if (currentSession == null) { currentTickHitsplats.clear(); return; }

        ticksSinceLastAction++;

        int relativeTick = client.getTickCount() - currentSession.getStartTick() + 1;

        logGameTick(relativeTick, currentTickHitsplats);

        TickRecord record = new TickRecord(client.getTickCount());
        for (HitsplatData h : currentTickHitsplats) {
            record.addHitsplat(h);
        }

        currentSession.addTickRecord(record);


        currentTickHitsplats.clear();

        if (ticksSinceLastAction >= COMBAT_TIMEOUT_TICKS) {
            endCombat();
        }
    }

    private void logGameTick(int relativeGameTick, List<HitsplatData> currentTickHitsplats) {
        StringBuilder logStr = new StringBuilder(String.format("\n----- Tick %d -----\n", relativeGameTick));

        // Target Information
        // Targeting
        String targetingStr = (playerData.getTargeting() == null) ? "Nobody" : playerData.getTargeting().getName();
        logStr.append(String.format("Current Target: %s\n", targetingStr));
        // Targeted By
        List<NPC> targetedBy = npcData.getTargetingPlayer();
        StringBuilder targetedByStr = new StringBuilder();
        if (targetedBy.isEmpty()) {
            targetedByStr.append("Nobody");
        } else {
            for (int i = 0; i < targetedBy.size(); i++) {
                NPC npc = targetedBy.get(i);
                targetedByStr.append(String.format("%s (Lvl %d)", npc.getName(), npc.getCombatLevel()));
                if (i != targetedBy.size() - 1) { targetedByStr.append(", "); }
            }
        }
        logStr.append((String.format("Targeted By: %s\n", targetedByStr)));

        // Hitsplats
        List<HitsplatData> dealtAmounts = new ArrayList<>();
        List<HitsplatData> receivedAmounts = new ArrayList<>();
        for (HitsplatData h : currentTickHitsplats) {
            if (h.isIncoming()) {
                receivedAmounts.add(h);
            } else {
                dealtAmounts.add(h);
            }
        }

        // Outgoing Damage Line
        if (!dealtAmounts.isEmpty()) {
            StringBuilder dealtStr = new StringBuilder();
            for (int i=0; i < dealtAmounts.size(); i++) {
                HitsplatData hit = dealtAmounts.get(i);
                dealtStr.append(String.format("[%d -> %s (Lvl %s)]", hit.getAmount(), hit.getAppliedTo().getName(), hit.getAppliedTo().getCombatLevel()));
                if (i != dealtAmounts.size() - 1) {
                    dealtStr.append(", ");
                }
            }
            logStr.append(String.format("Damage Dealt: %s\n", dealtStr.toString()));
        }

        // Incoming Damage Line
        if (!receivedAmounts.isEmpty()) {
            StringBuilder receivedStr = new StringBuilder();
            for (int i = 0; i < receivedAmounts.size(); i++) {
                HitsplatData hit = receivedAmounts.get(i);
                receivedStr.append(String.format("[%d]", hit.getAmount()));
                if (i != receivedAmounts.size() - 1) {
                    receivedStr.append(", ");
                }
            }
            logStr.append(String.format("Damage Received: %s\n", receivedStr.toString()));
        }

        logStr.append("--------------------");

        // Log the entire thing
        log.info(logStr.toString());
    }

    private void registerCombatAction() {
        ticksSinceLastAction = 0;

        if (currentSession == null) {
            currentSession = new CombatSession(client.getTickCount());
            eventBus.post(new CombatStartedEvent(currentSession));
            log.info("Combat started! Tick: {}", currentSession.getStartTick());
        }
    }

    public void endCombat() {
        if (currentSession == null) {
            return;
        }

        currentSession.setEndTick(client.getTickCount());
        eventBus.post(new CombatEndedEvent(currentSession));

        log.info("Combat ended! Duration: {} ticks", currentSession.getDurationTicks());
        int totalDealt = 0;
        int totalReceived = 0;

        for (TickRecord record : currentSession.getTickRecords()) {
            for (HitsplatData h : record.getHitsplats()) {
                if (h.isIncoming()) totalReceived += h.getAmount();
                else totalDealt += h.getAmount();
            }
        }
        log.info("Summary -> Dealt: {}, Received: {}", totalDealt, totalReceived);

        currentSession = null;
    }
}
