package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedList;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
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

		return switch (ctx.parsed())
		{
			case ParsedCompound ignore -> TextCompoundType.INSTANCE.getSuggestions(ctx);
			case ParsedList ignore -> TextCompoundType.LIST_INSTANCE.getSuggestions(ctx);
			case ParsedPrimitive ignore -> PrimitiveType.STRING.getSuggestions(ctx);
			default -> SuggestionList.empty();
		};
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		list.addRaw(ModConfig.getDefaultQuotationMarkStr(false), "(simple string) [#text_component]", 3);
		list.addRaw("{", "(text component) [#text_component]", 2);
		list.addRaw("[", "(test component list) [#text_component]", 1);
	}
}
