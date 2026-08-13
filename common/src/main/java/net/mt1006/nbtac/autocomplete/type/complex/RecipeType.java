package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class RecipeType extends ComplexType
{
	public static final RecipeType INSTANCE = new RecipeType();

	private RecipeType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null) { return; }

		for (RecipeHolder<?> recipeHolder : server.getRecipeManager().getRecipes())
		{
			list.add(new IdSuggestion(recipeHolder.id(), null, ctx.parserType()));
		}
	}
}
