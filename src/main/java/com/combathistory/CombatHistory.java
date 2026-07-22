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

    // --- EVENT ROUTING (Forward transient events to the active fight) ---

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (currentFight != null) {
            currentFight.onNpcSpawned(event.getNpc());
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (currentFight != null) {
            currentFight.onNpcDespawned(event.getNpc());
        }
    }

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

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (currentFight != null) {
            currentFight.onGameObjectSpawned(event.getGameObject());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (currentFight != null) {
            currentFight.onGameObjectDespawned(event.getGameObject());
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (currentFight != null) {
            currentFight.onChatMessage(event.getType(), event.getName(), event.getMessage());
        }
    }

    // --- GAME TICK ---

    @Subscribe
    public void onGameTick(GameTick event) {
        int globalTick = client.getTickCount();

        if (currentFight != null) {
            if (currentFight.isFightEnded()) {
                currentFight = null;
                fightStartTick = -1;
            } else {
                int relativeTick = globalTick - fightStartTick;
                currentFight.onGameTick(relativeTick, client);
                // playerTracker.onGameTick(relativeTick, client);
            }


        } else {
            for (AbstractBossFight boss: registeredBosses) {
                if (boss.checkStartConditions(client)) {
                    currentFight = boss;
                    fightStartTick = globalTick;
                    currentFight.init(client);
                    break;
                }
            }
        }
    }

    // --- OUTPUT ---

//    private void outputReport(TickReport report) {
//
//    }
}