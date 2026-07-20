package com.combathistory.events;

import com.combathistory.model.CombatSession;

public class CombatEndedEvent {
    private final CombatSession session;

    public CombatEndedEvent(CombatSession session) {
        this.session = session;
    }

    public CombatSession getSession() { return session; }
}
