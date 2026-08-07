package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerRegistryKeyType extends ComplexType
{
	public static final Map<Identifier, List<Identifier>> registryKeyMap = new HashMap<>();
	private final @Nullable ResourceKey<Registry<Object>> registryKey;

	public ServerRegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		Identifier id = arg != null ? Identifier.parse(arg) : null;
		registryKey = id != null ? ResourceKey.createRegistryKey(id) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		//TODO: remove serverreg
		//TODO: merge with RegistryKey, using this code?
		if (registryKey == null) { return; }
		String subtext = "[#" + registryKey.identifier().getPath() + "]";

		if (Minecraft.getInstance().level == null) { return; }
		Registry<?> registry = Minecraft.getInstance().level.registryAccess().lookup(registryKey).orElse(null);
		if (registry == null) { return; }

		registry.keySet().forEach((id) -> list.add(new IdSuggestion(id, subtext, ctx.parserType())));
	}
}
