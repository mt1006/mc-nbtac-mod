package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

public class DisassembledValue
{
	Type type;
	Object object;

	public DisassembledValue(Type type, Object object)
	{
		this.type = type;
		this.object = object;
	}

	public enum Type
	{
		STRING,     // String
		INTEGER,    // Integer
		COMPOUND,   // NbtSuggestions
		LIST_TAG,   // NbtSuggestions
		THIS        // null
	}
}
