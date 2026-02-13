package net.mt1006.nbtac.autocomplete;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NbtTagManager
{
	private static final Map<String, NbtTagMap> tagMaps = new HashMap<>();
	public static final Map<ResourceLocation, String> blockToBlockEntityMap = new HashMap<>();

	public static void add(String key, @Nullable NbtTagMap tagMap)
	{
		tagMaps.put(key, tagMap);
	}

	public static @Nullable NbtTagMap get(@Nullable String key)
	{
		if (key == null) { return null; }

		if (key.startsWith("entity/"))
		{
			ResourceLocation id = ResourceLocation.tryParse(key.substring(7));
			if (id != null && !id.getNamespace().equals("minecraft")) { return getForModdedEntity(id); }
		}
		else if (key.startsWith("block/"))
		{
			ResourceLocation id = ResourceLocation.tryParse(key.substring(6));
			String blockEntityKey = blockToBlockEntityMap.get(id);
			if (blockEntityKey != null) { key = blockEntityKey; }
		}

		return tagMaps.get(key);
	}

	public static Set<Map.Entry<String, NbtTagMap>> tagMapSet()
	{
		return tagMaps.entrySet();
	}

	private static @Nullable NbtTagMap getForModdedEntity(ResourceLocation id)
	{
		//TODO: finish
		return null;
	}
}
