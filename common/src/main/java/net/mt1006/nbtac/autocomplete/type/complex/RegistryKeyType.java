package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class RegistryKeyType extends ComplexType
{
	private final @Nullable ResourceKey<Registry<Object>> registryKey;

	public RegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		Identifier id = arg != null ? Identifier.parse(arg) : null;
		registryKey = id != null ? ResourceKey.createRegistryKey(id) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryKey == null) { return; }
		String subtext = "[#" + registryKey.identifier().getPath() + "]";

		if (Minecraft.getInstance().level == null) { return; }
		Registry<?> registry = Minecraft.getInstance().level.registryAccess().lookup(registryKey).orElse(null);
		if (registry == null) { return; }

		registry.keySet().forEach((id) -> list.add(new IdSuggestion(id, subtext, ctx.parserType())));
	}
}
