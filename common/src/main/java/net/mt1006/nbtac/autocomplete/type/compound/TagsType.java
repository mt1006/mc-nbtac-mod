package net.mt1006.nbtac.autocomplete.type.compound;

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
	private final boolean withId;

	public TagsType(@Nullable String id, @Nullable String keyId, boolean withId)
	{
		if (id != null)
		{
			id = id.replace("block/item/", "block/");
			id = id.replace("entity/item/", "entity/");
		}
		this.id = id;
		this.keyId = keyId;
		this.withId = withId;
	}

	@Override protected void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsed, NbtTagMap map)
	{
		if (withId)
		{
			Type type = null;
			if (keyId != null) { type = new RegistryKeyType(keyId); }
			else if (id != null) { type = new IdType(id.substring(id.indexOf('/') + 1)); }
			map.add(new GeneratedNbtTag("id", type, 100, null));
		}

		if (id == null) { return; }
		NbtTagMap tagMap = NbtTagManager.get(id);
		map.addAll(tagMap);
	}
}
