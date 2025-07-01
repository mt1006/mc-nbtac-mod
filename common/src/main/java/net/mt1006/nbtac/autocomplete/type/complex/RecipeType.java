package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class RecipeType extends ComplexType
{
	public static final RecipeType INSTANCE = new RecipeType();

	private RecipeType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		MinecraftServer recipeServer = Minecraft.getInstance().getSingleplayerServer();
		if (recipeServer == null) { return; }

		for (RecipeHolder<?> recipeHolder : recipeServer.getRecipeManager().getRecipes())
		{
			ctx.list().add(new IdSuggestion(recipeHolder.id().location(), null, ctx.parserType()));
		}
	}
}
