package com.combathistory.Events;

import net.runelite.api.Projectile;

public class ProjectileEvent extends RawEvent {
    private final Projectile projectile;

    public ProjectileEvent(Projectile projectile) {
        this.projectile = projectile;
    }

    public Projectile getProjectile() {
        return projectile;
    }
}