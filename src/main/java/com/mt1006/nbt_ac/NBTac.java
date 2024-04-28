package com.mt1006.nbt_ac;

import com.mojang.logging.LogUtils;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class NBTac implements ModInitializer
{
	public static final String MOD_ID = "nbt_ac";
	public static final String VERSION = "1.2.1";
	public static final String FOR_VERSION = "1.18.2";
	public static final String FOR_LOADER = "Fabric";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean isDedicatedServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

	@Override public void onInitialize()
	{
		LOGGER.info("{} - Author: mt1006 (mt1006x)", getFullName());

		if (isDedicatedServer)
		{
			LOGGER.info("Dedicated server detected - mod setup stopped!");
			return;
		}

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ResourceLoader());
		ModConfig.load();
		Fields.init();
		NbtSuggestion.Type.init();
	}

	public static String getFullName()
	{
		return "NBTac v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
