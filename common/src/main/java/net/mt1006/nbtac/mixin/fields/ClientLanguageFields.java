package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ClientLanguage.class)
public interface ClientLanguageFields
{
	@Accessor("storage") Map<String, String> nbtac$getStorage();
}
