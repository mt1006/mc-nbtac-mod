package com.mt1006.nbt_ac.forge;

import com.google.gson.JsonElement;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ForgeResourceLoader extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>>
{
	ResourceLoader loader = new ResourceLoader();

	@Override protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager,
																			@NotNull ProfilerFiller profilerFiller)
	{
		return loader.prepare(resourceManager);
	}

	@Override protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources,
								   @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
	{
		loader.apply(resources);
	}
}
