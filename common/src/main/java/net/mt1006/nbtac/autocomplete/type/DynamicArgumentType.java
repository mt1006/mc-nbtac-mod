package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.autocomplete.parser.ParsedTag;
import net.mt1006.nbtac.autocomplete.parser.ParsedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DynamicArgumentType implements Type
{
	private final TypeConstructor constructor;
	private final List<Type> subtypes;
	private final List<String> args;
	private final List<String> staticArgs;
	private final PrimitiveType primitive;

	public DynamicArgumentType(TypeConstructor constructor, List<Type> subtypes, List<String> args, int firstDynamicArg)
	{
		this.constructor = constructor;
		this.subtypes = subtypes;
		this.args = args;
		this.staticArgs = args.subList(0, firstDynamicArg);
		this.primitive = constructor.create(subtypes, staticArgs).getPrimitive();
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		return constructor.create(subtypes, parseArgs(ctx.parsed())).getSuggestions(ctx);
	}

	@Override public PrimitiveType getPrimitive()
	{
		return primitive;
	}

	private List<String> parseArgs(ParsedValue parsed)
	{
		List<String> parsedArgs = new ArrayList<>();
		for (String arg : args)
		{
			boolean isDynamicPart = arg.startsWith("$");
			if (isDynamicPart) { arg = arg.substring(1); }
			if (arg.endsWith("$")) { arg = arg.substring(0, arg.length() - 1); }

			StringBuilder builder = new StringBuilder();
			for (String part : arg.split("\\$"))
			{
				if (!isDynamicPart)
				{
					builder.append(part);
				}
				else if (part.isEmpty())
				{
					// if dollar sign escaped ($$)
					builder.append("$");
				}
				else
				{
					String dynamicArg = parseDynamic(part, parsed);
					if (dynamicArg == null) { return staticArgs; }
					builder.append(dynamicArg);
				}
				isDynamicPart = !isDynamicPart;
			}
			parsedArgs.add(builder.toString());
		}
		return parsedArgs;
	}

	private static @Nullable String parseDynamic(String arg, ParsedValue parsed)
	{
		if (arg.startsWith("@")) { return parseArgumentSwitch(arg.substring(1), parsed); }

		boolean lastAsKey = false;
		String[] parts = arg.split("/");
		if (parts.length == 0) { return null; }

		for (String part : parts)
		{
			if (part.equals(".."))
			{
				if (!(parsed instanceof ParsedValue) || parsed.parentTag == null) { return null; }
				parsed = parsed.parentTag.parentCompound;
				lastAsKey = true;
			}
			else
			{
				if (!(parsed instanceof ParsedCompound compound)) { return null; }
				ParsedTag tag = compound.get(part);

				if (tag == null) { return null; }
				parsed = tag.val;
				lastAsKey = false;
			}
		}

		return lastAsKey
				? ((parsed instanceof ParsedValue && parsed.parentTag != null) ? parsed.parentTag.key : null)
				: ((parsed instanceof ParsedPrimitive primitive) ? primitive.val : null);
	}

	private static @Nullable String parseArgumentSwitch(String arg, ParsedValue parsed)
	{
		int atCharPos = arg.indexOf('@');
		if (atCharPos == -1 || parsed.parentTag == null) { return null; }

		String tagKey = arg.substring(0, atCharPos);
		String[] values = arg.substring(atCharPos + 1).split(";");
		if (values.length % 2 != 0) { return null; }

		ParsedTag tag = parsed.parentTag.parentCompound.get(tagKey);
		if (tag == null || !(tag.val instanceof ParsedPrimitive parsedVal)) { return null; }

		for (int i = 0; i < values.length; i += 2)
		{
			if (values[i].equals(parsedVal.val)) { return values[i + 1]; }
		}
		return null;
	}
}
