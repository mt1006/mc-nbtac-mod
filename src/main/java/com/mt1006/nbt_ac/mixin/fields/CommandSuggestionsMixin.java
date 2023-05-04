package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestionHelper.class)
public interface CommandSuggestionsMixin
{
	@Accessor FontRenderer getFont();
	@Accessor TextFieldWidget getInput();
}
