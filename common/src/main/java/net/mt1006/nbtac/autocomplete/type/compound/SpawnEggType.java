package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public class SpawnEggType extends ComplexCompoundType
{
	private final @Nullable ResourceLocation id;

	public SpawnEggType(@Nullable String id)
	{
		if (id != null && id.startsWith("item/")) { id = id.substring(5); }
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override protected void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map)
	{
		if (id == null) { return; }
		Item item = RegistryUtils.ITEM.get(id);

		try
		{
			if (item instanceof SpawnEggItem)
			{
				String key = RegistryUtils.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null, new ItemStack(item))).toString();
				NbtTagMap spawnEggTags = NbtTagManager.get("entity/" + key);
				map.addAll(spawnEggTags);
			}
		}
		catch (Exception ignore) {}
	}
}
