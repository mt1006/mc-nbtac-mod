package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.TagIdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.Fields;

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
