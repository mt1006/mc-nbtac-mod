package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.autocomplete.type.complex.IdType;
import net.mt1006.nbtac.autocomplete.type.complex.RegistryKeyType;
import org.jetbrains.annotations.Nullable;

public class TagsType extends ComplexCompoundType
{
	private final @Nullable String id;
	private final @Nullable String keyId;
	private final String idKey;

	public TagsType(@Nullable String id, @Nullable String keyId, @Nullable String idKey)
	{
		if (id != null)
		{
			id = id.replace("block/item/", "block/");
			id = id.replace("entity/item/", "entity/");
		}
		this.id = id;
		this.keyId = keyId;
		this.idKey = idKey;
	}

	@Override protected void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map)
	{
		if (idKey != null)
		{
			Type type = null;
			if (keyId != null)
			{
				type = new RegistryKeyType(keyId);
			}
			else if (id != null)
			{
				String finalId = id;
				if (id.startsWith("block/"))
				{
					ResourceLocation requiredId = ResourceLocation.tryParse(id.substring(id.indexOf('/') + 1));
					if (requiredId != null) { finalId = NbtTagManager.blockToBlockEntityMap.getOrDefault(requiredId, id); }
				}
				type = new IdType(finalId.substring(id.indexOf('/') + 1));
			}

			map.add(new GeneratedNbtTag(idKey, type, 200, null));
		}

		if (id == null) { return; }
		NbtTagMap tagMap = NbtTagManager.get(idWithNamespace(id));
		map.addAll(tagMap);
	}

	private static String idWithNamespace(String idStr)
	{
		int slashPos = idStr.indexOf('/');
		if (slashPos == -1) { return idStr; } // shouldn't happen

		String prefix = idStr.substring(0, slashPos + 1);
		ResourceLocation id = ResourceLocation.tryParse(idStr.substring(slashPos + 1));
		if (id == null) { return idStr; }

		return prefix + id;
	}
}
