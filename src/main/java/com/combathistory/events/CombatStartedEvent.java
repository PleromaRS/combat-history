package com.combathistory.events;

import com.combathistory.model.CombatSession;

public class CombatStartedEvent {
    private final CombatSession session;

    public CombatStartedEvent(CombatSession session) {
        this.session = session;
    }

    public CombatSession getSession() { return session; }
}
