package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.Property;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.complex.EnumType;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockStateTagsType extends ComplexCompoundType
{
	private final @Nullable Identifier id;

	public BlockStateTagsType(@Nullable String id)
	{
		if (id != null)
		{
			if (id.startsWith("block/")) { id = id.substring(6); }
			else if (id.startsWith("item/")) { id = id.substring(5); }
		}
		this.id = id != null ? Identifier.tryParse(id) : null;
	}

	@Override protected void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsedCompound, NbtTagMap map)
	{
		if (id == null) { return; }

		Item blockItem = RegistryUtils.ITEM.get(id);
		if (!(blockItem instanceof BlockItem)) { return; }

		for (Property<?> property : ((BlockItem)blockItem).getBlock().defaultBlockState().getProperties())
		{
			EnumType type = new EnumType(List.of(PrimitiveType.STRING), buildEnumList(property), false);
			map.add(new GeneratedNbtTag(property.getName(), type));
		}
	}

	private static <T extends Comparable<T>> List<String> buildEnumList(Property<?> property)
	{
		List<String> list = new ArrayList<>();
		for (T possibleValue : ((Property<T>)property).getPossibleValues())
		{
			list.add(((Property<T>)property).getName(possibleValue));
		}
		return list;
	}
}
