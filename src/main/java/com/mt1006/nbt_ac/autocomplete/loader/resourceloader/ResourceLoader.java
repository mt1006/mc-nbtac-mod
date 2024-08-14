package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class ResourceLoader implements SimpleResourceReloadListener<Map<ResourceLocation, JsonElement>>
{
	private static final String RESOURCE_DIRECTORY = "nbt_ac_suggestions_v2";
	public static final List<TagStructure> tags = new ArrayList<>();
	public static final List<Pair<JsonArray, JsonArray>> predictions = new ArrayList<>();
	public static boolean firstCall = true;
	public static CountDownLatch countDownLatch = new CountDownLatch(1);

	@Override public ResourceLocation getFabricId()
	{
		return new ResourceLocation("nbt_ac", "nbt_ac");
	}

	@Override public CompletableFuture<Map<ResourceLocation, JsonElement>> load(ResourceManager manager,
																				ProfilerFiller profiler, Executor executor)
	{
		return CompletableFuture.supplyAsync(() -> prepare(manager, profiler));
	}

	private @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager,
																@NotNull ProfilerFiller profilerFiller)
	{
		Gson gson = new Gson();
		Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(RESOURCE_DIRECTORY);

		for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet())
		{
			ResourceLocation resourceLocation = fileToIdConverter.fileToId(entry.getKey());

			try (Reader reader = entry.getValue().openAsReader())
			{
				JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
				map.put(resourceLocation, jsonElement);
			}
			catch (Exception ignore) {}
		}
		return map;
	}

	@Override public CompletableFuture<Void> apply(@NotNull Map<ResourceLocation, JsonElement> resources,
												   @NotNull ResourceManager resourceManager,
												   @NotNull ProfilerFiller profilerFiller, Executor executor)
	{
		if (!firstCall) { return CompletableFuture.runAsync(() -> {}); }
		firstCall = false;

		for (Map.Entry<ResourceLocation, JsonElement> resourceEntry : resources.entrySet())
		{
			try
			{
				JsonObject json = resourceEntry.getValue().getAsJsonObject();
				MutablePair<JsonArray, JsonArray> predictionPair = new MutablePair<>();
				TagStructure tagStructure = new TagStructure();

				for (Map.Entry<String, JsonElement> entry : json.entrySet())
				{
					String key = entry.getKey();
					JsonElement value = entry.getValue();

					switch (key)
					{
						case "id" -> tagStructure.id = value.getAsString();
						case "apply_to" -> tagStructure.applyTo = value.getAsJsonArray();
						case "tags" -> tagStructure.tags = value.getAsJsonArray();
						case "conditions" -> predictionPair.left = value.getAsJsonArray();
						case "operations" -> predictionPair.right = value.getAsJsonArray();
					}
				}

				if (predictionPair.left != null && predictionPair.right != null)
				{
					predictions.add(predictionPair);
					continue;
				}

				if (tagStructure.tags == null)
				{
					NBTac.LOGGER.warn("Failed to identify the resource: {}", resourceEntry.getKey());
					continue;
				}

				if (tagStructure.id == null)
				{
					String path = resourceEntry.getKey().getPath();
					if (!path.startsWith("tags/"))
					{
						NBTac.LOGGER.warn("Suggestion without ID outside of \"tags/\" directory: {}", resourceEntry.getKey());
						continue;
					}
					tagStructure.id = path.substring(5);
				}
				tags.add(tagStructure);
			}
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to load the resource: {}", resourceEntry.getKey());
			}
		}

		countDownLatch.countDown();
		if (!ModConfig.useNewThread.val) { Loader.load(); }
		return CompletableFuture.runAsync(() -> {});
	}

	public static class TagStructure
	{
		public String id;
		public @Nullable JsonArray applyTo;
		public JsonArray tags;
	}
}
