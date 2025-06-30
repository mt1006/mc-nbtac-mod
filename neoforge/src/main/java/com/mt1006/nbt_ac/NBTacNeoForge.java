package com.mt1006.nbt_ac;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.neoforge.ConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Set;

@Mod(NBTac.MOD_ID)
public class NBTacNeoForge implements NBTacLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();

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

	@Override public void appendModVersionIds(Set<String> mods)
	{
		for (IModInfo modInfo : ModList.get().getMods())
		{
			String id = modInfo.getModId();
			ArtifactVersion version = modInfo.getVersion();
			String qualifier = version.getQualifier();

			mods.add(String.format("%s@%d.%d.%d.%d#%s;", id, version.getMajorVersion(), version.getMinorVersion(),
					version.getIncrementalVersion(), version.getBuildNumber(), qualifier != null ? qualifier : "?"));
		}
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
