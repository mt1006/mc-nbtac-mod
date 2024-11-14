package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyMappingFields
{
	@Accessor static @Nullable Map<String, KeyMapping> getALL() { return null; }
}
