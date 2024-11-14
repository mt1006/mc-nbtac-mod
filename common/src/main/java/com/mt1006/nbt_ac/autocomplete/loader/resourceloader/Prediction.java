package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestionSubtype;
import com.mt1006.nbt_ac.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Prediction
{
	private final List<Condition> conditions = new ArrayList<>();
	private final List<Operation> operations = new ArrayList<>();

	public Prediction(Pair<JsonArray, JsonArray> prediction)
	{
		prediction.getLeft().forEach((element) -> conditions.add(new Condition(element.getAsJsonObject())));
		prediction.getRight().forEach((element) -> operations.add(new Operation(element.getAsJsonObject())));
	}

	public void execute()
	{
		for (Condition condition : conditions)
		{
			Collection<NbtSuggestion> matching = condition.matchingSuggestions();
			if (ModConfig.debugMode.val && matching.isEmpty()) { NBTac.LOGGER.warn("No matching suggestions - {}", condition); }

			for (NbtSuggestion suggestion : matching)
			{
				operations.forEach((operation) -> operation.execute(suggestion));
			}
		}
	}

	private static class Condition
	{
		private final @Nullable String root;
		private final @Nullable String path;

		public Condition(JsonObject object)
		{
			JsonElement rootElement = object.get("root");
			root = rootElement != null ? rootElement.getAsString() : null;

			JsonElement pathElement = object.get("path");
			path = pathElement != null ? pathElement.getAsString() : null;
		}

		public Collection<NbtSuggestion> matchingSuggestions()
		{
			if (path != null)
			{
				if (path.startsWith("*"))
				{
					if (root != null) { return List.of(); }
					String key = path.substring(1);
					return NbtSuggestions.suffixFullMap.get(key);
				}
				else if (path.endsWith("*"))
				{
					if (root != null) { return List.of(); }
					String key = path.substring(0, path.length() - 1);
					return NbtSuggestions.prefixFullMap.get(key);
				}
				else
				{
					if (root == null) { return NbtSuggestions.fullMap.get(path); }
					NbtSuggestions suggestionsFromRoot = NbtSuggestionManager.get(root);
					NbtSuggestion suggestionFromRoot = suggestionsFromRoot != null ? suggestionsFromRoot.get(path) : null;
					return suggestionFromRoot != null ? List.of(suggestionFromRoot) : List.of();
				}
			}
			else if (root != null)
			{
				// If path == null then operations on a root as a suggestion (like changing type) are ignored
				return List.of(NbtSuggestion.getDummyCompound(NbtSuggestionManager.get(root)));
			}
			return List.of();
		}

		@Override public String toString()
		{
			return String.format("root:%s path:%s", root, path);
		}
	}

	private static class Operation
	{
		public final @Nullable String on;
		public final @Nullable String with;
		public final String type;
		public final String val;

		public Operation(JsonObject object)
		{
			JsonElement onElement = object.get("on");
			on = onElement != null ? onElement.getAsString() : null;

			JsonElement withElement = object.get("with");
			with = withElement != null ? withElement.getAsString() : null;

			JsonElement typeElement = object.get("type");
			type = typeElement != null ? typeElement.getAsString() : "";

			JsonElement valElement = object.get("val");
			val = valElement != null ? valElement.getAsString() : "";
		}

		public void execute(NbtSuggestion suggestion)
		{
			if (on == null) { executeOn(suggestion, null); }
			else if (suggestion.subcompound != null) { executeOn(suggestion.subcompound.get(on), suggestion.subcompound); }
		}

		private void executeOn(@Nullable NbtSuggestion suggestion, @Nullable NbtSuggestions root)
		{
			if (suggestion == null) { return; }

			switch (type)
			{
				case "override_unknown": // val = "float", val = "string@enum/aaa;bbb"
					if (suggestion.type != NbtSuggestion.Type.UNKNOWN) { return; }
					suggestion.setType(ParseJson.parseType(val));
					suggestion.changeSuggestionSource(NbtSuggestion.Source.TYPE_PREDICTION);
					return;

				case "set_type":         // val = "float", val = "string@enum/aaa;bbb"
					int atPos = val.indexOf('@');
					suggestion.setType(ParseJson.parseType(atPos != -1 ? val.substring(0, atPos) : val));
					suggestion.changeSuggestionSource(NbtSuggestion.Source.TYPE_PREDICTION);
					if (atPos == -1) { return; }

				case "set_subtype":      // val = "registry_key/minecraft:item"
					int slashPos = val.indexOf('/');
					suggestion.subtype = NbtSuggestionSubtype.fromName(slashPos != -1 ? val.substring(0, slashPos) : val);
					if (slashPos != -1) { suggestion.subtypeData = val.substring(slashPos + 1); }
					suggestion.subtypeWith = with;
					suggestion.changeSuggestionSource(NbtSuggestion.Source.SUBTYPE_PREDICTION);
					return;

				case "set_subcompound":  // val = "compound/nbt_ac:attributes"
					if (suggestion.type == NbtSuggestion.Type.UNKNOWN) { suggestion.type = NbtSuggestion.Type.COMPOUND; }
					else if (suggestion.type == NbtSuggestion.Type.LIST && suggestion.listType == NbtSuggestion.Type.UNKNOWN)
					{
						suggestion.listType = NbtSuggestion.Type.COMPOUND;
					}
					suggestion.subcompound = NbtSuggestionManager.get(val);
					suggestion.changeSuggestionSource(NbtSuggestion.Source.COMPOUND_PREDICTION);
					return;
			}

			if (!suggestion.hasSubcompound()) { return; }

			switch (type)
			{
				case "recursion":
					if (root == null) { return; }
					suggestion.subcompound = root;
					suggestion.changeSuggestionSource(NbtSuggestion.Source.COMPOUND_PREDICTION);
					return;

				case "add_tag":          // val = "id/string/registry_key/minecraft:entity_type"
					int firstSlash = val.indexOf('/');
					int secondSlash = firstSlash != -1 ? val.indexOf('/', firstSlash + 1) : -1;
					int thirdSlash = secondSlash != -1 ? val.indexOf('/', secondSlash + 1) : -1;

					if (firstSlash == -1) { return; }

					String tagStr = val.substring(0, firstSlash);
					String typeStr = secondSlash != -1 ? val.substring(firstSlash + 1, secondSlash) : val.substring(firstSlash + 1);

					NbtSuggestion.Type type = NbtSuggestion.Type.fromName(typeStr);
					NbtSuggestion newSuggestion = new NbtSuggestion(tagStr, type, NbtSuggestion.Source.PREDICTION);

					if (secondSlash != -1)
					{
						String subtypeStr = thirdSlash != -1 ? val.substring(secondSlash + 1, thirdSlash) : val.substring(secondSlash + 1);
						String subtypeDataStr = thirdSlash != -1 ? val.substring(thirdSlash + 1) : null;

						newSuggestion.subtype = NbtSuggestionSubtype.fromName(subtypeStr);
						newSuggestion.subtypeData = subtypeDataStr;
					}

					suggestion.getSubcompound().add(newSuggestion);
					return;

				case "add_compound":     // val = "id/component/nbt_ac:inventory"
					int slash = val.indexOf('/');
					NbtSuggestion compoundSuggestion = new NbtSuggestion(val.substring(0, slash),
							NbtSuggestion.Type.COMPOUND, NbtSuggestion.Source.PREDICTION);
					compoundSuggestion.subcompound = NbtSuggestionManager.get(val.substring(slash + 1));
					suggestion.getSubcompound().add(compoundSuggestion);
					return;

				case "copy_tags":        // val = "compound/nbt_ac:inventory"
					NbtSuggestions copyTagsFrom = NbtSuggestionManager.get(val);
					if (copyTagsFrom != null) { suggestion.getSubcompound().copyAll(copyTagsFrom, true); }
					return;
			}

			NBTac.LOGGER.warn("Unknown prediction type: {}", type);
		}
	}
}
