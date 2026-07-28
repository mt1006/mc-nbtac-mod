package net.mt1006.nbtac.mixin.fields;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(EntitySelector.class)
public interface EntitySelectorFields
{
	@Accessor @Nullable String getPlayerName();
	@Accessor @Nullable UUID getEntityUUID();
	@Accessor EntityTypeTest<Entity, ?> getType();
}
