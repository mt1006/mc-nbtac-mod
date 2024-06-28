package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ComponentSuggestion extends CustomSuggestion
{
	private final @Nullable String textWithoutNamespace;

	public ComponentSuggestion(@Nullable ResourceLocation resLoc, @Nullable String subtext, boolean relevant)
	{
		super(resLoc != null ? (resLoc + "=") : "_error", subtext, relevant ? 0 : -1);

		this.textWithoutNamespace = (resLoc != null && resLoc.getNamespace().equals("minecraft"))
				? (resLoc.getPath() + "=")
				: null;
	}

	@Override public String getVisibleText()
	{
		return (ModConfig.hideMcNamespaceInComponents.val && textWithoutNamespace != null) ? textWithoutNamespace : text;
	}

	@Override public boolean match(String str)
	{
		return str.equals(text) || str.equals(textWithoutNamespace);
	}

	@Override public boolean matchUnfinished(String str)
	{
		return matchPrefix(text, str) || (textWithoutNamespace != null && matchPrefix(textWithoutNamespace, str));
	}
}
