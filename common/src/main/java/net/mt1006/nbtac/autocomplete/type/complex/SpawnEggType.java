package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.mt1006.nbtac.autocomplete.NbtSuggestionManager;
import net.mt1006.nbtac.autocomplete.NbtSuggestionMap;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public class SpawnEggType extends ComplexType
{
	private final @Nullable ResourceLocation id;

	public SpawnEggType(@Nullable String id)
	{
		super(PrimitiveType.COMPOUND);
		if (id != null && id.startsWith("item/")) { id = id.substring(5); }
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: todo
	}

	@Override public void getCompoundSuggestions(SuggestionListContext ctx)
	{
		if (id == null) { return; }
		Item item = RegistryUtils.ITEM.get(id);

		try
		{
			if (item instanceof SpawnEggItem)
			{
				String key = RegistryUtils.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null, new ItemStack(item))).toString();
				NbtSuggestionMap spawnEggSuggestions = NbtSuggestionManager.get("entity/" + key);
				ctx.list().addAll(spawnEggSuggestions, id.toString(), ctx.parserType());
			}
		}
		catch (Exception ignore) {}
	}
}
