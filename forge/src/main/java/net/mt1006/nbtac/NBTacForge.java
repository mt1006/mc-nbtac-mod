package net.mt1006.nbtac;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.mt1006.nbtac.autocomplete.loader.Loader;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.forge.ConfigScreenFactory;

@Mod(NBTac.MOD_ID)
public class NBTacForge implements NBTacLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();

	public NBTacForge(FMLJavaModLoadingContext ctx)
	{
		if (isDedicatedServer) { return; }
		ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, ConfigScreenFactory::create);

		IEventBus eventBus = ctx.getModEventBus();
		eventBus.addListener(this::setup);
		eventBus.addListener(this::loadComplete);
	}

	public void setup(final FMLCommonSetupEvent event)
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
