package com.mt1006.nbt_ac.autocomplete.type;

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
		this.suffix = "";
		this.lowerCaseName = name().toLowerCase();
		this.symbol = String.format("[%s]", lowerCaseName);
	}

	PrimitiveType(String suffix)
	{
		this.suffix = suffix;
		this.lowerCaseName = name().toLowerCase();
		this.symbol = String.format("[%s]", lowerCaseName);
	}

	public String getName()
	{
		return lowerCaseName;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: fix
		ctx.list().addRaw("a", "b");
	}

	@Override public String getSubtext()
	{
		return symbol;
	}

	@Override public PrimitiveType getPrimitive()
	{
		return this;
	}
}