package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

public class InventorySlotType extends ComplexType
{
	public InventorySlotType(@Nullable Type type)
	{
		super(type != null ? type.getPrimitive() : PrimitiveType.UNKNOWN);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		for (int i = 0; i < 9; i++)
		{
			String subtext = String.format("(Hotbar %d) [#inventory_slot]", i + 1);
			ctx.list().addRaw(i + primitive.suffix, subtext);
		}

		for (int i = 9; i < 35; i++)
		{
			int row = ((i - 9) / 9) + 1;
			int column = ((i - 9) % 9) + 1;
			String subtext = String.format("(Storage %d:%d) [#inventory_slot]", row, column);
			ctx.list().addRaw(i + primitive.suffix, subtext);
		}

		ctx.list().addRaw("100" + primitive.suffix, "(Feet) [#inventory_slot]");
		ctx.list().addRaw("101" + primitive.suffix, "(Legs) [#inventory_slot]");
		ctx.list().addRaw("102" + primitive.suffix, "(Chest) [#inventory_slot]");
		ctx.list().addRaw("103" + primitive.suffix, "(Head) [#inventory_slot]");
		ctx.list().addRaw("-106" + primitive.suffix, "(Off-hand) [#inventory_slot]");
	}
}
