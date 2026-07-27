package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public class ParsedTag
{
	public final ParsedCompound parentCompound;
	public @Nullable String key;
	public @Nullable ParsedValue val;

	public ParsedTag(ParsedCompound parent)
	{
		this.parentCompound = parent;
	}

	public static ParsedTag createVirtualItemParents(@Nullable String itemId)
	{
		// creates virtual structure of parents for item data components
		// it's necessary for providing item id for types with dynamic arguments
		// it's similar to the structure of compound/nbtac:item_stack component tag,
		// that is: {id:"minecraft:some_item", components:{...}}

		ParsedCompound rootCompound = new ParsedCompound(null, 0);

		ParsedTag idTag = createVirtualId(rootCompound, itemId);
		rootCompound.add(idTag, 0);

		ParsedTag componentsTag = new ParsedTag(rootCompound);
		componentsTag.key = "components";
		componentsTag.val = new ParsedCompound(componentsTag, 0);
		rootCompound.add(componentsTag, 0);

		return new ParsedTag((ParsedCompound)componentsTag.val);
	}

	public static ParsedTag createVirtualId(ParsedCompound rootCompound, @Nullable String id)
	{
		ParsedTag idTag = new ParsedTag(rootCompound);
		ParsedPrimitive idVal = new ParsedPrimitive(idTag, 0);
		idVal.val = id;
		idVal.closed = true;
		idTag.key = "id";
		idTag.val = idVal;
		return idTag;
	}
}
