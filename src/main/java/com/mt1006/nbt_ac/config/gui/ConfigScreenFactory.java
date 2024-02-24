package com.mt1006.nbt_ac.config.gui;

import net.minecraftforge.client.ConfigGuiHandler;

public class ConfigScreenFactory
{
	public static ConfigGuiHandler.ConfigGuiFactory create()
	{
		// Needs to be separated from main class! In other case it crashed dedicated servers.
		return new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen));
	}
}
