package net.mt1006.nbtac.mixin.constructors;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.nbtac.utils.BlockEntityCatcher;
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
		if (BlockEntityCatcher.thread != null && BlockEntityCatcher.thread == Thread.currentThread())
		{
			BlockEntityCatcher.lastObject = this;
			throw new Exception();
		}
	}
}
