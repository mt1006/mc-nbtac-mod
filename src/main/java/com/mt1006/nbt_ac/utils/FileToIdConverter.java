package com.mt1006.nbt_ac.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileToIdConverter
{
	private final String prefix;
	private final String extension;

	public FileToIdConverter(String prefix, String extension)
	{
		this.prefix = prefix;
		this.extension = extension;
	}

	public static FileToIdConverter json(String prefix)
	{
		return new FileToIdConverter(prefix, ".json");
	}

	public ResourceLocation fileToId(ResourceLocation resLoc)
	{
		String path = resLoc.getPath();
		return withPath(resLoc, path.substring(this.prefix.length() + 1, path.length() - this.extension.length()));
	}

	public Map<ResourceLocation, Resource> listMatchingResources(ResourceManager resLoc)
	{
		Collection<ResourceLocation> resources = resLoc.listResources(this.prefix, (res) -> res.endsWith(this.extension));
		Map<ResourceLocation, Resource> map = new HashMap<>();

		for (ResourceLocation res : resources)
		{
			try
			{
				map.put(res, resLoc.getResource(res));
			}
			catch (Exception ignore) {}
		}
		return map;
	}

	private static ResourceLocation withPath(ResourceLocation resLoc, String path)
	{
		return new ResourceLocation(resLoc.getNamespace(), resLoc.getPath());
	}
}