package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelManager.class)
public interface ModelManagerFields
{
	@Accessor("bakedItemStackModels") Map<ResourceLocation, ItemModel> nbtac$getBakedItemStackModels();
}
