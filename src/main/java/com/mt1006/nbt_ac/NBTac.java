package com.mt1006.nbt_ac;

import com.mojang.logging.LogUtils;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(NBTac.MOD_ID)
@Mod.EventBusSubscriber(modid = NBTac.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NBTac
{
	//TODO: add support for storage NBTs (1.2)
	public static final String MOD_ID = "nbt_ac";
	public static final String VERSION = "1.1.8";
	public static final String FOR_VERSION = "1.20.4";
	public static final String FOR_LOADER = "Forge";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();

	public NBTac()
	{
		if (isDedicatedServer) { return; }
		MinecraftForge.EVENT_BUS.register(this);
		((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(new ResourceLoader());
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");

		if (isDedicatedServer)
		{
			LOGGER.info("Dedicated server detected - mod setup stopped!");
			return;
		}

		ModConfig.initConfig();
		ModConfig.loadConfig();
		Fields.init();
		NbtSuggestion.Type.init();
	}

	@SubscribeEvent
	public static void loadComplete(FMLLoadCompleteEvent event)
	{
		if (isDedicatedServer) { return; }
		if (ModConfig.useNewThread.getValue()) { new Thread(Loader::load).start(); }
		else { Loader.load(); }
	}

	public static String getFullName()
	{
		return "NBTac v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
