package com.mt1006.nbt_ac.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin
{
	@Shadow @Final private Rect2i rect;
	@Shadow @Final private List<Suggestion> suggestionList;
	@Shadow private int offset;
	@Shadow private int current;
	private Font fontToUse = null;
	private boolean addTypeNames = false;

	@Inject(at = @At(value = "RETURN"), method = "<init>")
	private void atConstructor(CommandSuggestions commandSuggestions, int x, int y, int w,
							   List<?> suggestions, boolean narrated, CallbackInfo callbackInfo)
	{
		if (!ModConfig.showTagTypes.getValue()) { return; }

		try
		{
			if (Fields.suggestionsListRect != null && Fields.commandSuggestionsFont != null)
			{
				EditBox editBox = (EditBox)Fields.commandSuggestionsEditBox.get(commandSuggestions);
				fontToUse = (Font)Fields.commandSuggestionsFont.get(commandSuggestions);

				int newW = 0;
				for (Suggestion suggestion : (List<Suggestion>)suggestions)
				{
					String subtext = NbtSuggestionManager.getSubtext(suggestion);
					if (subtext == null)
					{
						addTypeNames = false;
						return;
					}

					newW = Math.max(newW, fontToUse.width(suggestion.getText()) + fontToUse.width(subtext) + 3);
				}

				int newX = Mth.clamp(rect.getX(), 0, editBox.getScreenX(0) + editBox.getInnerWidth() - newW) - 1;

				addTypeNames = true;
				Fields.suggestionsListRect.set(this, new Rect2i(newX, rect.getY(), newW, rect.getHeight()));
			}
		}
		catch (Exception ignore) {}
	}

	@Inject(at = @At(value = "RETURN"), method = "render")
	private void atRender(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo callbackInfo)
	{
		if (addTypeNames && Fields.suggestionsListRect != null && Fields.commandSuggestionsFont != null)
		{
			int height = rect.getHeight() / 12;

			for (int i = 0; i < height; ++i)
			{
				String subtext = NbtSuggestionManager.getSubtext(suggestionList.get(i + this.offset));
				if (subtext == null) { continue; }

				fontToUse.drawShadow(poseStack, subtext,
						(float)(rect.getX() + rect.getWidth() - fontToUse.width(subtext) - 1),
						(float)(rect.getY() + 2 + 12 * i),
						i + offset == current ? -256 : -5592406);
			}
		}
	}
}
