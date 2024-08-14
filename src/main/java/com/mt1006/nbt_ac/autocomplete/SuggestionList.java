package com.mt1006.nbt_ac.autocomplete;

import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.RawSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.TagSuggestion;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SuggestionList extends ArrayList<CustomSuggestion>
{
	public void addRaw(String text, @Nullable String subtext)
	{
		add(new RawSuggestion(text, subtext));
	}

	public void addRaw(String text, @Nullable String subtext, int priority)
	{
		add(new RawSuggestion(text, subtext, priority));
	}

	public void addAll(@Nullable NbtSuggestions suggestions, CustomTagParser.Type parserType, int priority)
	{
		if (suggestions == null) { return; }
		suggestions.getAll().forEach((s) -> add(new TagSuggestion(s, parserType, priority)));
	}

	public void addAll(@Nullable NbtSuggestions suggestions, CustomTagParser.Type parserType)
	{
		if (suggestions == null) { return; }
		suggestions.getAll().forEach((s) -> add(new TagSuggestion(s, parserType)));
	}

	public void addAll(@Nullable NbtSuggestions suggestions, @Nullable String rootTag, CustomTagParser.Type parserType)
	{
		addAll(suggestions, parserType);
		getCommonSuggestions(rootTag).forEach((s) -> addAll(s, parserType));
	}

	public void replaceWith(Collection<CustomSuggestion> suggestions)
	{
		clear();
		addAll(suggestions);
	}

	private static List<NbtSuggestions> getCommonSuggestions(@Nullable String tag)
	{
		if (tag == null) { return List.of(); }
		List<NbtSuggestions> list = new ArrayList<>();

		if (tag.startsWith("item/"))
		{
			Item item = RegistryUtils.ITEM.get(tag.substring(5));
			if (item != null)
			{
				if (item instanceof BlockItem) { list.add(NbtSuggestionManager.get("common/block_item")); }
				if (item instanceof SpawnEggItem) { list.add(NbtSuggestionManager.get("common/spawn_egg_item")); }
				if (item.canBeDepleted()) { list.add(NbtSuggestionManager.get("common/damageable")); }
			}
			list.add(NbtSuggestionManager.get("common/item"));
		}
		else if (tag.startsWith("block/"))
		{
			list.add(NbtSuggestionManager.get("common/block"));
		}
		else if (tag.startsWith("entity/"))
		{
			list.add(NbtSuggestionManager.get("common/entity"));
		}

		list.removeIf(Objects::isNull);
		return list;
	}
}
