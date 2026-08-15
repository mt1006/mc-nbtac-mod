package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class DataComponentSuggestion extends CustomSuggestion
{
	private final String withNamespace;
	private final @Nullable String withoutNamespace;

	public DataComponentSuggestion(NbtTag tag, boolean addEqualSign)
	{
		super(tag.getSubtext(), tag.getPriority(null));

		ResourceLocation id = tag.getNameAsId();
		if (id == null) { id = ResourceLocation.tryParse("error:error");  }

		this.withNamespace = id + (addEqualSign ? "=" : "");
		this.withoutNamespace = id.getNamespace().equals("minecraft") ? (id.getPath() + (addEqualSign ? "=" : "")) : null;
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
