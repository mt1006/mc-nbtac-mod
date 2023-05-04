package com.mt1006.nbt_ac.mixin.fields;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsMixin
{
	@Accessor Font getFont();
	@Accessor EditBox getInput();
}
