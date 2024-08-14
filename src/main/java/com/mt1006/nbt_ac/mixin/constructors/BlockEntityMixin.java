package com.mt1006.nbt_ac.mixin.constructors;

import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin
{
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;validateBlockState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
	private void atConstructor(BlockEntityType<?> blockEntityType, BlockPos blockPos,
							   BlockState blockState, CallbackInfo ci) throws Exception
	{
		if (TypeLoader.objectCatcher != null && TypeLoader.objectCatcher == Thread.currentThread())
		{
			TypeLoader.lastObject = this;
			throw new Exception();
		}
	}
}
