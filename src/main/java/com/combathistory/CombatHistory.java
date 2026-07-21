package com.combathistory;

import com.combathistory.Fights.AbstractBossFight;
import com.combathistory.Fights.ZulrahFight;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(name = "Combat History")
public class CombatHistory extends Plugin {

    @Inject
    private Client client;

//    Caused an error, commented out until I need it
//    @Inject
//    private CombatHistoryConfig config;

    // --- BOSS REGISTRY ---
    private List<AbstractBossFight> registeredBosses = Arrays.asList(
            new ZulrahFight()
            // More to add
    );

    private AbstractBossFight currentFight = null;
//    private abstract PlayerTracker = new PlayerTracker();

    private int fightStartTick = -1;

    // --- LIFECYCLE ---

    @Override
    protected void startUp() {
        log.info("Combat History started");
    }

    @Override
    protected void shutDown() {
        currentFight = null;
        log.info("Combat History stopped");
    }

    // --- BOSS DETECTION ---

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();

        // If we don't have an active fight, check if this NPC is a boss we track
        if (currentFight == null) {
            for (AbstractBossFight boss : registeredBosses) {
                if (boss.isBossNpc(npc.getId())) {
                    fightStartTick = client.getTickCount();

                    currentFight = boss;
                    currentFight.init(npc);
                    // playerTracker.init();
                    log.info("Fight Started: " + boss.getBossName());
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();

        // If the boss despawns (dies or player leaves), stop tracking
        if (currentFight != null && currentFight.isBossNpc(npc.getId())) {
            currentFight.endFight();
            // playerTracker.endFight();
            log.info("Fight Ended: " + currentFight.getBossName());
            currentFight = null;
            fightStartTick = -1;
        }
    }

    // --- EVENT ROUTING (Forward transient events to the active fight) ---

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (currentFight != null && event.getActor() instanceof NPC) {
            NPC npc = (NPC) event.getActor();
            currentFight.onAnimationChanged(npc, npc.getAnimation());
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (currentFight != null && event.getActor() instanceof NPC) {
            NPC npc = (NPC) event.getActor();
            currentFight.onHitsplatApplied(npc, event.getHitsplat());
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (currentFight != null) {
            currentFight.onProjectileMoved(event.getProjectile());
        }
    }

    // --- GAME TICK (Process and output) ---

    @Subscribe
    public void onGameTick(GameTick event) {
        if (currentFight != null) {
            int globalTick = client.getTickCount();
            int relativeTick = globalTick - fightStartTick;

            // Tell the boss fight to process everything
            currentFight.onGameTick(relativeTick);
            // playerTracker.onGameTick(relativeTick);

            // Merge their outputs into one unified report
//            TickReport report = new TickReport();
//            report.relativeTick = relativeTick;
//            report.bossState = currentFight.getBossSnapshot();
//            report.bossEvents = currentFight.getTickEvents();
//            report.playerState = playerTracker.getPlayerSnapshot();
//            report.playerEvents = playerTracker.getTickEvents();
//
//            // Output (to file, UI, or buffer for end-of-fight upload)
//            outputReport(report);
        }
    }

    // --- OUTPUT ---

//    private void outputReport(TickReport report) {
//
//    }
}