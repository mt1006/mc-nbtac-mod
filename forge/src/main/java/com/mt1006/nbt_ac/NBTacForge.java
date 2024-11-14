package com.mt1006.nbt_ac;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.forge.ConfigScreenFactory;
import com.mt1006.nbt_ac.forge.ForgeResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Set;

@Mod(NBTac.MOD_ID)
public class NBTacForge implements NBTacLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();

	public NBTacForge(FMLJavaModLoadingContext ctx)
	{
		if (isDedicatedServer) { return; }
		ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, ConfigScreenFactory::create);
		((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(new ForgeResourceLoader());

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
