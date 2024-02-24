package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.utils.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ResourceLoader extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>>
{
	private static final String RESOURCE_DIRECTORY = "nbt_ac_suggestions";
	public static final List<Pair<String, JsonObject>> common = new ArrayList<>();
	public static final List<Pair<String, JsonObject>> tags = new ArrayList<>();
	public static final List<Pair<JsonArray, JsonArray>> predictions = new ArrayList<>();
	public static boolean firstCall = true;
	public static CountDownLatch countDownLatch = new CountDownLatch(1);

	@Override protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager,
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

	@Override protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources,
								   @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
	{
		if (!firstCall) { return; }
		firstCall = false;

		for (Map.Entry<ResourceLocation, JsonElement> resourceEntry : resources.entrySet())
		{
			try
			{
				JsonObject json = resourceEntry.getValue().getAsJsonObject();
				MutablePair<JsonArray, JsonArray> predictionPair = new MutablePair<>(null, null);

				for (Map.Entry<String, JsonElement> entry : json.entrySet())
				{
					String key = entry.getKey();
					JsonElement value = entry.getValue();

					if (key.equals("conditions")) { predictionPair.left = value.getAsJsonArray(); }
					else if (key.equals("operations")) { predictionPair.right = value.getAsJsonArray(); }
					else if (key.startsWith("common/")) { common.add(new ImmutablePair<>(key, value.getAsJsonObject())); }
					else if (key.startsWith("tag/")) { tags.add(new ImmutablePair<>(key, value.getAsJsonObject())); }
					else if (key.startsWith("parent/")) { tags.add(new ImmutablePair<>(null, value.getAsJsonObject())); }
				}

				if (predictionPair.left != null && predictionPair.right != null) { predictions.add(predictionPair); }
			}
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to load resource: " + resourceEntry.getKey().toString());
			}
		}

		countDownLatch.countDown();
	}
}
