package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.type.complex.TextComponentType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(ComponentArgument.class)
public abstract class ComponentArgumentMixin implements ArgumentType<Component>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder builder)
	{
		try
		{
			CustomTagParser parser = CustomTagParser.forValueOfType(builder.getRemaining(), TextComponentType.INSTANCE);
			return NbtTagManager.finishSuggestions(parser.parse(), builder);
		}
		catch (Exception e)
		{
			return Suggestions.empty();
		}
	}
}
