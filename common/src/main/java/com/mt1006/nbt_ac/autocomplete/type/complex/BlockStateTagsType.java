package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.TagSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockStateTagsType extends ComplexType
{
	private final @Nullable ResourceLocation id;

	public BlockStateTagsType(@Nullable String id)
	{
		super(PrimitiveType.COMPOUND);
		if (id != null)
		{
			if (id.startsWith("block/")) { id = id.substring(6); }
			else if (id.startsWith("item/")) { id = id.substring(5); }
		}
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		if (id == null) { return; }

		Item blockItem = RegistryUtils.ITEM.get(id);
		if (!(blockItem instanceof BlockItem)) { return; }

		for (Property<?> property : ((BlockItem)blockItem).getBlock().defaultBlockState().getProperties())
		{
			EnumType type = new EnumType(PrimitiveType.STRING, buildEnumList(property), false);
			ctx.list().add(new TagSuggestion(new NbtSuggestion(property.getName(), type), ctx.parserType()));
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
