package net.mt1006.nbtac;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NBTacFabric implements ModInitializer, NBTacLoaderInterface
{
	private static final FabricLoader FABRIC_LOADER = FabricLoader.getInstance();
	public static final boolean isDedicatedServer = FABRIC_LOADER.getEnvironmentType() == EnvType.SERVER;

	@Override public void onInitialize()
	{
		NBTac.init(isDedicatedServer, this);
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
