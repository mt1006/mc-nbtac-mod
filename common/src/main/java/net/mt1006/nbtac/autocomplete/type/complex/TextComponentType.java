package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedList;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.autocomplete.parser.ParsedValue;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.compound.TextCompoundType;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class TextComponentType extends ComplexType
{
	public static final TextComponentType INSTANCE = new TextComponentType();

	private TextComponentType()
	{
		super(PrimitiveType.COMPOUND);
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		if (ctx.getRemaining().isEmpty())
		{
			// it will call getBasicSuggestions()
			return super.getSuggestions(ctx);
		}

		ParsedValue parsed = ctx.parsed();
		if (parsed instanceof ParsedCompound) { return TextCompoundType.INSTANCE.getSuggestions(ctx); }
		else if (parsed instanceof ParsedList) { return TextCompoundType.LIST_INSTANCE.getSuggestions(ctx); }
		else if (parsed instanceof ParsedPrimitive) { return PrimitiveType.STRING.getSuggestions(ctx); }
		else { return SuggestionList.empty(); }
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		list.addRaw(ModConfig.defaultQuotationMark.val.getStr(false), "(simple string) [#text_component]", 3);
		list.addRaw("{", "(text component) [#text_component]", 2);
		list.addRaw("[", "(test component list) [#text_component]", 1);
	}
}
