package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceLoader extends SimpleJsonResourceReloadListener
{
	private static final String RESOURCE_DIRECTORY = "nbt_ac_suggestions";
	public static final List<Map.Entry<String, JsonElement>> listOfEntries = new ArrayList<>();
	public static boolean firstCall = true;

	public ResourceLoader()
	{
		super(new Gson(), RESOURCE_DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profilerFiller)
	{
		if (!firstCall) { return; }
		firstCall = false;

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
	}
}
