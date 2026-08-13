package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnumType extends ComplexType
{
	private final List<String> elements;
	private final boolean ordered;

	public static EnumType dynamicEnum(List<Type> types, @Nullable String name, boolean ordered)
	{
		Object[] enumValues = switch (name)
		{
			case "ArmadilloState" -> Armadillo.ArmadilloState.values();
			case "AttributeModifierOperation" -> AttributeModifier.Operation.values();
			case "AxolotlVariant" -> Axolotl.Variant.values();
			case "Direction" -> Direction.values();
			case "DisplayBillboardConstraints" -> Display.BillboardConstraints.values();
			case "DisplayTextDisplayAlign" -> Display.TextDisplay.Align.values();
			case "DyeColor" -> DyeColor.values();
			case "EquineVariant" -> Variant.values();
			case "EquipmentSlot" -> EquipmentSlot.values();
			case "EquipmentSlotGroup" -> EquipmentSlotGroup.values();
			case "FireworkExplosionShape" -> FireworkExplosion.Shape.values();
			case "FoxVariant" -> Fox.Type.values();
			case "GossipType" -> GossipType.values();
			case "HumanoidArm" -> HumanoidArm.values();
			//case "ItemAttributeModifiersDisplayType" -> ItemAttributeModifiers.Display.Type.values();
			case "ItemDisplayContext" -> ItemDisplayContext.values();
			//case "ItemUseAnimation" -> ItemUseAnimation.values();
			case "JigsawJointType" -> JigsawBlockEntity.JointType.values();
			case "LlamaVariant" -> Llama.Variant.values();
			case "MushroomCowVariant" -> MushroomCow.MushroomType.values();
			case "PandaGene" -> Panda.Gene.values();
			case "ParrotVariant" -> Parrot.Variant.values();
			case "PlayerModelPart" -> PlayerModelPart.values();
			case "Pose" -> Pose.values();
			case "RabbitVariant" -> Rabbit.Variant.values();
			case "Rarity" -> Rarity.values();
			case "Rotation" -> Rotation.values();
			//case "SalmonVariant" -> Salmon.Variant.values();
			//case "SwingAnimationType" -> SwingAnimationType.values();
			//case "TestBlockMode" -> TestBlockMode.values();
			//case "TestInstanceStatus" -> TestInstanceBlockEntity.Status.values();
			case "TropicalFishPattern" -> TropicalFish.Pattern.values();
			case "WeatheringCopperWeatherState" -> WeatheringCopper.WeatherState.values();
			case null, default -> null;
		};

		// shouldn't happen
		if (enumValues == null) { return new EnumType(types, List.of(), ordered); }

		List<String> entries = new ArrayList<>();
		for (Object val : enumValues)
		{
			// should always be true
			if (val instanceof StringRepresentable sr) { entries.add(sr.getSerializedName()); }
		}

		return new EnumType(types, entries, ordered);
	}

	public EnumType(List<Type> types, List<String> args, boolean ordered)
	{
		super(types.isEmpty() ? PrimitiveType.STRING : types.getFirst().getPrimitive());
		this.elements = args;
		this.ordered = ordered;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		int priority = 99; // start with 99, because order is from first to last, and priority < 0 means irrelevant
		for (String element : elements)
		{
			list.add(CustomSuggestion.fromType(element, null, this, ctx.parserType(), priority));
			if (!ordered) { priority--; }
		}
	}
}
