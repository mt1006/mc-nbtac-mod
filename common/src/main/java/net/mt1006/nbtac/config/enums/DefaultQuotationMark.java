package net.mt1006.nbtac.config.enums;

public enum DefaultQuotationMark
{
	SINGLE,
	DOUBLE;

	public char getChar(boolean isRawJson)
	{
		return (this == SINGLE && !isRawJson) ? '\'' : '"';
	}

	public String getStr(boolean isRawJson)
	{
		return (this == SINGLE && !isRawJson) ? "'" : "\"";
	}
}
