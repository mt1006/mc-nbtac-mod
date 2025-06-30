package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;

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
