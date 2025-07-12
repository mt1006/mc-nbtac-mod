package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(I18n.class)
public interface I18nFields
{
	@Accessor static @Nullable Language getLanguage() { return null; }
}
