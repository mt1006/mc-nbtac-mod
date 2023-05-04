package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class ParseJson
{
	public static void parseAll()
	{
		parseCommon();
		parseTags();
		parsePredictions();
	}

	private static void parseCommon()
	{
		for (Pair<String, JsonObject> common : ResourceLoader.common)
		{
			try
			{
				parseObject(common.getLeft(), common.getRight());
			}
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to parse common: " + common.getLeft());
			}
		}
	}

	private static void parseTags()
	{
		for (Pair<String, JsonObject> tag : ResourceLoader.tags)
		{
			try
			{
				parseObject(tag.getLeft(), tag.getRight());
			}
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to parse tag: " + tag.getLeft());
			}
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
			catch (Exception exception)
			{
				NBTac.LOGGER.warn("Failed to parse prediction!");
			}
		}
	}

	private static NbtSuggestions parseObject(@Nullable String name, JsonObject jsonObject)
	{
		NbtSuggestions nbtSuggestions = new NbtSuggestions();

		JsonElement suggestions = jsonObject.get("suggestions");
		if (!(suggestions instanceof JsonArray)) { return null; }

		for (JsonElement jsonElement : suggestions.getAsJsonArray())
		{
			if (jsonElement instanceof JsonObject) { parseSuggestion((JsonObject)jsonElement, nbtSuggestions); }
		}

		JsonElement tags = jsonObject.get("tags");
		if (tags instanceof JsonArray)
		{
			for (JsonElement tagElement : (JsonArray)tags)
			{
				if (tagElement instanceof JsonPrimitive && ((JsonPrimitive)tagElement).isString())
				{
					NbtSuggestionManager.add(tagElement.getAsString(), nbtSuggestions);
				}
			}
		}

		if (name != null)
		{
			if (name.startsWith("tag/")) { NbtSuggestionManager.add(name.substring(4), nbtSuggestions); }
			else if (name.startsWith("common/")) { NbtSuggestionManager.add(name, nbtSuggestions); }
		}
		return nbtSuggestions;
	}

	private static void parseSuggestion(JsonObject suggestion, NbtSuggestions nbtSuggestions)
	{
		String tag = suggestion.get("tag").getAsString();
		String typeString = suggestion.get("type").getAsString();

		NbtSuggestion.Type type, listType;

		int typeSlashPos = typeString.indexOf('/');
		if (typeSlashPos == -1)
		{
			type = NbtSuggestion.Type.fromName(typeString);
			listType = NbtSuggestion.Type.UNKNOWN;
		}
		else
		{
			type = NbtSuggestion.Type.fromName(typeString.substring(0, typeSlashPos));
			listType = NbtSuggestion.Type.fromName(typeString.substring(typeSlashPos + 1));
		}

		NbtSuggestion.Subtype subtype = NbtSuggestion.Subtype.NONE;
		String subtypeData = null;

		JsonElement subtypeElement = suggestion.get("subtype");
		if (subtypeElement instanceof JsonPrimitive && ((JsonPrimitive)subtypeElement).isString())
		{
			String subtypeString = subtypeElement.getAsString();
			int subtypeSlashPos = subtypeString.indexOf('/');

			if (subtypeSlashPos == -1)
			{
				subtype = NbtSuggestion.Subtype.fromName(subtypeString);
			}
			else
			{
				subtype = NbtSuggestion.Subtype.fromName(subtypeString.substring(0, subtypeSlashPos));
				subtypeData = subtypeString.substring(subtypeSlashPos + 1);
			}
		}

		String withString = null;
		JsonElement withElement = suggestion.get("with");
		if (withElement instanceof JsonPrimitive && ((JsonPrimitive)withElement).isString())
		{
			withString = withElement.getAsString();
		}

		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type);
		newSuggestion.listType = listType;
		newSuggestion.subtype = subtype;
		newSuggestion.subtypeData = subtypeData;
		newSuggestion.subtypeWith = withString;

		JsonElement subcompoundElement = suggestion.get("subcompound");
		if (subcompoundElement instanceof JsonObject)
		{
			newSuggestion.subcompound = parseObject(null, (JsonObject)subcompoundElement);
		}

		nbtSuggestions.add(newSuggestion);
	}
}
