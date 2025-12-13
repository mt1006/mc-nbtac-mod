package com.mt1006.nbt_ac.neoforge;

import com.google.gson.JsonElement;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class NeoForgeResourceLoader extends SimplePreparableReloadListener<Map<Identifier, JsonElement>>
{
	ResourceLoader loader = new ResourceLoader();

	@Override protected @NotNull Map<Identifier, JsonElement> prepare(@NotNull ResourceManager resourceManager,
																			@NotNull ProfilerFiller profilerFiller)
	{
		return loader.prepare(resourceManager);
	}

	@Override protected void apply(@NotNull Map<Identifier, JsonElement> resources,
								   @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
	{
		loader.apply(resources);
	}
}
