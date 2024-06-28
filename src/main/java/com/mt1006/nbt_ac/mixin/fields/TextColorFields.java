package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextColor.class)
public interface TextColorFields
{
	@Accessor static @Nullable Map<String, TextColor> getNAMED_COLORS() { return null; }
}
