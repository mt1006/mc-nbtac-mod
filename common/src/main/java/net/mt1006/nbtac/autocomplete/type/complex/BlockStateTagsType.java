package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.Property;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.TagSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.RegistryUtils;
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
