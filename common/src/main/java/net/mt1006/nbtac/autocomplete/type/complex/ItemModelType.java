package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.ModelManagerFields;

public class ItemModelType extends ComplexType
{
	public static final ItemModelType INSTANCE = new ItemModelType();

	private ItemModelType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		for (Identifier id : ((ModelManagerFields)modelManager).getBakedItemStackModels().keySet())
		{
			list.add(new IdSuggestion(id, "[#item_model]", ctx.parserType()));
		}
	}
}
