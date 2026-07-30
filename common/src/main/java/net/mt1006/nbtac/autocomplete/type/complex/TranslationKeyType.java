package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.StringSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.ClientLanguageFields;
import net.mt1006.nbtac.mixin.fields.I18nFields;

public class TranslationKeyType extends ComplexType
{
	public static final TranslationKeyType INSTANCE = new TranslationKeyType();

	public TranslationKeyType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		Language lang = I18nFields.getLanguage();
		if (lang instanceof ClientLanguage)
		{
			for (String key : ((ClientLanguageFields)lang).getStorage().keySet())
			{
				list.add(new StringSuggestion(key, "[#translation_key]", ctx.parserType()));
			}
		}
	}
}
