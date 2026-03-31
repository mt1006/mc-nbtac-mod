package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.ParticleSuggestionManager;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(ParticleArgument.class)
public abstract class ParticleArgumentMixin implements ArgumentType<ParticleOptions>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			return ParticleSuggestionManager.load(suggestionsBuilder);
		}
		catch (Exception e)
		{
			return Suggestions.empty();
		}
	}
}
