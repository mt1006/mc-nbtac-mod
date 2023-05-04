package com.mt1006.nbt_ac.mixin.fields;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public interface ClientLevelMixin
{
	@Accessor Int2ObjectMap<Entity> getEntitiesById();
}
