package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class DataComponentSuggestion extends CustomSuggestion
{
	private final String withNamespace;
	private final @Nullable String withoutNamespace;

	public DataComponentSuggestion(ResourceLocation id, @Nullable String subtext, boolean relevant, boolean addSuffix)
	{
		super(subtext, relevant ? 0 : -1);
		this.withNamespace = id.toString() + (addSuffix ? "=" : "");
		this.withoutNamespace = id.getNamespace().equals("minecraft") ? (id.getPath() + (addSuffix ? "=" : "")) : null;
	}

	@Override public String getText()
	{
		return (ModConfig.hideMcNamespaceInComponents.val && withoutNamespace != null) ? withoutNamespace : withNamespace;
	}

	@Override public boolean match(String str)
	{
		return str.equals(withNamespace) || str.equals(withoutNamespace);
	}

	@Override public boolean matchPrefix(String prefix)
	{
		return matchPrefix(getText(), prefix) || (withoutNamespace != null && matchPrefix(withoutNamespace, prefix));
	}
}
