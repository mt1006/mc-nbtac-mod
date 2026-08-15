package net.mt1006.nbtac.config.enums;

import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public enum JsonStringSuggestion
{
	NONE,
	RECOMMENDED,
	DEFAULT_FOR_STRINGS,
	LEGACY_SEPARATED,
	LEGACY_MERGED,
	LEGACY_BACKSLASH;

	public @Nullable String get()
	{
		return switch (this)
		{
			case NONE -> null;
			case DEFAULT_FOR_STRINGS -> ModConfig.defaultQuotationMark.val.getStr(false);
			case RECOMMENDED, LEGACY_SEPARATED -> "' \"";
			case LEGACY_MERGED -> "'\"";
			case LEGACY_BACKSLASH -> "\"\\\"";
		};
	}
}
