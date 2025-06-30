package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RegistryKeyType extends ComplexType
{
	private final @Nullable ResourceLocation registryId;
	private final @Nullable Registry<?> registry;

	public RegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		if (arg != null)
		{
			registryId = ResourceLocation.parse(arg);
			Holder.Reference<? extends Registry<?>> ref = RegistryUtils.REGISTRY.get(registryId).orElse(null);
			registry = ref != null ? ref.value() : null;
		}
		else
		{
			registryId = null;
			registry = null;
		}
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		if (registryId == null || registry == null) { return; }
		String subtext = "[#" + registryId.getPath() + "]";
		for (Object obj : registry)
		{
			ctx.list().add(new IdSuggestion(((Registry<Object>)registry).getKey(obj), subtext, ctx.parserType()));
		}
	}
}
