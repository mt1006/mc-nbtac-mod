package com.mt1006.nbt_ac.config.gui;

import net.minecraftforge.client.ConfigScreenHandler;

public class ConfigScreenFactory
{
	public static ConfigScreenHandler.ConfigScreenFactory create()
	{
		// Needs to be separated from main class! In other case it crashed dedicated servers.
		return new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new ConfigScreen(screen));
	}
}
