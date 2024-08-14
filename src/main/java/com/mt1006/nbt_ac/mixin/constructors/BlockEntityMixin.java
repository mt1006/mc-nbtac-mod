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
	// order = 500 is used, so it will be injected before Lithium's initSupportCache
	// Lithium's mixin: https://github.com/CaffeineMC/lithium-fabric/blob/develop/src/main/java/me/jellysquid/mods/lithium/mixin/minimal_nonvanilla/world/block_entity_ticking/support_cache/BlockEntityMixin.java
	// Issue: https://github.com/mt1006/mc-nbtac-mod/issues/28
	@Inject(method = "<init>", at = @At(value = "RETURN"), order = 500)
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
