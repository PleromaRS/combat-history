package com.combathistory;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("combathistory")
public interface CombatHistoryConfig extends Config
{
	@ConfigItem(
		keyName = "trackPerTickData",
		name = "Track Per-Tick Data",
		description = "Records HP and target every game tick during combat."
	)
	default boolean trackPerTickData()
	{
		return true;
	}
}