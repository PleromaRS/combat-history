package com.combathistory.Events;

import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

public class ObjectDespawnEvent extends RawEvent {
    private final GameObject gameObject;

    public ObjectDespawnEvent(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public GameObject getGameObject() { return gameObject; }
    public int getObjectId() { return gameObject.getId(); }
    public WorldPoint getWorldLocation() { return gameObject.getWorldLocation(); }
}
