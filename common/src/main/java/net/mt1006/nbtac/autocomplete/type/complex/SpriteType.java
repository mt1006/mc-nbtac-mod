package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.AtlasEntryFields;
import net.mt1006.nbtac.mixin.fields.AtlasManagerFields;
import net.mt1006.nbtac.mixin.fields.TextureAtlasFields;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class SpriteType extends ComplexType
{
	private final @Nullable ResourceLocation atlasId;

	public SpriteType(String atlasId)
	{
		super(PrimitiveType.STRING);
		this.atlasId = atlasId != null ? ResourceLocation.tryParse(atlasId) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (atlasId == null) { return; }

		Map<ResourceLocation, AtlasEntryFields> atlasById = ((AtlasManagerFields)Minecraft.getInstance().getAtlasManager()).nbtac$getAtlasById();
		AtlasEntryFields atlasEntry = atlasById.get(atlasId);
		if (atlasEntry == null) { return; }

		Collection<ResourceLocation> sprites = ((TextureAtlasFields)atlasEntry.nbtac$getAtlas()).nbtac$getTexturesByName().keySet();
		sprites.forEach((id) -> list.add(new IdSuggestion(id, "[#sprite]", ctx.parserType())));
	}
}
