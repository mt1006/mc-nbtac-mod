package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.DefinedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.ListType;
import net.mt1006.nbtac.autocomplete.type.complex.RegistryKeyType;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public class CompoundType extends AbstractCompoundType
{
	private @Nullable NbtTagMap tagMap = null;

	public static CompoundType fromName(@Nullable String name)
	{
		if (name == null) { return new CompoundType(null); }
		if (name.startsWith("item/")) { return new CompoundType(buildMapForItem(name)); }

		NbtTagMap tagMap = NbtTagManager.get(name);
		return (name.startsWith("entity/") || name.startsWith("block/"))
				? new EntityCompoundType(tagMap, name.substring(name.indexOf('/') + 1))
				: new CompoundType(tagMap);
	}

	public static @Nullable NbtTagMap buildMapForItem(String name)
	{
		String idStr = name.substring(5);
		ResourceLocation id = ResourceLocation.tryParse(idStr);
		if (id == null) { return null; }

		NbtTagMap tagMap = new NbtTagMap();
		tagMap.addAll(NbtTagManager.get("_item/minecraft:_common"));
		if (id.getNamespace().equals("minecraft")) { tagMap.addAll(NbtTagManager.get("item/" + id)); }

		Item item = RegistryUtils.ITEM.get(id);
		if (item == null) { return tagMap; }

		if (item instanceof BlockItem)
		{
			tagMap.add(new GeneratedNbtTag("BlockEntityTag", new TagsType("block/" + idStr, null, null)));
			tagMap.add(new GeneratedNbtTag("BlockStateTag", new BlockStateTagsType(idStr)));
			tagMap.add(new GeneratedNbtTag("CanPlaceOn", new ListType(new RegistryKeyType("minecraft:block"))));
		}
		if (item instanceof SpawnEggItem spawnEgg)
		{
			TagsType dataTagType = new TagsType("entity/" + RegistryUtils.ENTITY_TYPE.getKey(spawnEgg.getType(null)), null, null);
			tagMap.add(new GeneratedNbtTag("EntityTag", dataTagType));
		}
		return tagMap;
	}

	public CompoundType() {}

	protected CompoundType(@Nullable NbtTagMap tagMap)
	{
		this.tagMap = tagMap;
	}

	@Override public NbtTagMap getMutableTagMap()
	{
		if (tagMap == null) { tagMap = new NbtTagMap(); }
		return tagMap;
	}

	@Override public void setTagMap(@Nullable NbtTagMap subcompound)
	{
		tagMap = subcompound;
	}

	@Override public @Nullable NbtTagMap getSuggestionsTagMap(ParsedCompound parsed)
	{
		return tagMap;
	}

	public boolean hasTagMap()
	{
		return tagMap != null;
	}
}
