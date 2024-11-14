package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.DataComponentManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestionSubtype;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class ParseJson
{
	public static void parseAll()
	{
		parseTags();
		parseComponents();
		parsePredictions();
	}

	private static void parseTags()
	{
		for (ResourceLoader.TagStructure tag : ResourceLoader.tags)
		{
			try
			{
				parseObject(tag.id, tag.applyTo, tag.tags, true);
			}
			catch (Exception exception) { NBTac.LOGGER.warn("Failed to parse tag: {}", tag.id); }
		}
	}

	private static void parseComponents()
	{
		for (ResourceLoader.ComponentStructure component : ResourceLoader.components)
		{
			try
			{
				parseComponent(component.getId(), component.type, component.subtype,
						component.with, component.alwaysRelevant, component.getTags());
			}
			catch (Exception exception) { NBTac.LOGGER.warn("Failed to parse component: {}", component.getId()); }
		}
	}

	private static void parsePredictions()
	{
		for (Pair<JsonArray, JsonArray> prediction : ResourceLoader.predictions)
		{
			try
			{
				new Prediction(prediction).execute();
			}
			catch (Exception exception) { NBTac.LOGGER.warn("Failed to parse prediction!"); }
		}
	}

	private static void parseComponent(String id, String typeString, @Nullable String subtypeString,
									   @Nullable String with, boolean alwaysRelevant, @Nullable JsonArray tags)
	{
		Pair<NbtSuggestion.Type, NbtSuggestion.Type> type = parseType(typeString);
		Pair<NbtSuggestionSubtype, String> subtype = parseSubtype(subtypeString);
		NbtSuggestions suggestions = tags != null ? parseObject(null, null, tags, false) : null;

		NbtSuggestion component = new NbtSuggestion(id, type.getLeft(), NbtSuggestion.Source.DEFAULT, type.getRight());
		component.subtype = subtype.getLeft();
		component.subtypeData = subtype.getRight();
		component.subcompound = suggestions;
		component.subtypeWith = with;
		if (alwaysRelevant) { component.setAlwaysRelevant(); }

		DataComponentManager.componentMap.put(id, component);
	}

	private static NbtSuggestions parseObject(@Nullable String id, @Nullable JsonArray applyTo,
											  JsonArray tags, boolean allowPredictions)
	{
		NbtSuggestions nbtSuggestions = new NbtSuggestions(allowPredictions);

		for (JsonElement tag : tags)
		{
			if (tag instanceof JsonObject) { parseTag((JsonObject)tag, nbtSuggestions, allowPredictions); }
		}

		if (applyTo != null)
		{
			for (JsonElement tagElement : applyTo)
			{
				if (tagElement instanceof JsonPrimitive && ((JsonPrimitive)tagElement).isString())
				{
					NbtSuggestionManager.add(tagElement.getAsString(), nbtSuggestions);
				}
			}
		}

		if (id != null) { NbtSuggestionManager.add(id, nbtSuggestions); }
		return nbtSuggestions;
	}

	private static void parseTag(JsonObject suggestion, NbtSuggestions nbtSuggestions, boolean allowPredictions)
	{
		String tag = suggestion.get("tag").getAsString();
		Pair<NbtSuggestion.Type, NbtSuggestion.Type> type = parseType(suggestion.get("type").getAsString());
		Pair<NbtSuggestionSubtype, String> subtype = parseSubtype(suggestion.get("subtype"));

		String withString = null;
		JsonElement withElement = suggestion.get("with");
		if (withElement instanceof JsonPrimitive && ((JsonPrimitive)withElement).isString())
		{
			withString = withElement.getAsString();
		}

		boolean recommended = false;
		JsonElement recommendedElement  = suggestion.get("recommended");
		if (recommendedElement instanceof JsonPrimitive && ((JsonPrimitive)recommendedElement).isBoolean())
		{
			recommended = recommendedElement.getAsBoolean();
		}

		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type.getLeft());
		newSuggestion.listType = type.getRight();
		newSuggestion.subtype = subtype.getLeft();
		newSuggestion.subtypeData = subtype.getRight();
		newSuggestion.subtypeWith = withString;
		newSuggestion.recommended = recommended;

		JsonElement subcompoundElement = suggestion.get("tags");
		if (subcompoundElement instanceof JsonArray)
		{
			newSuggestion.subcompound = parseObject(null, null, (JsonArray)subcompoundElement, allowPredictions);
		}

		nbtSuggestions.add(newSuggestion);
	}

	public static Pair<NbtSuggestion.Type, NbtSuggestion.Type> parseType(String typeString)
	{
		int typeSlashPos = typeString.indexOf('/');
		if (typeSlashPos == -1)
		{
			return Pair.of(NbtSuggestion.Type.fromName(typeString), NbtSuggestion.Type.UNKNOWN);
		}
		else
		{
			return Pair.of(NbtSuggestion.Type.fromName(typeString.substring(0, typeSlashPos)),
					NbtSuggestion.Type.fromName(typeString.substring(typeSlashPos + 1)));
		}
	}

	private static Pair<NbtSuggestionSubtype, String> parseSubtype(@Nullable JsonElement subtypeElement)
	{
		String subtypeString = (subtypeElement instanceof JsonPrimitive && ((JsonPrimitive)subtypeElement).isString())
				? subtypeElement.getAsString()
				: null;
		return parseSubtype(subtypeString);
	}

	private static Pair<NbtSuggestionSubtype, String> parseSubtype(@Nullable String subtypeString)
	{
		if (subtypeString == null) { return Pair.of(NbtSuggestionSubtype.NONE, null); }
		int subtypeSlashPos = subtypeString.indexOf('/');

		if (subtypeSlashPos == -1)
		{
			return Pair.of(NbtSuggestionSubtype.fromName(subtypeString), null);
		}
		else
		{
			return Pair.of(NbtSuggestionSubtype.fromName(subtypeString.substring(0, subtypeSlashPos)),
					subtypeString.substring(subtypeSlashPos + 1));
		}
	}
}
