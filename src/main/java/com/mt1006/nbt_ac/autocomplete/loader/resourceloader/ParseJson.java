package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;

import java.util.Map;

public class ParseJson
{
	public static void parseAll()
	{
		for (Map.Entry<String, JsonElement> jsonEntry : ResourceLoader.listOfEntries)
		{
			try
			{
				if (!(jsonEntry.getValue() instanceof JsonObject)) { continue; }
				parseObject(jsonEntry.getKey(), (JsonObject)jsonEntry.getValue());
			}
			catch (Exception ignore) {}
		}
	}

	private static NbtSuggestions parseObject(String name, JsonObject jsonObject)
	{
		NbtSuggestions nbtSuggestions = new NbtSuggestions();

		JsonElement suggestions = jsonObject.get("suggestions");
		if (!(suggestions instanceof JsonArray)) { return null; }

		for (JsonElement jsonElement : suggestions.getAsJsonArray())
		{
			if (jsonElement instanceof JsonObject) { parseSuggestion((JsonObject)jsonElement, nbtSuggestions); }
		}

		JsonElement keys = jsonObject.get("keys");
		if (keys instanceof JsonArray)
		{
			for (JsonElement keyElement : (JsonArray)keys)
			{
				if (keyElement instanceof JsonPrimitive && ((JsonPrimitive)keyElement).isString())
				{
					NbtSuggestionManager.add(keyElement.getAsString(), nbtSuggestions);
				}
			}
		}

		if (name != null) { NbtSuggestionManager.add(name, nbtSuggestions); }
		return nbtSuggestions;
	}

	private static void parseSuggestion(JsonObject suggestion, NbtSuggestions nbtSuggestions)
	{
		JsonElement tagElement = suggestion.get("tag");
		if (!(tagElement instanceof JsonPrimitive) || !((JsonPrimitive)tagElement).isString()) { return; }
		String tag = tagElement.getAsString();

		JsonElement typeElement = suggestion.get("type");
		if (!(typeElement instanceof JsonPrimitive) || !((JsonPrimitive)typeElement).isString()) { return; }
		String typeString = typeElement.getAsString();

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

		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type);
		newSuggestion.listType = listType;
		newSuggestion.subtype = subtype;
		newSuggestion.subtypeData = subtypeData;

		JsonElement subcompoundElement = suggestion.get("subcompound");
		if (subcompoundElement instanceof JsonObject)
		{
			newSuggestion.subcompound = parseObject(null, (JsonObject)subcompoundElement);
		}

		nbtSuggestions.addSuggestion(newSuggestion);
	}
}
