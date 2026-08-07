package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ParticleArgument.class)
public abstract class ParticleArgumentMixin implements ArgumentType<ParticleOptions>
{
	@Inject(method = "listSuggestions", at = @At(value = "HEAD"), cancellable = true)
	public void atListSuggestions(CommandContext<?> ctx, SuggestionsBuilder builder,
								  CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		try
		{
			String str = builder.getRemaining();
			int optionsStart = str.indexOf('{');

			if (optionsStart == -1)
			{
				Identifier particleId = Identifier.tryParse(str);
				if (particleId == null || NbtTagManager.get("particle/" + particleId) == null) { return; }

				builder = builder.createOffset(builder.getStart() + str.length());
				builder.suggest("{");
				cir.setReturnValue(builder.buildFuture());
				cir.cancel();
				return;
			}

			Identifier particleId = Identifier.tryParse(str.substring(0, optionsStart));
			if (particleId == null) { return; }

			builder = builder.createOffset(builder.getStart() + optionsStart);
			cir.setReturnValue(SuggestionManager.loadFromName(builder.getRemaining(), "particle/" + particleId, builder, false));
			cir.cancel();
		}
		catch (Exception ignore) {}
	}
}
