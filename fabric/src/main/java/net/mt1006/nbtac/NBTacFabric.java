package net.mt1006.nbtac;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Set;

public class NBTacFabric implements ModInitializer, NBTacLoaderInterface
{
	private static final FabricLoader FABRIC_LOADER = FabricLoader.getInstance();
	public static final boolean isDedicatedServer = FABRIC_LOADER.getEnvironmentType() == EnvType.SERVER;

	@Override public void onInitialize()
	{
		NBTac.init(isDedicatedServer, this);
	}

	@Override public void appendModVersionIds(Set<String> mods)
	{
		for (ModContainer modInfo : FABRIC_LOADER.getAllMods())
		{
			ModMetadata metadata = modInfo.getMetadata();
			mods.add(String.format("%s@%s;", metadata.getId(), metadata.getVersion().getFriendlyString()));
		}
	}

	@Override public boolean isModPresent(String id)
	{
		return FABRIC_LOADER.getModContainer(id).isPresent();
	}

	@Override public boolean isFabric()
	{
		return true;
	}
}
