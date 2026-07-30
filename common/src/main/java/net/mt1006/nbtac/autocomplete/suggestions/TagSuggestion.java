package net.mt1006.nbtac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import org.jetbrains.annotations.Nullable;

public class TagSuggestion extends StringSuggestion
{
	private final Message tooltip;

	protected TagSuggestion(NbtTag tag, String tagName, ParserType parserType, StringType stringType, int priority)
	{
		super(tagName, tag.getSubtext(), parserType, stringType, priority);
		this.tooltip = tag.getTooltip();
	}

	public static TagSuggestion create(NbtTag tag, ParserType parserType, @Nullable ParsedCompound compound)
	{
		return tag.getNameAsId() != null
				? new IdTagSuggestion(tag, tag.getNameAsId(), parserType, tag.getPriority(compound))
				: new TagSuggestion(tag, tag.getName(), parserType, StringType.TAG, tag.getPriority(compound));
	}

	@Override public @Nullable Message getTooltip()
	{
		return tooltip;
	}
}
