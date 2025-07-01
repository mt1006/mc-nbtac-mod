package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.CustomTagParser;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.type.complex.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Type
{
	Map<String, TypeConstructor> MAP = Map.ofEntries(
			Map.entry("unknown", (s, a) -> PrimitiveType.UNKNOWN),
			Map.entry("boolean", (s, a) -> PrimitiveType.BOOLEAN),
			Map.entry("byte", (s, a) -> PrimitiveType.BYTE),
			Map.entry("short", (s, a) -> PrimitiveType.SHORT),
			Map.entry("int", (s, a) -> PrimitiveType.INT),
			Map.entry("long", (s, a) -> PrimitiveType.LONG),
			Map.entry("float", (s, a) -> PrimitiveType.FLOAT),
			Map.entry("double", (s, a) -> PrimitiveType.DOUBLE),
			Map.entry("string", (s, a) -> PrimitiveType.STRING),
			Map.entry("byte_array", (s, a) -> PrimitiveType.BYTE_ARRAY),
			Map.entry("int_array", (s, a) -> PrimitiveType.INT_ARRAY),
			Map.entry("long_array", (s, a) -> PrimitiveType.LONG_ARRAY),
			Map.entry("compound", (s, a) -> PrimitiveType.COMPOUND),
			Map.entry("list", (s, a) -> PrimitiveType.LIST),
			Map.entry("List", (s, a) -> new ListType(s)),
			Map.entry("BlockStateTags", (s, a) -> new BlockStateTagsType(firstOrNull(a))),
			Map.entry("DescribedEnum", DescribedEnumType::new),
			Map.entry("DyeColor", (s, a) -> DyeColorType.INSTANCE),
			Map.entry("EmptyCompound", (s, a) -> EmptyCompound.INSTANCE),
			Map.entry("Enchantments", (s, a) -> EnchantmentsType.INSTANCE),
			Map.entry("EntitySelector", (s, a) -> EntitySelectorType.INSTANCE),
			Map.entry("Enum", (s, a) -> new EnumType(s, a, false)),
			Map.entry("OrderedEnum", (s, a) -> new EnumType(s, a, true)),
			Map.entry("Font", (s, a) -> FontType.INSTANCE),
			Map.entry("InventorySlot", (s, a) -> new InventorySlotType(s)),
			Map.entry("Keybind", (s, a) -> KeybindType.INSTANCE),
			Map.entry("LongSeed", (s, a) -> LongSeedType.INSTANCE),
			Map.entry("LootTable", (s, a) -> LootTableType.INSTANCE),
			Map.entry("MapDecorations", (s, a) -> MapDecorationsType.INSTANCE),
			Map.entry("MapDecorationType", (s, a) -> MapDecorationTypeType.INSTANCE),
			Map.entry("PotDecoration", (s, a) -> PotDecorationType.INSTANCE),
			Map.entry("Recipe", (s, a) -> RecipeType.INSTANCE),
			Map.entry("RegistryKey", (s, a) -> new RegistryKeyType(firstOrNull(a))),
			Map.entry("RequiredId", (s, a) -> new RequiredIdType(firstOrNull(a))),
			Map.entry("SpawnEgg", (s, a) -> new SpawnEggType(firstOrNull(a))),
			Map.entry("BannerPattern", (s, a) -> StaticIdsType.BANNER_PATTERN),
			Map.entry("TrimPattern", (s, a) -> StaticIdsType.TRIM_PATTERN),
			Map.entry("TrimMaterial", (s, a) -> StaticIdsType.TRIM_MATERIAL),
			Map.entry("JukeboxSong", (s, a) -> StaticIdsType.JUKEBOX_SONG),
			Map.entry("DamageType", (s, a) -> StaticIdsType.DAMAGE_TYPE),
			Map.entry("Tags", (s, a) -> new TagsType(firstOrNull(a), false)),
			Map.entry("TagsWithId", (s, a) -> new TagsType(firstOrNull(a), true)),
			Map.entry("TextColor", (s, a) -> TextColorType.INSTANCE),
			Map.entry("TextComponent", (s, a) -> TextComponentType.INSTANCE),
			Map.entry("TranslationKey", (s, a) -> TranslationKeyType.INSTANCE),
			Map.entry("UUID", (s, a) -> UUIDType.UNKNOWN),
			Map.entry("RandomUUID", (s, a) -> UUIDType.RANDOM)
	);

	void getSuggestions(SuggestionListContext ctx);

	default void getCompoundSuggestions(SuggestionListContext ctx) {}

	String getSubtext();

	PrimitiveType getPrimitive();

	private static @Nullable String firstOrNull(List<String> args)
	{
		return !args.isEmpty() ? args.getFirst() : null;
	}

	record SuggestionListContext(SuggestionList list,
								 CustomTagParser.Type parserType,
								 NbtSuggestion.ParentInfo parentInfo) {}

	interface TypeConstructor
	{
		Type create(@Nullable Type subtype, List<String> args);
	}
}
