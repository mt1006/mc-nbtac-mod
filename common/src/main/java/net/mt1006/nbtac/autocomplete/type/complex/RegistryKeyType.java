package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public class RegistryKeyType extends ComplexType
{
	private final @Nullable ResourceLocation registryId;

	public RegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		registryId = arg != null ? ResourceLocation.tryParse(arg) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryId == null) { return; }

		Registry<?> registry = RegistryUtils.REGISTRY.get(registryId);
		if (registry == null) { return; }

		String subtext = "[#" + registryId.getPath() + "]";
		for (ResourceKey<?> id : registry.registryKeySet())
		{
			list.add(new IdSuggestion(id.location(), subtext, ctx.parserType()));
		}
	}
}
