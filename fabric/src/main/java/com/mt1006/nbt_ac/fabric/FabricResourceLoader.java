package com.mt1006.nbt_ac.fabric;

import com.google.gson.JsonElement;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import com.mt1006.nbt_ac.config.ModConfig;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricResourceLoader implements SimpleResourceReloadListener<Map<ResourceLocation, JsonElement>>
{
	ResourceLoader loader = new ResourceLoader();

	@Override public ResourceLocation getFabricId()
	{
		return new ResourceLocation("nbt_ac", "nbt_ac");
	}

	@Override public CompletableFuture<Map<ResourceLocation, JsonElement>> load(ResourceManager manager, ProfilerFiller profiler, Executor executor)
	{
		return CompletableFuture.supplyAsync(() -> loader.prepare(manager));
	}

	@Override public CompletableFuture<Void> apply(@NotNull Map<ResourceLocation, JsonElement> resources,
												   @NotNull ResourceManager resourceManager, ProfilerFiller profiler, Executor executor)
	{
		if (loader.apply(resources) && !ModConfig.useNewThread.val) { Loader.load(); }
		return CompletableFuture.runAsync(() -> {});
	}
}
