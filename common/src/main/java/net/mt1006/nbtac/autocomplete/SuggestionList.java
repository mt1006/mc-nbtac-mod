package net.mt1006.nbtac.autocomplete;

import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.RawSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SuggestionList implements Iterable<CustomSuggestion>
{
	private final List<CustomSuggestion> list = new ArrayList<>();
	public int cursor = 0;

	public SuggestionList() {}

	public SuggestionList(int cursor)
	{
		this.cursor = cursor;
	}

	public static SuggestionList empty()
	{
		return new SuggestionList();
	}

	public void add(CustomSuggestion suggestion)
	{
		list.add(suggestion);
	}

	public void addRaw(String text, @Nullable String subtext)
	{
		add(new RawSuggestion(text, subtext));
	}

	public void addRaw(String text, @Nullable String subtext, int priority)
	{
		add(new RawSuggestion(text, subtext, priority));
	}

	public SuggestionList withOperators(String... ops)
	{
		for (String op : ops)
		{
			add(new RawSuggestion(op, null));
		}
		return this;
	}

	public boolean hasMatch(String str)
	{
		for (CustomSuggestion suggestion : this)
		{
			if (suggestion.match(str)) { return true; }
		}
		return false;
	}

	public void removeByName(@Nullable String str)
	{
		if (str == null) { return; }
		list.removeIf((s) -> s.match(str));
	}

	public void filterByPrefix(String prefix)
	{
		list.removeIf((s) -> !s.matchPrefix(prefix));
	}

	public @Nullable SuggestionList matchOrFiler(String str)
	{
		if (hasMatch(str)) { return null; }

		filterByPrefix(str);
		return this;
	}

	@Override public @NotNull Iterator<CustomSuggestion> iterator()
	{
		return list.iterator();
	}
}
