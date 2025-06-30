package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.TagIdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

public class EnchantmentsType extends ComplexType
{
	public static final EnchantmentsType INSTANCE = new EnchantmentsType();

	private EnchantmentsType()
	{
		super(PrimitiveType.COMPOUND);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: todo
	}

	@Override public void getCompoundSuggestions(SuggestionListContext ctx)
	{
		List<ResourceKey> enchantments = Fields.getStaticFields(Enchantments.class, ResourceKey.class);
		for (ResourceKey<?> resourceKey : enchantments)
		{
			ResourceLocation id = resourceKey.location();
			NbtSuggestion tempSuggestion = new NbtSuggestion(id.toString(), PrimitiveType.INT);
			ctx.list().add(new TagIdSuggestion(tempSuggestion, id, ctx.parserType(), true));
		}
	}
}
