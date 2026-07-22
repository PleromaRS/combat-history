package com.combathistory.Fights;


import com.combathistory.Events.*;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZulrahFight extends AbstractBossFight {
    private final String BOSS_NAME = "Zulrah";

    private static final Logger log = LoggerFactory.getLogger(ZulrahFight.class);

    // ----- PRIVATE ENUMS -----
    private enum NPC_IDS {
        SERPENTINE(2042, "Zulrah (Serpentine)", true),
        TANZANITE(2043, "Zulrah (Tanzanite)", true),
        MAGMA(2044, "Zulrah (Magma)", true),
        SNAKELING_MELEE(2045, "Snakeling (Melee)", false),
        SNAKELING_MAGIC(2046, "Snakeling (Magic)", false);

        private final int id;
        private final String name;
        private final boolean isBoss;

        NPC_IDS(int id, String name, boolean isBoss) {
            this.id = id;
            this.name = name;
            this.isBoss = isBoss;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public boolean isBoss() { return isBoss; }
        public boolean isMinion() { return !isBoss; }

        private static final Map<Integer, NPC_IDS> ID_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(NPC_IDS::getId, e -> e));

        public static NPC_IDS fromId(int id) {
            return ID_MAP.get(id);
        }
    }

    private enum PROJECTILE_IDS {
        ZULRAH_RANGED(1044, "Zulrah Ranged Attack"),
        ZULRAH_SMOKE(1045, "Zulrah Smoke Spawner"),
        ZULRAH_MAGIC(1046, "Zulrah Magic Attack"),
        ZULRAH_SNAKELING(1047, "Zulrah Snakeling Spawner"),
        SNAKELING_MAGIC(1230, "Snakeling Magic Attack");

        private final int id;
        private final String name;

        PROJECTILE_IDS(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        private static final Map<Integer, PROJECTILE_IDS> ID_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(PROJECTILE_IDS::getId, e -> e));

        public static PROJECTILE_IDS fromId(int id) {
            return ID_MAP.get(id);
        }
    }

    private enum ANIMATION_IDS {
        ZULRAH_ATTACK(5069, "Zulrah Attack"),
        ZULRAH_MELEE_ATTACK(5807, "Zulrah Melee Attack"),
        ZULRAH_ASCENDS(5073, "Zulrah Ascends"),
        ZULRAH_DESCENDS(5072, "Zulrah Descends"),
        ZULRAH_DIES(5804, "Zulrah Dies"),
        ZULRAH_DIES_ALT(5805, "Zulrah Dies"),
        SNAKELING_SPAWNS(2413, "Snakeling Spawns"),
        SNAKELING_ATTACK(1741, "Snakeling Attack"),
        SNAKELING_DIES(2408, "Snakeling Dies");

        private final int id;
        private final String name;

        ANIMATION_IDS(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        private static final Map<Integer, ANIMATION_IDS> ID_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(ANIMATION_IDS::getId, e -> e));

        public static ANIMATION_IDS fromId(int id) {
            return ID_MAP.get(id);
        }
    }

    private enum OBJECT_IDS {
        SMOKE_CLOUD(11700, "Smoke Cloud");

        private final int id;
        private final String name;

        OBJECT_IDS(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        private static final Map<Integer, OBJECT_IDS> ID_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(OBJECT_IDS::getId, e -> e));

        public static OBJECT_IDS fromId(int id) {
            return ID_MAP.get(id);
        }
    }

    // ----- PRIVATE STATE VARIABLES -----
    private NPC bossNpc;                // The active Zulrah NPC
    private int currentHp;              // Polled from bossNpc.getHealthRatio()
    private WorldPoint bossLocation;    // Polled from bossNpc.getWorldLocation()
    private int phase;                  // Which phase of the fight the boss is on

    // ----- IMPLEMENTATION OF ABSTRACT METHODS
    @Override
    protected void processBufferedEvents() {
        List<RawEvent> eventsToProcess = new ArrayList<>(this.eventBuffer);
        eventBuffer.clear();

        // Loop through the inherited 'eventBuffer'
        for (RawEvent event : eventsToProcess) {

            // ANIMATION EVENT HANDLING
            if (event instanceof AnimationEvent) {
                AnimationEvent animEvent = (AnimationEvent) event;
                int animId = animEvent.getAnimationId();
                NPC npc = animEvent.getNpc();

                ANIMATION_IDS action = ANIMATION_IDS.fromId(animId);

                // TODO: Handle different animation events if necessary.
                // Such as remove snakelings from their set?
                if (action != null) {
                    log.info(String.format("[Combat History][Tick: %d][Animation] %s: %s", currentTick, npc.getName(), action.getName()));
                }

            // HITSPLAT EVENT HANDLING
            } else if (event instanceof HitsplatEvent) {

                HitsplatEvent hitsplatEvent = (HitsplatEvent) event;
                Hitsplat hitsplat = hitsplatEvent.getHitsplat();
                NPC npc = hitsplatEvent.getNpc();
                log.info(String.format("[Combat History][Tick: %d][Hitsplat] %s: %s, %d", currentTick, npc.getName(), hitsplat.getAmount(), hitsplat.getHitsplatType()));

            // PROJECTILE EVENT HANDLING
            } else if (event instanceof ProjectileEvent) {
                ProjectileEvent projEvent = (ProjectileEvent) event;
                int projId = projEvent.getProjectile().getId();

                PROJECTILE_IDS projectile = PROJECTILE_IDS.fromId(projId);

                // TODO: Handle different projectile events if necessary.
                if (projectile != null) {
                    log.info(String.format("[Combat History][Tick: %d][Projectile] %s", currentTick, projectile.getName()));
                }

            // OBJECT SPAWN EVENT HANDLING
            } else if (event instanceof ObjectSpawnEvent) {
                ObjectSpawnEvent objEvent = (ObjectSpawnEvent) event;
                int objId = objEvent.getObjectId();

                OBJECT_IDS object = OBJECT_IDS.fromId(objId);

                if(object != null) {
                    log.info(String.format("[Combat History][Tick: %d][Object Spawned] %s\n[Current Count] %s", currentTick, object.getName(), activeObjects.size()));
                }

            // OBJECT DESPAWN EVENT HANDLING
            } else if (event instanceof ObjectDespawnEvent) {
                ObjectDespawnEvent objEvent = (ObjectDespawnEvent) event;
                int objId = objEvent.getObjectId();

                OBJECT_IDS object = OBJECT_IDS.fromId(objId);

                if(object != null) {
                    log.info(String.format("[Combat History][Tick: %d][Object Despawned] %s\n[Current Count] %s", currentTick, object.getName(), activeObjects.size()));
                }

            // NPC SPAWN EVENT HANDLING
            } else if (event instanceof NPCSpawnEvent) {
                NPCSpawnEvent npcEvent = (NPCSpawnEvent) event;
                NPC_IDS npc = NPC_IDS.fromId(npcEvent.getNpcId());

                if (npc != null) {
                    if (isBossNpc(npc.getId())) {
                        log.info(String.format("[Combat History][Tick: %d][Boss Spawned] %s", currentTick, npc.getName()));
                    } else {
                        log.info(String.format("[Combat History][Tick: %d][Minion Spawned] %s [Current Count] %d", currentTick, npc.getName(), activeMinions.size()));
                    }
                }

            // NPC DESPAWN EVENT HANDLING
            } else if (event instanceof NPCDespawnEvent) {
                NPCDespawnEvent npcEvent = (NPCDespawnEvent) event;
                NPC_IDS npc = NPC_IDS.fromId(npcEvent.getNpcId());

                if (npc != null) {
                    if (isBossNpc(npc.getId())) {
                        log.info(String.format("[Combat History][Tick: %d][Boss Despawned] %s", currentTick, npc.getName()));
                    } else {
                        log.info(String.format("[Combat History][Tick: %d][Minion Despawned] %s [Current Count] %d",
                                currentTick, npc.getName(), activeMinions.size()));
                    }
                }

            // CHAT MESSAGE EVENT HANDLING
            } else if (event instanceof ChatMessageEvent) {
                ChatMessageEvent chatMessageEvent = (ChatMessageEvent) event;

                if (chatMessageEvent.getType() == ChatMessageType.GAMEMESSAGE) {
                    String message = chatMessageEvent.getMessage();
                    if (message.contains("Fight duration:")) {
                        endFight(message);
                    }
                }
            }
        }
    }

    @Override
    protected void updatePolledState() {
        // Poll the game client for persistent states

        // Update currentHp using bossNpc.getHealthRatio()
        // Update bossLocation using bossNpc.getWorldLocation()
        // Update activeMinions list (add newly spawned, remove dead ones)
        // Determine currentPhase based on bossLocation or HP thresholds
        // Increment ticksSinceLastAction
    }

    @Override
    protected void buildTickReport() {
        // Combine the processed events and polled states into the inherited 'currentTickReport'

        // Example: If an attack animation and hitsplat were matched, add:
        //   currentTickReport.add("Zulrah attacks with Magic for 42 damage.");

        // Example: If a phase transition was detected, add:
        //   currentTickReport.add("Zulrah descends into the water (Phase 2).");

        // Example: If a Snakeling spawned, add:
        //   currentTickReport.add("Snakeling spawned. Total minions: 2.");

        // If the eventBuffer was empty and no state changes occurred, add:
        //   currentTickReport.add("Tick " + currentTick + ": No actions occurred.");
    }

    @Override
    protected boolean isRelevantNpc(int npcId) {
        return NPC_IDS.fromId(npcId) != null;
    }

    @Override
    protected boolean isRelevantProjectile(int projectileId) {
        return PROJECTILE_IDS.fromId(projectileId) != null;
    }

    @Override
    protected boolean isRelevantObject(int objectId) {
        return OBJECT_IDS.fromId(objectId) != null;
    }

    @Override
    public boolean isBossNpc(int npcId) {
        NPC_IDS npc = NPC_IDS.fromId(npcId);
        return npc != null && npc.isBoss();
    }

    @Override
    public String getBossName() {
        return "Zulrah";
    }

    @Override
    public boolean checkStartConditions(Client client) {
        WorldView worldView = client.getTopLevelWorldView();
        IndexedObjectSet<? extends NPC> npcs = worldView.npcs();

        for (NPC npc : npcs) {
            if (isBossNpc(npc.getId())) {
                init(client);
                return true;
            }
        }
        return false;
    }

    public void init(Client client) {
        this.fightEnded = false;
        this.currentHp = -1;
        this.eventBuffer.clear();
        this.currentTick = 0;
        resetProjectileTracking();
        resetObjectTrackings();
        resetActiveMinions();

        log.info("[Combat History][Fight Started]: " + this.BOSS_NAME);
        // NOTE: Reminder that in this if a boss starts within minions active then we can always just find them here through a loop and set them, good to remember
    }

    public void endFight(String durationMessage) {
        this.bossNpc = null;
        this.fightEnded = true;
        this.eventBuffer.clear();
        this.currentTick = 0;
        resetProjectileTracking();
        resetObjectTrackings();
        resetActiveMinions();

        log.info(String.format("[Combat History][Fight Ended]: %s | %s", this.BOSS_NAME, durationMessage));
    }


}