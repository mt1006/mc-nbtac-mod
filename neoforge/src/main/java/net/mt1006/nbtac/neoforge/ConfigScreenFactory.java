package net.mt1006.nbtac.neoforge;

import net.minecraft.client.gui.screens.Screen;
import net.mt1006.nbtac.config.gui.ConfigScreen;
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
