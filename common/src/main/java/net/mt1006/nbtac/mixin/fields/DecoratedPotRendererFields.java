package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DecoratedPotRenderer.class)
public interface DecoratedPotRendererFields
{
	@Accessor static @Nullable Map<ResourceKey<Item>, SpriteId> getDECORATED_POT_SPRITES() { return null; }
}
