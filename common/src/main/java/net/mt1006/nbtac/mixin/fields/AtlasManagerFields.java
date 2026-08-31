package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AtlasManager.class)
public interface AtlasManagerFields
{
	@Accessor("atlasById") Map<Identifier, AtlasEntryFields> nbtac$getAtlasById();
}
