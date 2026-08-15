package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class RegistryIdType extends ComplexType
{
	private final @Nullable ResourceKey<Registry<Object>> registryKey;

	public RegistryIdType(@Nullable String arg)
	{
		super(PrimitiveType.INT);
		ResourceLocation id = arg != null ? ResourceLocation.tryParse(arg) : null;
		registryKey = id != null ? ResourceKey.createRegistryKey(id) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryKey == null) { return; }

		if (Minecraft.getInstance().level == null) { return; }
		Registry<?> registry = Minecraft.getInstance().level.registryAccess().registry(registryKey).orElse(null);
		if (registry == null) { return; }

		addRegistryIds(list, registry, registryKey.location().getPath());
	}

	private <T> void addRegistryIds(SuggestionList list, Registry<T> registry, String registryName)
	{
		for (T object : registry)
		{
			list.addRaw(Integer.toString(registry.getId(object)), "\"" + registry.getKey(object) + "\" [#" + registryName + "]");
		}
	}
}
