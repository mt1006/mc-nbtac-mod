package net.mt1006.nbtac.autocomplete.type.string;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.autocomplete.suggestions.RawSuggestion;
import net.mt1006.nbtac.config.ModConfig;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class EntitySelectorType extends ComplexStringType
{
	public static final EntitySelectorType INSTANCE = new EntitySelectorType();

	@Override public int getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		String str = null;
		if (ctx.parsed() instanceof ParsedPrimitive parsed)
		{
			str = parsed.val;
			if (parsed.closed) { return -1; } // getSuggestions should return null
		}

		StringReader reader = new StringReader(str != null ? str : "");
		EntitySelectorParser parser = new EntitySelectorParser(reader, true);

		char quote = ModConfig.defaultQuotationMark.val.getChar(false);
		boolean parserSuccess = true;
		try
		{
			parser.parse();
		}
		catch (CommandSyntaxException e) { parserSuccess = false; }

		List<Suggestion> suggestions;
		try
		{
			suggestions = parser.fillSuggestions(new SuggestionsBuilder(reader.getString(), 0), (arg) -> {}).get().getList();
		}
		catch (InterruptedException | ExecutionException e) { return 0; }

		if (parserSuccess) { list.add(new RawSuggestion(String.valueOf(quote), "[#entity_selector]")); }

		if (suggestions.isEmpty()) { return reader.getString().length() + 1; }
		if (reader.getCursor() == 0)
		{
			suggestions.forEach((s) -> list.add(new RawSuggestion(quote + s.getText(), "[#entity_selector]")));
			return suggestions.getFirst().getRange().getStart();
		}
		else
		{
			suggestions.forEach((s) -> list.add(new RawSuggestion(escapeStr(s.getText(), quote), "[#entity_selector]")));
			return suggestions.getFirst().getRange().getStart() + 1;
		}
	}
}
