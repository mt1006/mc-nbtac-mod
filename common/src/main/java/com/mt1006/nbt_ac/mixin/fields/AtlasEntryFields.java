package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.resources.model.sprite.AtlasManager$AtlasEntry")
public interface AtlasEntryFields
{
	@Accessor TextureAtlas getAtlas();
}
