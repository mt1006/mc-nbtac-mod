package com.mt1006.nbt_ac;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NBTac.MOD_ID)
@Mod.EventBusSubscriber(modid = NBTac.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NBTac
{
	public static final String MOD_ID = "nbt_ac";
	public static final String VERSION = "1.0";
	public static final String FOR_VERSION = "1.16.5";
	public static final String FOR_LOADER = "Forge";
	public static final Logger LOGGER = LogManager.getLogger();

	public NBTac()
	{
		MinecraftForge.EVENT_BUS.register(this);
		((IReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(new ResourceLoader());
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");

		ModConfig.initConfig();
		ModConfig.loadConfig();
		Fields.init();
		NbtSuggestion.Type.init();
	}

	@SubscribeEvent
	public static void loadComplete(FMLLoadCompleteEvent event)
	{
		new Thread(Loader::load).start();
	}

	public static String getFullName()
	{
		return "NBTac v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
