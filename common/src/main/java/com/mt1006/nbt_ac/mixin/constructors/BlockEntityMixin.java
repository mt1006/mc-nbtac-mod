package com.mt1006.nbt_ac.mixin.constructors;

import com.mt1006.nbt_ac.autocomplete.DataComponentManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin
{
	@Inject(method = "validateBlockState", at = @At(value = "HEAD"))
	private void atValidateBlockState(BlockState blockState, CallbackInfo ci) throws Exception
	{
		if (DataComponentManager.blockCatcher != null && DataComponentManager.blockCatcher == Thread.currentThread())
		{
			DataComponentManager.blockLastObject = this;
			throw new Exception();
		}
	}
}
