package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import org.jetbrains.annotations.Nullable;

public class IdTagSuggestion extends TagSuggestion
{
	public IdTagSuggestion(NbtTag tag, @Nullable ResourceLocation id, ParserType parserType, int priority)
	{
		super(tag, id != null ? id.toString() : "error", parserType, StringType.ID_TAG, priority);

		if (id != null && id.getNamespace().equals("minecraft"))
		{
			matching.add(withHiddenNamespace ? id.toString() : id.getPath());
		}
	}
}
