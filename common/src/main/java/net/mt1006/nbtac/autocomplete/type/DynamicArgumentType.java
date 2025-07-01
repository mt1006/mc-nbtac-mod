package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DynamicArgumentType implements Type
{
	private final TypeConstructor constructor;
	private final Type subtype;
	private final List<String> args;
	private final PrimitiveType primitive;

	public DynamicArgumentType(TypeConstructor constructor, Type subtype, List<String> args)
	{
		this.constructor = constructor;
		this.subtype = subtype;
		this.args = args;
		this.primitive = constructor.create(subtype, List.of()).getPrimitive();
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		constructor.create(subtype, parseArgs(args, ctx.parentInfo())).getSuggestions(ctx);
	}

	@Override public void getCompoundSuggestions(SuggestionListContext ctx)
	{
		constructor.create(subtype, parseArgs(args, ctx.parentInfo())).getCompoundSuggestions(ctx);
	}

	@Override public String getSubtext()
	{
		return primitive.getSubtext();
	}

	@Override public PrimitiveType getPrimitive()
	{
		return primitive;
	}

	private static List<String> parseArgs(List<String> args, NbtSuggestion.ParentInfo parentInfo)
	{
		List<String> parsedArgs = new ArrayList<>();
		for (String arg : args)
		{
			boolean isDynamicPart = arg.startsWith("$");
			String[] parts = arg.split("\\$");

			StringBuilder builder = new StringBuilder();
			for (String part : parts)
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
					String dynamicArg = parseDynamic(part, parentInfo);
					if (dynamicArg == null) { return List.of(); }
					builder.append(dynamicArg);
				}
				isDynamicPart = !isDynamicPart;
			}
			parsedArgs.add(builder.toString());
		}
		return parsedArgs;
	}

	private static @Nullable String parseDynamic(String arg, NbtSuggestion.ParentInfo parentInfo)
	{
		//TODO: fix
		return null;
	}
}
