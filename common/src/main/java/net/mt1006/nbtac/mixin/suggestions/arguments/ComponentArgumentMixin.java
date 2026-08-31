package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.type.complex.TextComponentType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ComponentArgument.class)
public abstract class ComponentArgumentMixin implements ArgumentType<Component>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		return SuggestionManager.get(builder.getRemaining(), TextComponentType.INSTANCE, builder, false, Function.identity());
	}
}
