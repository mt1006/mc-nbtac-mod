package com.mt1006.nbt_ac.config.gui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

public class ConfigScreenFactory implements IConfigScreenFactory
{
	@Override public @NotNull Screen createScreen(@NotNull ModContainer container, @NotNull Screen modListScreen)
	{
		// Needs to be separated from main class! In other case it crashed dedicated servers.
		return new ConfigScreen(modListScreen);
	}
}
