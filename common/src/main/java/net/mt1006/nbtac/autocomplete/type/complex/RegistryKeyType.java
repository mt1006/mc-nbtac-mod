package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public class RegistryKeyType extends ComplexType
{
	private final @Nullable Identifier registryId;

	public RegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		registryId = arg != null ? Identifier.parse(arg) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryId == null) { return; }

		Holder.Reference<? extends Registry<?>> ref = RegistryUtils.REGISTRY.get(registryId).orElse(null);
		Registry<?> registry = ref != null ? ref.value() : null;
		if (registry == null) { return; }

		String subtext = "[#" + registryId.getPath() + "]";
		for (ResourceKey<?> id : registry.listElementIds().toList())
		{
			list.add(new IdSuggestion(id.identifier(), subtext, ctx.parserType()));
		}
	}
}
