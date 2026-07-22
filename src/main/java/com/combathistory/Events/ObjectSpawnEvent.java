package com.combathistory.Events;

import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

public class ObjectSpawnEvent extends RawEvent {
    private final GameObject gameObject;

    public ObjectSpawnEvent(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public GameObject getGameObject() { return gameObject; }
    public int getObjectId() { return gameObject.getId(); }
    public WorldPoint getWorldLocation() { return gameObject.getWorldLocation(); }
}
