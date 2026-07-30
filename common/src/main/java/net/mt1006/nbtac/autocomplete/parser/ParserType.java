package net.mt1006.nbtac.autocomplete.parser;

public enum ParserType
{
	VALUE(false, false),
	PATH(false, true),
	JSON(true, false);

	public final boolean requiresDoubleQuotes;
	public final boolean requiresNamespace;

	ParserType(boolean requiresDoubleQuotes, boolean requiresNamespace)
	{
		this.requiresDoubleQuotes = requiresDoubleQuotes;
		this.requiresNamespace = requiresNamespace;
	}
}
