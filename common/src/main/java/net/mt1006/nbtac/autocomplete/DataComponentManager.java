package net.mt1006.nbtac.autocomplete;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.suggestions.ComponentSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.TagIdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Fields;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataComponentManager
{
	private static final NbtSuggestion UNKNOWN_COMPONENT = new NbtSuggestion("nbtac:empty", PrimitiveType.UNKNOWN);
	public static final Map<String, NbtSuggestion> componentMap = new HashMap<>();

	//TODO: move it somewhere else
	public static @Nullable Thread blockCatcher = null;
	public static @Nullable Object blockLastObject = null;

	public static void loadSuggestions(SuggestionList suggestionList, String str, Set<DataComponentType<?>> usedComponents,
									   @Nullable Item item, @Nullable CustomTagParser.Type parserType, boolean addSuffix)
	{
		List<Map.Entry<ResourceKey<DataComponentType<?>>, DataComponentType<?>>> entryList = new ArrayList<>();
		SharedSuggestionProvider.filterResources(RegistryUtils.DATA_COMPONENT_TYPE.entrySet(), str,
				(entry) -> entry.getKey().location(), entryList::add);

		Set<DataComponentType<?>> predefinedComponents = getPredefinedComponents(item);
		Set<DataComponentType<?>> hardcodedRelevancy = getHardcodedRelevant(item);

		for (Map.Entry<ResourceKey<DataComponentType<?>>, DataComponentType<?>> entry : entryList)
		{
			// https://minecraft.wiki/w/Data_component_format#Non-encoded_components
			ResourceLocation resLoc = entry.getKey().location();
			DataComponentType<?> componentType = entry.getValue();
			if (componentType.codec() == null || usedComponents.contains(componentType)) { continue; }

			NbtSuggestion component = DataComponentManager.componentMap.get("item/" + resLoc);
			if (component == null) { component = UNKNOWN_COMPONENT; }
			boolean relevant = predefinedComponents.contains(componentType)
					|| hardcodedRelevancy.contains(componentType) || component.isRelevant();

			if (parserType != null)
			{
				suggestionList.add(new TagIdSuggestion(component, resLoc, parserType, relevant));
			}
			else
			{
				String subtext = component.getSubtext();
				suggestionList.add(new ComponentSuggestion(resLoc, subtext, relevant, addSuffix));
			}
		}
	}

	private static Set<DataComponentType<?>> getPredefinedComponents(@Nullable Item item)
	{
		if (item == null) { return Set.of(); }
		return item.components().keySet();
	}

	private static Set<DataComponentType<?>> getHardcodedRelevant(@Nullable Item item)
	{
		if (item == null) { return Set.of(); }
		Set<DataComponentType<?>> relevant = new HashSet<>();

		if (ModConfig.showCustomDataAsRelevant.val) { relevant.add(DataComponents.CUSTOM_DATA); }
		if (ModConfig.showCustomModelDataAsRelevant.val) { relevant.add(DataComponents.CUSTOM_MODEL_DATA); }
		if (item.builtInRegistryHolder().is(ItemTags.DYEABLE)) { relevant.add(DataComponents.DYED_COLOR); }

		if (item instanceof SpawnEggItem || item instanceof HangingEntityItem || item instanceof ArmorStandItem
				|| item instanceof MinecartItem || item instanceof BoatItem)
		{
			relevant.add(DataComponents.ENTITY_DATA);
		}

		if (item instanceof BlockItem)
		{
			Block block = ((BlockItem)item).getBlock();

			relevant.add(DataComponents.CAN_PLACE_ON);
			if (!block.defaultBlockState().getProperties().isEmpty()) { relevant.add(DataComponents.BLOCK_STATE); }
			if (block instanceof EntityBlock) { getHardcodedBlockEntityRelevant(relevant, block); }
			if (block instanceof DecoratedPotBlock) { relevant.add(DataComponents.POT_DECORATIONS); }

			if (block instanceof PlayerHeadBlock)
			{
				relevant.add(DataComponents.NOTE_BLOCK_SOUND);
				relevant.add(DataComponents.PROFILE);
			}
		}
		else
		{
			if (item == Items.DEBUG_STICK) { relevant.add(DataComponents.DEBUG_STICK_STATE); }
			if (item == Items.OMINOUS_BOTTLE) { relevant.add(DataComponents.OMINOUS_BOTTLE_AMPLIFIER); }
			if (item == Items.ENCHANTED_BOOK) { relevant.add(DataComponents.STORED_ENCHANTMENTS); }
			if (item == Items.FILLED_MAP) { relevant.add(DataComponents.FIREWORK_EXPLOSION); }
			if (item instanceof FireworkRocketItem) { relevant.add(DataComponents.FIREWORKS); }
			if (item instanceof InstrumentItem) { relevant.add(DataComponents.INSTRUMENT); }
			if (item instanceof CrossbowItem) { relevant.add(DataComponents.CHARGED_PROJECTILES); }
			if (item instanceof ArrowItem) { relevant.add(DataComponents.INTANGIBLE_PROJECTILE); }
			if (item instanceof CompassItem) { relevant.add(DataComponents.LODESTONE_TRACKER); }
			if (item instanceof KnowledgeBookItem) { relevant.add(DataComponents.RECIPES); }
			if (item instanceof WritableBookItem) { relevant.add(DataComponents.WRITABLE_BOOK_CONTENT); }
			if (item instanceof WrittenBookItem) { relevant.add(DataComponents.WRITTEN_BOOK_CONTENT); }

			if (item.components().has(DataComponents.EQUIPPABLE)) //TODO: improve
			{
				relevant.add(DataComponents.TRIM);
			}

			if (item instanceof PotionItem || item instanceof TippedArrowItem)
			{
				relevant.add(DataComponents.POTION_CONTENTS);
				relevant.add(DataComponents.POTION_DURATION_SCALE);
			}

			if (item instanceof MapItem)
			{
				relevant.add(DataComponents.MAP_COLOR);
				relevant.add(DataComponents.MAP_DECORATIONS);
				relevant.add(DataComponents.MAP_ID);
			}

			if (Fields.isMethodOverridden(Fields.itemUseOnMethodData, item, Item.class))
			{
				relevant.add(DataComponents.CAN_PLACE_ON);
			}
		}

		return relevant;
	}

	private static void getHardcodedBlockEntityRelevant(Set<DataComponentType<?>> relevant, Block block)
	{
		relevant.add(DataComponents.BLOCK_ENTITY_DATA);

		// this blockEntity SHOULDN'T be used with anything other than instanceof
		BlockEntity blockEntity = blockEntityFromBlock(block);
		if (blockEntity instanceof Container)
		{
			relevant.add(DataComponents.CONTAINER);
			if (blockEntity instanceof RandomizableContainer) { relevant.add(DataComponents.CONTAINER_LOOT); }
			if (blockEntity instanceof BaseContainerBlockEntity) { relevant.add(DataComponents.LOCK); }
		}
	}

	public static @Nullable BlockEntity blockEntityFromBlock(Block block)
	{
		if (!ModConfig.allowBlockEntityExtraction.val) { return null; }
		ResourceLocation resLoc = RegistryUtils.BLOCK.getKey(block);
		if (resLoc == null) { return null; }

		BlockEntityType<?> blockEntityType = RegistryUtils.BLOCK_ENTITY_TYPE.get(resLoc);
		if (blockEntityType == null) { return null; }

		if (blockCatcher != null && blockCatcher != Thread.currentThread()) { return null; }
		blockCatcher = Thread.currentThread();
		blockLastObject = null;

		try
		{
			blockEntityType.create(BlockPos.ZERO, null);
		}
		catch (Throwable throwable)
		{
			if (throwable instanceof Error)
			{
				NBTac.LOGGER.error("Block entity \"{}\" constructor thrown error: {}", resLoc, throwable);
			}
		}

		blockCatcher = null;
		return (blockLastObject instanceof BlockEntity) ? (BlockEntity)blockLastObject : null;
	}
}
