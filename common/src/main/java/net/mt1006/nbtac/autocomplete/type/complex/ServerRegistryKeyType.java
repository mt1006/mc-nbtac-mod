package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerRegistryKeyType<T> extends ComplexType
{
	public static final Map<ResourceLocation, List<ResourceLocation>> registryKeyMap = new HashMap<>();
	private final @Nullable ResourceKey<Registry<T>> registryKey;

	public ServerRegistryKeyType(@Nullable String arg)
	{
		super(PrimitiveType.STRING);
		ResourceLocation id = arg != null ? ResourceLocation.parse(arg) : null;
		registryKey = id != null ? ResourceKey.createRegistryKey(id) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryKey == null) { return; }
		String subtext = "[#" + registryKey.location().getPath() + "]";

		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null)
		{
			List<ResourceLocation> idList = registryKeyMap.get(registryKey.location());
			if (idList != null) { idList.forEach((id) -> list.add(new IdSuggestion(id, subtext, ctx.parserType()))); }
		}
		else
		{
			Optional<? extends HolderLookup.RegistryLookup<T>> registry =
					server.reloadableRegistries().lookup().lookup(registryKey);
			if (registry.isEmpty()) { return; }

			for (ResourceKey<T> id : registry.get().listElementIds().toList())
			{
				list.add(new IdSuggestion(id.location(), subtext, ctx.parserType()));
			}
		}
	}
}
