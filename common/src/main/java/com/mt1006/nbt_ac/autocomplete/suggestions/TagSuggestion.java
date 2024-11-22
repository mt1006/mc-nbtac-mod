package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import org.jetbrains.annotations.Nullable;

public class TagSuggestion extends StringSuggestion
{
	private final NbtSuggestion sourceSuggestion;
	private final Message tooltip;

	protected TagSuggestion(NbtSuggestion suggestion, @Nullable String tag, CustomTagParser.Type parserType, int priority)
	{
		super(tag == null ? suggestion.tag : tag, suggestion.getSubtext(), parserType,
				tag == null ? StringType.TAG : StringType.TAG_ID, priority);
		this.sourceSuggestion = suggestion;
		this.tooltip = suggestion.getTooltip();
	}

	public TagSuggestion(NbtSuggestion suggestion, CustomTagParser.Type parserType, int priority)
	{
		this(suggestion, null, parserType, priority);
	}

	public TagSuggestion(NbtSuggestion suggestion, CustomTagParser.Type parserType)
	{
		this(suggestion, null, parserType, suggestion.recommended ? 100 : 0);
	}

	public NbtSuggestion getSourceSuggestion()
	{
		return sourceSuggestion;
	}

	@Override public @Nullable Message getTooltip()
	{
		return tooltip;
	}
}
