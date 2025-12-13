package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class TagIdSuggestion extends TagSuggestion
{
	private final @Nullable String altText;

	public TagIdSuggestion(NbtSuggestion suggestion, @Nullable Identifier id,
						   CustomTagParser.Type parserType, boolean relevant)
	{
		super(suggestion, id != null ? id.toString() : "_error", parserType, relevant ? 0 : -1);

		boolean mcNamespaceHidden = ModConfig.hideMcNamespaceInTags.val && parserType.requiresNamespace;
		this.altText = (id != null && id.getNamespace().equals("minecraft"))
				? (mcNamespaceHidden ? String.format("minecraft:%s", text) : id.getPath())
				: null;
	}

	@Override public boolean match(String str)
	{
		return str.equals(text) || str.equals(altText);
	}

	@Override public boolean matchUnfinished(String str)
	{
		return matchPrefix(text, str) || (altText != null && matchPrefix(altText, str));
	}
}
