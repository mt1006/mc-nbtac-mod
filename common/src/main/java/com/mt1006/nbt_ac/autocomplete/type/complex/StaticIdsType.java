package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.List;

public class StaticIdsType extends ComplexType
{
	public static final StaticIdsType BANNER_PATTERN = new StaticIdsType(BannerPatterns.class, "banner_pattern", false);
	public static final StaticIdsType TRIM_PATTERN = new StaticIdsType(TrimPatterns.class, "trim_pattern", false);
	public static final StaticIdsType TRIM_MATERIAL = new StaticIdsType(TrimMaterial.class, "trim_material", false);
	public static final StaticIdsType JUKEBOX_SONG = new StaticIdsType(JukeboxSongs.class, "jukebox_song", false);
	public static final StaticIdsType DAMAGE_TYPE = new StaticIdsType(DamageTypeTags.class, "damage_type", true);
	private final String subtext;
	private final List<ResourceKey> resourceKeys;
	private final boolean isTagId;

	private StaticIdsType(Class<?> fromClass, String subtextId, boolean isTagId)
	{
		super(PrimitiveType.STRING);
		this.subtext = "[#" + subtextId + "]";
		this.resourceKeys = Fields.getStaticFields(fromClass, ResourceKey.class);
		this.isTagId = isTagId;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		resourceKeys.forEach((key) -> ctx.list().add(new IdSuggestion(key.location(), subtext, ctx.parserType(), 0, isTagId)));
	}
}
