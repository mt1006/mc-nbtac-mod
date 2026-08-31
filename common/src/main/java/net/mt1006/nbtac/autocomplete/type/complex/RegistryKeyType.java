package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class RegistryKeyType extends ComplexType
{
	private final @Nullable ResourceKey<Registry<Object>> registryKey;
	private final Contents contents;

	public RegistryKeyType(@Nullable String arg)
	{
		this(arg, Contents.KEYS);
	}

	public RegistryKeyType(@Nullable String arg, Contents contents)
	{
		super(PrimitiveType.STRING);
		ResourceLocation id = arg != null ? ResourceLocation.tryParse(arg) : null;
		this.registryKey = id != null ? ResourceKey.createRegistryKey(id) : null;
		this.contents = contents;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (registryKey == null) { return; }
		String subtext = "[#" + registryKey.location().getPath() + "]";

		if (Minecraft.getInstance().level == null) { return; }
		Registry<?> registry = Minecraft.getInstance().level.registryAccess().lookup(registryKey).orElse(null);
		if (registry == null) { return; }

		if (contents.includeKeys)
		{
			registry.keySet().forEach((id) -> list.add(new IdSuggestion(id, subtext, ctx.parserType(), 0, contents.keysAsTags)));
		}
		if (contents.includeTags)
		{
			registry.getTags().forEach((t) -> list.add(new IdSuggestion(t.key().location(), subtext, ctx.parserType(), 0, true)));
		}
	}

	public enum Contents
	{
		KEYS(true, false, false),
		TAGS(false, true, false),
		BOTH(true, true, false),
		BOTH_PREFIXED(true, true, true);

		public final boolean includeKeys;
		public final boolean includeTags;
		public final boolean keysAsTags;

		Contents(boolean includeKeys, boolean includeTags, boolean keysAsTags)
		{
			this.includeKeys = includeKeys;
			this.includeTags = includeTags;
			this.keysAsTags = keysAsTags;
		}
	}
}
