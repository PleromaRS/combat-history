package com.combathistory.tracker;

import com.combathistory.events.CombatEndedEvent;
import com.combathistory.events.CombatStartedEvent;
import com.combathistory.model.CombatSession;
import com.combathistory.model.HitsplatData;
import com.combathistory.model.TickRecord;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.Player;
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
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) return;

        Actor actor = event.getActor();
        Actor target = localPlayer.getInteracting();

        Hitsplat hitsplat = event.getHitsplat();

        boolean isIncoming = (actor == localPlayer);
        boolean isOutgoing = (actor == target) && (hitsplat.isMine());

        if (isIncoming || isOutgoing) {
            if (currentSession == null) {
                registerCombatAction();
            }

            HitsplatData data = new HitsplatData(
                    isIncoming,
                    hitsplat.getAmount(),
                    hitsplat.getHitsplatType()
            );

            currentTickHitsplats.add(data);
            ticksSinceLastAction = 0;
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (currentSession == null) {
            return;
        }

        ticksSinceLastAction++;

        Player localPlayer = client.getLocalPlayer();
        Actor target = localPlayer.getInteracting();
        String targetName = (target != null) ? target.getName() : "None";

        int relativeTick = client.getTickCount() - currentSession.getStartTick() + 1;

        List<Integer> dealtAmounts = new ArrayList<>();
        List<Integer> receivedAmounts = new ArrayList<>();

        for (HitsplatData h : currentTickHitsplats) {
            if (h.isIncoming()) {
                receivedAmounts.add(h.getAmount());
            } else {
                dealtAmounts.add(h.getAmount());
            }
        }

        String dealtStr = dealtAmounts.isEmpty() ? "[]" : dealtAmounts.toString();
        String receivedStr = receivedAmounts.isEmpty() ? "[]" : receivedAmounts.toString();

        log.info(String.format("[Tick %2d] Dealt: %-6s | Received: %-6s | Current Target: %s",
                relativeTick, dealtStr, receivedStr, targetName));

        TickRecord record = new TickRecord(client.getTickCount(), targetName);

        for (HitsplatData h : currentTickHitsplats) {
            record.addHitsplat(h);
        }

        currentSession.addTickRecord(record);

        currentTickHitsplats.clear();

        if (ticksSinceLastAction >= COMBAT_TIMEOUT_TICKS) {
            endCombat();
        }
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
