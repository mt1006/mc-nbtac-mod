package net.mt1006.nbtac.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mt1006.nbtac.config.gui.ConfigScreen;

public class ModMenuApiImpl implements ModMenuApi
{
	@Override public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return ConfigScreen::new;
	}
}
