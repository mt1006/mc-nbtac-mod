package com.mt1006.nbt_ac.mixin.constructors;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public class BlockEntityMixin
{
	@Inject(at = @At(value = "RETURN"), method = "<init>")
	private void atConstructor(TileEntityType<?> blockEntityType, CallbackInfo callbackInfo) throws Exception
	{
		if (TypeLoader.getClasses && Loader.isCurrentThread())
		{
			TypeLoader.lastClass = this.getClass();
			throw new Exception();
		}
	}
}
