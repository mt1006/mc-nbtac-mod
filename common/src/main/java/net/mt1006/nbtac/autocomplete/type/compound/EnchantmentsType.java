package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.Fields;

import java.util.List;

public class EnchantmentsType extends ComplexCompoundType
{
	public static final EnchantmentsType INSTANCE = new EnchantmentsType();

	@Override public void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map)
	{
		List<ResourceKey> enchantments = Fields.getStaticFields(Enchantments.class, ResourceKey.class);
		for (ResourceKey<?> resourceKey : enchantments)
		{
			ResourceLocation id = resourceKey.location();
			map.add(new GeneratedNbtTag(id.toString(), PrimitiveType.INT, 0, id));
		}
	}
}
