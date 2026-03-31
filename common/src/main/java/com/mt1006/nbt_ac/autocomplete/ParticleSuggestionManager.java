package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.suggestions.RawSuggestion;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class ParticleSuggestionManager
{
	private static final String PARTICLE_ROOT = "particle/";

	public static CompletableFuture<Suggestions> load(SuggestionsBuilder suggestionsBuilder)
	{
		String remaining = suggestionsBuilder.getRemaining();
		int optionsStart = remaining.indexOf('{');

		if (optionsStart == -1)
		{
			return suggestParticleIds(suggestionsBuilder);
		}

		Identifier particleId = Identifier.tryParse(remaining.substring(0, optionsStart));
		if (particleId == null)
		{
			return suggestParticleIds(suggestionsBuilder);
		}

		SuggestionsBuilder optionBuilder = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + optionsStart);
		return NbtSuggestionManager.loadFromName(PARTICLE_ROOT + particleId, optionBuilder.getRemaining(), optionBuilder, false);
	}

	private static CompletableFuture<Suggestions> suggestParticleIds(SuggestionsBuilder suggestionsBuilder)
	{
		SuggestionList suggestionList = new SuggestionList();
		RegistryUtils.PARTICLE_TYPE.keySet().forEach((id) ->
				suggestionList.add(new RawSuggestion(id.toString(), "[#particle]")));
		suggestionList.forEach((suggestion) -> suggestion.suggest(suggestionsBuilder));
		return suggestionsBuilder.buildFuture();
	}
}
