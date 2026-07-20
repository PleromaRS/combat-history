package com.combathistory;

import com.combathistory.tracker.CombatTracker;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Combat History",
		description = "Tracks details combat statistics and sessions",
		tags = {"combat", "pvm", "history"}
)
public class CombatHistory extends Plugin {

	@Inject
	private EventBus eventBus;

	@Inject
	private CombatTracker combatTracker;

	@Override
	protected void startUp() {
		log.info("Combat History started!");
		eventBus.register(combatTracker);
		combatTracker.reset();
	}

	@Override
	public void shutDown() {
		log.info("Combat History stopped!");
		eventBus.unregister(combatTracker);
		combatTracker.reset();
	}

	@Provides
	CombatHistoryConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(CombatHistoryConfig.class);
	}
}