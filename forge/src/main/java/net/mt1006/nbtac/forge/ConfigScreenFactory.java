package net.mt1006.nbtac.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.mt1006.nbtac.config.gui.ConfigScreen;

public class ConfigScreenFactory
{
	public static ConfigScreenHandler.ConfigScreenFactory create()
	{
		// Needs to be separated from main class! In other case it crashed dedicated servers.
		return new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new ConfigScreen(screen));
	}
}
