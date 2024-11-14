package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DecoratedPotPatterns.class)
public interface DecoratedPotPatternsFields
{
	@Accessor static @Nullable Map<Item, ResourceKey<String>> getITEM_TO_POT_TEXTURE() { return null; }
}
