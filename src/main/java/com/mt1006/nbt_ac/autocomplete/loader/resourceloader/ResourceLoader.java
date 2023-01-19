package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ResourceLoader implements SimpleSynchronousResourceReloadListener
{
	private static final String RESOURCE_DIRECTORY = "nbt_ac_suggestions";
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	public static final List<Map.Entry<String, JsonElement>> listOfEntries = new ArrayList<>();
	public static boolean firstCall = true;
	public static CountDownLatch countDownLatch = new CountDownLatch(1);

	@Override
	public ResourceLocation getFabricId()
	{
		return new ResourceLocation("nbt_ac", "nbt_ac");
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
		if (!firstCall) { return; }
		firstCall = false;

		Map<ResourceLocation, JsonElement> resources = prepare(resourceManager);

		for (Map.Entry<ResourceLocation, JsonElement> resourceEntry : resources.entrySet())
		{
			try
			{
				if (!(resourceEntry.getValue() instanceof JsonObject)) { throw new Exception(); }
				JsonObject json = (JsonObject)resourceEntry.getValue();
				listOfEntries.addAll(json.entrySet());
			}
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to load resource: " + resourceEntry.getKey().toString());
			}
		}

		countDownLatch.countDown();
	}

	private Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager)
	{
		Gson gson = new Gson();
		Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
		Collection<ResourceLocation> resources = resourceManager.listResources(RESOURCE_DIRECTORY, (path) -> path.endsWith(".json"));

		for (ResourceLocation resourceLocation : resources)
		{
			String resourcePath = resourceLocation.getPath();
			ResourceLocation finalResourceLocation = new ResourceLocation(resourceLocation.getNamespace(),
					resourcePath.substring(RESOURCE_DIRECTORY.length(), resourcePath.length() - PATH_SUFFIX_LENGTH));

			try (Reader reader = new BufferedReader(new InputStreamReader(
					resourceManager.getResource(resourceLocation).getInputStream(), StandardCharsets.UTF_8)))
			{
				JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
				map.put(finalResourceLocation, jsonElement);
			}
			catch (Exception ignore) {}
		}

		return map;
	}
}
