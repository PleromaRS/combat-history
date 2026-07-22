package com.combathistory.Events;

import net.runelite.api.ChatMessageType;

public class ChatMessageEvent extends RawEvent {
    private final ChatMessageType type;
    private final String name;
    private final String message;

    public ChatMessageEvent(ChatMessageType type, String name, String message) {
        this.type = type;
        this.name = name;
        this.message = message;
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}