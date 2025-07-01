package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.CustomTagParser;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class TagIdSuggestion extends TagSuggestion
{
	private final @Nullable String altText;

	public TagIdSuggestion(NbtSuggestion suggestion, @Nullable ResourceLocation resLoc,
						   CustomTagParser.Type parserType, boolean relevant)
	{
		super(suggestion, resLoc != null ? resLoc.toString() : "_error", parserType, relevant ? 0 : -1);

		boolean mcNamespaceHidden = ModConfig.hideMcNamespaceInTags.val && parserType.requiresNamespace;
		this.altText = (resLoc != null && resLoc.getNamespace().equals("minecraft"))
				? (mcNamespaceHidden ? String.format("minecraft:%s", text) : resLoc.getPath())
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
