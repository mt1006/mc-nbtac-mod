package net.mt1006.nbtac.autocomplete;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NbtTagManager
{
	private static final Map<String, NbtTagMap> tagMaps = new ConcurrentHashMap<>();
	public static final Map<Identifier, String> blockToBlockEntityMap = new HashMap<>();
	private static @Nullable NbtTagMap moddedEntityTagMap = null;

	public static void add(String key, NbtTagMap tagMap)
	{
		tagMaps.put(key, tagMap);
	}

	public static @Nullable NbtTagMap get(@Nullable String key)
	{
		if (key == null) { return null; }

		if (key.startsWith("entity/"))
		{
			Identifier id = Identifier.tryParse(key.substring(7));
			if (id != null && !id.getNamespace().equals("minecraft")) { return getForModdedEntity(); }
		}
		else if (key.startsWith("block/"))
		{
			Identifier id = Identifier.tryParse(key.substring(6));
			String blockEntityKey = blockToBlockEntityMap.get(id);
			if (blockEntityKey != null) { key = blockEntityKey; }
		}

		return tagMaps.get(key);
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
