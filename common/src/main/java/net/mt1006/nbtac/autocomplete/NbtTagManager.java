package net.mt1006.nbtac.autocomplete;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NbtTagManager
{
	private static final Map<String, NbtTagMap> tagMaps = new HashMap<>();

	public static void add(String key, NbtTagMap tagMap)
	{
		tagMaps.put(key, tagMap);
	}

	public static @Nullable NbtTagMap get(String key)
	{
		return key != null ? tagMaps.get(key) : null;
	}

	public static Set<Map.Entry<String, NbtTagMap>> tagMapSet()
	{
		return tagMaps.entrySet();
	}
}
