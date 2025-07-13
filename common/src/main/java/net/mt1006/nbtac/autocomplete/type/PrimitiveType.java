package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public enum PrimitiveType implements Type
{
	UNKNOWN,
	BOOLEAN,
	BYTE("b"),
	SHORT("s"),
	INT,
	LONG("l"),
	FLOAT("f"),
	DOUBLE,
	STRING,
	BYTE_ARRAY,
	INT_ARRAY,
	LONG_ARRAY,
	COMPOUND,
	LIST;

	private final String lowerCaseName;
	public final String symbol;
	public final String suffix;

	PrimitiveType()
	{
		this("");
	}

	PrimitiveType(String suffix)
	{
		this.suffix = suffix;
		this.lowerCaseName = name().toLowerCase();
		this.symbol = "[" + lowerCaseName + "]";
	}

	public String getName()
	{
		return lowerCaseName;
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		SuggestionList list = new SuggestionList(ctx.parsed().pos);
		String val = ctx.getRemaining();

		if (this == BOOLEAN)
		{
			if (ModConfig.shortBoolean.val)
			{
				list.addRaw("1b", symbol);
				list.addRaw("0b", symbol);
			}
			else
			{
				list.addRaw("true", symbol);
				list.addRaw("false", symbol);
			}
			return list.matchOrFiler(val);
		}
		else if (this == STRING && val.isEmpty())
		{
			list.addRaw(ModConfig.defaultQuotationMark.val.getStr(false), symbol);
		}
		else
		{
			list.addRaw("", symbol);
		}

		return (this == STRING && (ctx.parsed() instanceof ParsedPrimitive primitive) && primitive.closed) ? null : list;
	}

	@Override public String getSubtext()
	{
		return symbol;
	}

	@Override public PrimitiveType getPrimitive()
	{
		return this;
	}

	public boolean isListOrArray()
	{
		return this == LIST || this == BYTE_ARRAY || this == INT_ARRAY || this == LONG_ARRAY;
	}
}
