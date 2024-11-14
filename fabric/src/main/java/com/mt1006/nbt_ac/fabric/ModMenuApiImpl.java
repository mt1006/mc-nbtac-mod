package com.mt1006.nbt_ac.fabric;

import com.mt1006.nbt_ac.config.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuApiImpl implements ModMenuApi
{
	@Override public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return ConfigScreen::new;
	}
}
