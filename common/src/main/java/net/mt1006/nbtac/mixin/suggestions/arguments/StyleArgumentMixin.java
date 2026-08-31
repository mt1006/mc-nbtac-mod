package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Style;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(StyleArgument.class)
public abstract class StyleArgumentMixin implements ArgumentType<Style>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		return SuggestionManager.get(builder.getRemaining(), "text/nbtac:style", builder, false);
	}
}
