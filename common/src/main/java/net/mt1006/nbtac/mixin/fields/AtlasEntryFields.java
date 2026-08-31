package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.resources.model.AtlasManager$AtlasEntry")
public interface AtlasEntryFields
{
	@Accessor("atlas") TextureAtlas nbtac$getAtlas();
}
