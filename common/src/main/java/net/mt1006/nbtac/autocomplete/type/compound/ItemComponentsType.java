package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public class ItemComponentsType extends ComplexCompoundType
{
	private final @Nullable ResourceLocation id;

	public ItemComponentsType(@Nullable String id)
	{
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override protected void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsed, NbtTagMap map)
	{
		map.addAll(CompoundType.buildMapForItem("item/" + id));
	}
}
