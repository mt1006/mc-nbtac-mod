package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedValue;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.autocomplete.type.complex.*;
import net.mt1006.nbtac.autocomplete.type.compound.*;
import net.mt1006.nbtac.autocomplete.type.string.EntitySelectorType;
import net.mt1006.nbtac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Type
{
	Map<String, TypeConstructor> MAP = Map.<String, TypeConstructor>ofEntries(
			Map.entry("unknown", (s, a) -> PrimitiveType.UNKNOWN),
			Map.entry("boolean", (s, a) -> PrimitiveType.BOOLEAN),
			Map.entry("byte", (s, a) -> PrimitiveType.BYTE),
			Map.entry("short", (s, a) -> PrimitiveType.SHORT),
			Map.entry("int", (s, a) -> PrimitiveType.INT),
			Map.entry("long", (s, a) -> PrimitiveType.LONG),
			Map.entry("float", (s, a) -> PrimitiveType.FLOAT),
			Map.entry("double", (s, a) -> PrimitiveType.DOUBLE),
			Map.entry("string", (s, a) -> PrimitiveType.STRING),
			Map.entry("byte_array", (s, a) -> ArrayType.BYTE),
			Map.entry("int_array", (s, a) -> ArrayType.INT),
			Map.entry("long_array", (s, a) -> ArrayType.LONG),
			Map.entry("compound", (s, a) -> new CompoundType()),
			Map.entry("list", (s, a) -> new ListType(s.getFirst())),
			Map.entry("BlockStateTags", (s, a) -> new BlockStateTagsType(firstOrNull(a))),
			Map.entry("DescribedEnum", DescribedEnumType::new),
			Map.entry("DyeColor", (s, a) -> EnumType.DYE_COLOR),
			Map.entry("EmptyCompound", (s, a) -> EmptyCompound.INSTANCE),
			Map.entry("Enchantments", (s, a) -> EnchantmentsType.INSTANCE),
			Map.entry("EntitySelector", (s, a) -> EntitySelectorType.INSTANCE),
			Map.entry("Enum", (s, a) -> new EnumType(s, a, false)),
			Map.entry("OrderedEnum", (s, a) -> new EnumType(s, a, true)),
			Map.entry("Font", (s, a) -> FontType.INSTANCE),
			Map.entry("PlayerInventorySlot", (s, a) -> new PlayerInventorySlotType(s.getFirst())),
			Map.entry("Keybind", (s, a) -> KeybindType.INSTANCE),
			Map.entry("LongSeed", (s, a) -> LongSeedType.INSTANCE),
			Map.entry("LootTable", (s, a) -> new ServerRegistryKeyType<>("loot_table")),
			Map.entry("MapDecorations", (s, a) -> MapDecorationsType.INSTANCE),
			Map.entry("MapDecorationType", (s, a) -> MapDecorationTypeType.INSTANCE),
			Map.entry("PotDecoration", (s, a) -> PotDecorationType.INSTANCE),
			Map.entry("Recipe", (s, a) -> RecipeType.INSTANCE),
			Map.entry("RegistryKey", (s, a) -> new RegistryKeyType(firstOrNull(a))),
			Map.entry("ServerRegistryKey", (s, a) -> new ServerRegistryKeyType<>(firstOrNull(a))),
			Map.entry("Id", (s, a) -> new IdType(firstOrNull(a))),
			Map.entry("SpawnEgg", (s, a) -> new SpawnEggType(firstOrNull(a))),
			Map.entry("BannerPattern", (s, a) -> StaticIdsType.BANNER_PATTERN),
			Map.entry("TrimPattern", (s, a) -> StaticIdsType.TRIM_PATTERN),
			Map.entry("TrimMaterial", (s, a) -> StaticIdsType.TRIM_MATERIAL),
			Map.entry("JukeboxSong", (s, a) -> StaticIdsType.JUKEBOX_SONG),
			Map.entry("DamageType", (s, a) -> StaticIdsType.DAMAGE_TYPE),
			Map.entry("Tags", (s, a) -> new TagsType(firstOrNull(a), null, false)),
			Map.entry("TagsWithId", (s, a) -> new TagsType(firstOrNull(a), null, true)),
			Map.entry("TagsFromId", (s, a) -> new TagsType(a.size() > 1 ? a.get(1) : null, a.getFirst(), true)),
			Map.entry("TextColor", (s, a) -> TextColorType.INSTANCE),
			Map.entry("TextComponent", (s, a) -> TextComponentType.INSTANCE),
			Map.entry("TranslationKey", (s, a) -> TranslationKeyType.INSTANCE),
			Map.entry("UUID", (s, a) -> UUIDType.UNKNOWN),
			Map.entry("RandomUUID", (s, a) -> UUIDType.RANDOM),
			Map.entry("ScoreboardTeam", (s, a) -> ScoreboardTeamType.INSTANCE),
			Map.entry("BlockPos", (s, a) -> ArrayType.BLOCK_POS),
			Map.entry("ItemComponents", (s, a) -> new ItemComponentsType(firstOrNull(a))),
			Map.entry("either", (s, a) -> new EitherType(s)),
			Map.entry("Brain", (s, a) -> new BrainType()),
			Map.entry("Depends", DependsType::new),
			Map.entry("SingleOrList", (s, a) -> new EitherType(List.of(s.getFirst(), new ListType(s.getFirst())))),
			Map.entry("ArmorStandSlots", (s, a) -> ArmorStandSlotsType.INSTANCE),
			Map.entry("TropicalFishVariant", (s, a) -> PrimitiveType.INT), //TODO: finish
			Map.entry("HorseVariant", (s, a) -> PrimitiveType.INT), //TODO: finish
			Map.entry("FurnaceRecipesUsed", (s, a) -> PrimitiveType.COMPOUND) //TODO: finish
	);

	@Nullable SuggestionList getSuggestions(SuggestionListContext ctx);

	default String getSubtext()
	{
		return getPrimitive().getSubtext();
	}

	PrimitiveType getPrimitive();

	default NbtTagMap getSubcompound()
	{
		throw new UnsupportedOperationException();
	}

	default void setSubcompound(@Nullable NbtTagMap subcompound)
	{
		if (subcompound != null) { throw new UnsupportedOperationException(); }
	}

	private static <T> @Nullable T firstOrNull(List<T> args)
	{
		return args.isEmpty() ? null : args.getFirst();
	}

	record SuggestionListContext(ParsedValue parsed,
								 ParserType parserType,
								 SimpleStringReader reader,
								 @Nullable SuggestionList expectedOperators)
	{
		public SuggestionListContext child(ParsedValue newParsed)
		{
			return new SuggestionListContext(newParsed, parserType, reader, expectedOperators);
		}

		public String getRemaining()
		{
			return reader.substring(parsed.pos);
		}
	}

	interface TypeConstructor
	{
		Type create(List<Type> subtypes, List<String> args);
	}
}
