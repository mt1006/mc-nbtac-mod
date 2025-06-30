package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.mixin.fields.DecoratedPotPatternsFields;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public class PotDecorationType extends ComplexType
{
	public static final PotDecorationType INSTANCE = new PotDecorationType();

	private PotDecorationType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		ctx.list().add(new IdSuggestion(RegistryUtils.ITEM.getKey(Items.BRICK), "[#pot_decoration]", ctx.parserType(), 1, false));

		Map<Item, ResourceKey<String>> itemToPotTexture = DecoratedPotPatternsFields.getITEM_TO_POT_TEXTURE();
		if (itemToPotTexture == null) { return; }

		for (Item item : itemToPotTexture.keySet())
		{
			ctx.list().add(new IdSuggestion(RegistryUtils.ITEM.getKey(item), "[#pot_decoration]", ctx.parserType()));
		}
	}
}
