package net.mt1006.nbtac.mixin.fields;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CompoundTag.class)
public interface CompoundTagFields
{
	@Accessor("tags") Map<String, Tag> nbtac$getTags();
}
