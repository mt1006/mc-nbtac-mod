package net.mt1006.nbtac.mixin.fields;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DecoratedPotPatterns.class)
public interface DecoratedPotPatternsFields
{
	@Accessor static @Nullable Map<Item, ResourceKey<DecoratedPotPattern>> getITEM_TO_POT_TEXTURE() { return null; }
}
