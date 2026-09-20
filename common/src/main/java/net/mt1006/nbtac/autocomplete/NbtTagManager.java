package net.mt1006.nbtac.autocomplete;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NbtTagManager
{
	private static final Map<String, NbtTagMap> tagMaps = new ConcurrentHashMap<>();
	public static final Map<ResourceLocation, String> blockToBlockEntityMap = new ConcurrentHashMap<>();
	private static @Nullable NbtTagMap moddedEntityTagMap = null;

	public static void add(String key, NbtTagMap tagMap, DataSource source)
	{
		tagMap.source = source;
		tagMaps.merge(key, tagMap, (m1, m2) -> m1.source.priority > m2.source.priority ? m1 : m2);
	}

	public static @Nullable NbtTagMap get(@Nullable String key)
	{
		if (key == null) { return null; }

		if (key.startsWith("block/"))
		{
			ResourceLocation id = ResourceLocation.tryParse(key.substring(6));
			String blockEntityKey = blockToBlockEntityMap.get(id);
			if (blockEntityKey != null) { key = blockEntityKey; }
		}

		NbtTagMap tagMap = tagMaps.get(key);
		if (tagMap == null && key.startsWith("entity/"))
		{
			ResourceLocation id = ResourceLocation.tryParse(key.substring(7));
			if (id != null && !id.getNamespace().equals("minecraft")) { return getForModdedEntity(); }
		}

		return tagMap;
	}

	private static @Nullable NbtTagMap getForModdedEntity()
	{
		if (moddedEntityTagMap != null) { return moddedEntityTagMap; }

		moddedEntityTagMap = new NbtTagMap();
		getRawMap("_entity/minecraft:_entity").values().forEach(moddedEntityTagMap::add);
		getRawMap("_entity/minecraft:_living_entity").forEach((k, v) ->
				moddedEntityTagMap.add(new GeneratedNbtTag(v, 0, null).withSubtext((s) -> "[?] " + s)));
		getRawMap("_entity/minecraft:_mob").forEach((k, v) ->
				moddedEntityTagMap.add(new GeneratedNbtTag(v, 0, null).withSubtext((s) -> "[??] " + s)));

		return moddedEntityTagMap;
	}

	private static Map<String, NbtTag> getRawMap(String tagMapId)
	{
		NbtTagMap nbtTagMap = NbtTagManager.get(tagMapId);
		if (nbtTagMap == null) { return Map.of(); }

		Map<String, NbtTag> rawMap = nbtTagMap.getRawMap();
		return rawMap != null ? rawMap : Map.of();
	}
}
