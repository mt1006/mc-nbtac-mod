package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.DecoratedPotRendererFields;
import net.mt1006.nbtac.utils.RegistryUtils;

import java.util.Map;

public class PotDecorationType extends ComplexType
{
	public static final PotDecorationType INSTANCE = new PotDecorationType();

	private PotDecorationType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		list.add(new IdSuggestion(RegistryUtils.ITEM.getKey(Items.BRICK), "[#pot_decoration]", ctx.parserType(), 1, false));

		Map<ResourceKey<Item>, SpriteId> itemToPotTexture = DecoratedPotRendererFields.getDECORATED_POT_SPRITES();
		if (itemToPotTexture == null) { return; }

		for (ResourceKey<Item> item : itemToPotTexture.keySet())
		{
			list.add(new IdSuggestion(item.identifier(), "[#pot_decoration]", ctx.parserType()));
		}
	}
}
