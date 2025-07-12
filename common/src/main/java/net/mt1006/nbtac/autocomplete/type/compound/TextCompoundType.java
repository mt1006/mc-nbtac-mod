package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.autocomplete.parser.ParsedTag;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.autocomplete.type.ListType;

public class TextCompoundType extends ComplexCompoundType
{
	public static final TextCompoundType INSTANCE = new TextCompoundType();
	public static final ListType LIST_INSTANCE = new ListType(INSTANCE);

	@Override protected void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsed, NbtTagMap map)
	{
		map.addAll(NbtTagManager.get("text/nbtac:common"));
		map.addAll(NbtTagManager.get("text/nbtac:style"));
		NbtTagMap initialContent = NbtTagManager.get("text/nbtac:initial_content");
		if (initialContent == null) { return; }

		String type = null;
		ParsedTag typeTag = parsed.get("type");
		if (typeTag != null && typeTag.val instanceof ParsedPrimitive typeVal && typeVal.val != null)
		{
			type = typeVal.val.equals("translatable") ? "translate" : typeVal.val;
		}
		else
		{
			for (NbtTag tag : initialContent.getAll())
			{
				if (parsed.containsKey(tag.getName()) && !tag.getName().equals("type"))
				{
					type = tag.getName();
					break;
				}
			}
		}

		if (type == null)
		{
			initialContent.getAll().forEach((t) -> map.add(new GeneratedNbtTag(t, 100)));
		}
		else
		{
			String finalType = type;
			initialContent.getAll().forEach((t) -> map.add(new GeneratedNbtTag(t, t.getName().equals(finalType) ? 100 : -1)));
		}
	}
}
