package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

public abstract class ComplexType implements Type
{
	protected final PrimitiveType primitive;

	public ComplexType(PrimitiveType primitive)
	{
		this.primitive = primitive;
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		SuggestionList list = new SuggestionList(ctx.parsed().pos);
		getBasicSuggestions(ctx, list);
		return list.matchOrFiler(ctx.getRemaining());
	}

	public abstract void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list);

	@Override public PrimitiveType getPrimitive()
	{
		return primitive;
	}
}
