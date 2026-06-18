package com.mt1006.nbt_ac.mixin.constructors;

import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin
{
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNextEntityId()I"))
	private int atConstructor(Level level) throws Exception
	{
		if (TypeLoader.objectCatcher != null && TypeLoader.objectCatcher == Thread.currentThread())
		{
			TypeLoader.lastObject = this;
			throw new Exception();
		}
		return level.getNextEntityId();
	}
}
