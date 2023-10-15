package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
			for (NbtSuggestion suggestion : condition.matchingSuggestions())
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
			path = pathElement != null ? pathElement.getAsJsonArray().get(0).getAsString() : null;
		}

		public Collection<NbtSuggestion> matchingSuggestions()
		{
			if (path != null)
			{
				if (path.startsWith("*"))
				{
					String key = path.substring(1);
					return (root != null ? NbtSuggestionManager.get(root).suffixMap : NbtSuggestions.suffixFullMap).get(key);
				}
				else if (path.endsWith("*"))
				{
					String key = path.substring(0, path.length() - 1);
					return (root != null ? NbtSuggestionManager.get(root).prefixMap : NbtSuggestions.prefixFullMap).get(key);
				}
				else
				{
					return (root != null ? Collections.singletonList(NbtSuggestionManager.get(root).get(path)) : NbtSuggestions.fullMap.get(path));
				}
			}
			else if (root != null)
			{
				// If path == null then operations on a root as a suggestion (like changing type) are ignored
				NbtSuggestion dummySuggestion = new NbtSuggestion("dummy-" + root, NbtSuggestion.Type.COMPOUND);
				dummySuggestion.subcompound = NbtSuggestionManager.get(root);
				return Collections.singletonList(dummySuggestion);
			}
			return new ArrayList<>();
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
				case "set_type":         // val = "float"
					suggestion.type = NbtSuggestion.Type.fromName(val);
					suggestion.changeSuggestionType(NbtSuggestion.SuggestionType.TYPE_PREDICTION);
					return;

				case "set_subtype":      // val = "registry_key/minecraft:item"
					int slashPos = val.indexOf('/');
					suggestion.subtype = NbtSuggestion.Subtype.fromName(slashPos != -1 ? val.substring(0, slashPos) : val);
					if (slashPos != -1) { suggestion.subtypeData = val.substring(slashPos + 1); }
					suggestion.subtypeWith = with;
					suggestion.subtypeWithParentTag = (on != null);
					suggestion.changeSuggestionType(NbtSuggestion.SuggestionType.SUBTYPE_PREDICTION);
					return;

				case "set_subcompound":  // val = "component/nbt_ac:attributes"
					suggestion.subcompound = NbtSuggestionManager.get(val);
					suggestion.changeSuggestionType(NbtSuggestion.SuggestionType.COMPOUND_PREDICTION);
					return;
			}

			if (suggestion.subcompound == null)
			{
				if (suggestion.type == NbtSuggestion.Type.COMPOUND || suggestion.listType == NbtSuggestion.Type.COMPOUND)
				{
					suggestion.subcompound = new NbtSuggestions();
				}
				else
				{
					return;
				}
			}
			NbtSuggestions compound = suggestion.subcompound;

			switch (type)
			{
				case "recursion":
					if (root == null) { return; }
					suggestion.subcompound = root;
					suggestion.changeSuggestionType(NbtSuggestion.SuggestionType.COMPOUND_PREDICTION);
					return;

				case "add_tag":          // val = "id/string/registry_key/minecraft:entity_type"
					int firstSlash = val.indexOf('/');
					int secondSlash = firstSlash != -1 ? val.indexOf('/', firstSlash + 1) : -1;
					int thirdSlash = secondSlash != -1 ? val.indexOf('/', secondSlash + 1) : -1;

					if (firstSlash == -1) { return; }

					String tagStr = val.substring(0, firstSlash);
					String typeStr = secondSlash != -1 ? val.substring(firstSlash + 1, secondSlash) : val.substring(firstSlash + 1);

					NbtSuggestion.Type type = NbtSuggestion.Type.fromName(typeStr);
					NbtSuggestion newSuggestion = new NbtSuggestion(tagStr, type, NbtSuggestion.SuggestionType.PREDICTION);

					if (secondSlash != -1)
					{
						String subtypeStr = thirdSlash != -1 ? val.substring(secondSlash + 1, thirdSlash) : val.substring(secondSlash + 1);
						String subtypeDataStr = thirdSlash != -1 ? val.substring(thirdSlash + 1) : null;

						newSuggestion.subtype = NbtSuggestion.Subtype.fromName(subtypeStr);
						newSuggestion.subtypeData = subtypeDataStr;
					}

					compound.add(newSuggestion);
					return;

				case "add_compound":          // val = "id/component/nbt_ac:inventory"
					int slash = val.indexOf('/');
					NbtSuggestion compoundSuggestion = new NbtSuggestion(val.substring(0, slash),
							NbtSuggestion.Type.COMPOUND, NbtSuggestion.SuggestionType.PREDICTION);
					compoundSuggestion.subcompound = NbtSuggestionManager.get(val.substring(slash + 1));
					compound.add(compoundSuggestion);
					return;

				case "copy_tags":        // val = "component/nbt_ac:inventory"
					compound.copyAll(NbtSuggestionManager.get(val), true);
					return;
			}

			NBTac.LOGGER.warn("Unknown prediction type: " + type);
		}
	}
}
