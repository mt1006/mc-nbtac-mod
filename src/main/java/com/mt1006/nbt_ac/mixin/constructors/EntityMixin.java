package com.mt1006.nbt_ac.mixin.constructors;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin
{
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getDimensions()Lnet/minecraft/world/entity/EntityDimensions;"))
	private EntityDimensions atConstructor(EntityType<?> entityType) throws Exception
	{
		if (TypeLoader.getClasses && Loader.isCurrentThread())
		{
			TypeLoader.lastClass = this.getClass();
			throw new Exception();
		}
		return entityType.getDimensions();
	}
}
