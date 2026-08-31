package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ItemComponentsType extends ComplexCompoundType
{
	private final @Nullable Identifier id;

	public ItemComponentsType(@Nullable String id)
	{
		this.id = id != null ? Identifier.tryParse(id) : null;
	}

	@Override protected void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map)
	{
		DataComponentManager.loadTagMap(map, "", Set.of(), id != null ? RegistryUtils.ITEM.get(id) : null);
	}
}
