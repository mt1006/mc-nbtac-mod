package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class IdSuggestion extends StringSuggestion
{
	private final @Nullable String altText;

	public IdSuggestion(@Nullable Identifier id, @Nullable String subtext, CustomTagParser.Type parserType, int priority)
	{
		super(id != null ? id.toString() : "_error", subtext, parserType,
				parserType.requiresNamespace ? StringType.FULL_ID : StringType.ID, priority);

		boolean mcNamespaceHidden = ModConfig.hideMcNamespaceInStrings.val && parserType.requiresNamespace;
		this.altText = (id != null && id.getNamespace().equals("minecraft"))
				? (mcNamespaceHidden ? String.format("minecraft:%s", text) : id.getPath())
				: null;
	}

	public IdSuggestion(@Nullable Identifier id, @Nullable String subtext, CustomTagParser.Type parserType)
	{
		this(id, subtext, parserType, 0);
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
