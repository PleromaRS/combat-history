package com.combathistory.Reporting;

import java.util.List;

public class TickReport {
    public int relativeTick;        // e.g., 1, 2, 3... (currentGlobalTick - fightStartTick)

    // The Boss Side
    public BossSnapshot bossState;  // HP, phase, location, active minions
    public List<String> bossEvents; // "Zulrah attacks", "Snakeling spawns"

    // The Player Side
    public PlayerSnapshot playerState; // HP, prayer, equipment, inventory
    public List<String> playerEvents;  // "Ate Shark", "Switched to Protect from Magic", "Attacked Zulrah"
}