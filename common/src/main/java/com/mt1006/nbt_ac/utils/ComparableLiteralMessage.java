package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.LiteralMessage;

public class ComparableLiteralMessage extends LiteralMessage
{
	public ComparableLiteralMessage(String string)
	{
		super(string);
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) { return true; }
		if (!(obj instanceof ComparableLiteralMessage)) { return false; }
		return getString().equals(((ComparableLiteralMessage)obj).getString());
	}

	@Override public int hashCode()
	{
		return getString().hashCode();
	}
}
