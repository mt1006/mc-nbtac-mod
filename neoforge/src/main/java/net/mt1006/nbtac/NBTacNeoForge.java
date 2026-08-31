package net.mt1006.nbtac;

import net.mt1006.nbtac.autocomplete.loader.Loader;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.neoforge.ConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(NBTac.MOD_ID)
public class NBTacNeoForge implements NBTacLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.getDist().isDedicatedServer();

	public NBTacNeoForge(IEventBus modEventBus)
	{
		if (isDedicatedServer) { return; }
		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ConfigScreenFactory::new);

		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::loadComplete);
	}

	public void setup(FMLCommonSetupEvent event)
	{
		NBTac.init(isDedicatedServer, this);
	}

	public void loadComplete(FMLLoadCompleteEvent event)
	{
		if (isDedicatedServer) { return; }
		if (ModConfig.useNewThread.val) { new Thread(Loader::load).start(); }
		else { Loader.load(); }
	}

	@Override public boolean isModPresent(String id)
	{
		return ModList.get().isLoaded(id);
	}

	@Override public boolean isFabric()
	{
		return false;
	}
}
