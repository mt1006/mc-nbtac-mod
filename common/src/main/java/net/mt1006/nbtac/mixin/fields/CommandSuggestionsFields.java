package net.mt1006.nbtac.mixin.fields;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsFields
{
	@Accessor("font") Font nbtac$getFont();
	@Accessor("input") EditBox nbtac$getInput();
}
