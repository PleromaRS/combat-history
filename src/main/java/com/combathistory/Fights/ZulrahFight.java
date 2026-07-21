package com.combathistory.Fights;


import com.combathistory.Events.AnimationEvent;
import com.combathistory.Events.HitsplatEvent;
import com.combathistory.Events.ProjectileEvent;
import com.combathistory.Events.RawEvent;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZulrahFight extends AbstractBossFight {

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
        ZULRAH_DIES(5805, "Zulrah Dies"),
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

    // ----- PRIVATE STATE VARIABLES -----
    private NPC bossNpc;                // The active Zulrah NPC
    private List<NPC> activeMinions = new ArrayList<>();    // List of currently alive Snakelings

    private int currentHp;              // Polled from bossNpc.getHealthRatio()
    private WorldPoint bossLocation;    // Polled from bossNpc.getWorldLocation()

    private int phase;                  // Which phase of the fight the boss is on

    // ----- IMPLEMENTATION OF ABSTRACT METHODS
    @Override
    protected void processBufferedEvents() {
        // Loop through the inherited 'eventBuffer'
        for (RawEvent event : eventBuffer) {
            if (event instanceof AnimationEvent) {
                AnimationEvent animEvent = (AnimationEvent) event;
                int animId = animEvent.getAnimationId();
                NPC npc = animEvent.getNpc();

                ANIMATION_IDS action = ANIMATION_IDS.fromId(animId);

                // TODO: Handle different animation events if necessary.
                if (action != null) {
                    log.info(String.format("[Combat History][Tick: %d][Animation] %s: %s", currentTick, npc.getName(), action.getName()));
                }
             } else if (event instanceof HitsplatEvent) {
                HitsplatEvent hitsplatEvent = (HitsplatEvent) event;
                Hitsplat hitsplat = hitsplatEvent.getHitsplat();
                NPC npc = hitsplatEvent.getNpc();
                log.info(String.format("[Combat History][Tick: %d][Hitsplat] %s: %s, %d", currentTick, npc.getName(), hitsplat.getAmount(), hitsplat.getHitsplatType()));
            } else if (event instanceof ProjectileEvent) {
                ProjectileEvent projEvent = (ProjectileEvent) event;
                int projId = projEvent.getProjectile().getId();

                PROJECTILE_IDS projectile = PROJECTILE_IDS.fromId(projId);

                // TODO: Handle different projectile events if necessary.
                if (projectile != null) {
                    log.info(String.format("[Combat History][Tick: %d][Projectile] %s", currentTick, projectile.getName()));
                }
            }
        }
        // If AnimationEvent:
        //   - Translate ID using Animation enum
        //   - Update lastAttackAnimationId
        //   - Check for phase transitions (e.g., Descends/Ascends)
        //   - Check if Zulrah dies, transition fight to ENDING

        // If HitsplatEvent:
        //   - Match damage to the lastAttackAnimationId
        //   - Record boss/minion damage taken

        // If ProjectileEvent:
        //   - Record incoming attacks and their expected landing tick
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
    public boolean isBossNpc(int npcId) {
        NPC_IDS npc = NPC_IDS.fromId(npcId);
        return npc != null && npc.isBoss();
    }

    @Override
    public String getBossName() {
        return "Zulrah";
    }

    @Override
    public void init(NPC npc) {
        this.bossNpc = npc;
        this.activeMinions.clear();
        this.currentHp = -1;
        this.eventBuffer.clear();
        this.currentTick = 0;
        resetProjectileTracking();
    }

    @Override
    public void endFight() {
        // Build final summary report

        // Clean things up
        this.bossNpc = null;
        this.activeMinions.clear();
        this.eventBuffer.clear();
        this.currentTick = 0;
        resetProjectileTracking();
    }
}