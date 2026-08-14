package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.*;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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

	public final String symbol;
	public final String suffix;

	PrimitiveType()
	{
		this("");
	}

	PrimitiveType(String suffix)
	{
		this.suffix = suffix;
		this.symbol = "[" + name().toLowerCase(Locale.ROOT) + "]";
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

	public Class<? extends ParsedValue> getClassOfParsed()
	{
		return switch (this)
		{
			case COMPOUND -> ParsedCompound.class;
			case LIST -> ParsedList.class;
			case BYTE_ARRAY, INT_ARRAY, LONG_ARRAY -> ParsedArray.class;
			default -> ParsedPrimitive.class;
		};
	}

	public boolean isListOrArray()
	{
		return this == LIST || this == BYTE_ARRAY || this == INT_ARRAY || this == LONG_ARRAY;
	}
}
