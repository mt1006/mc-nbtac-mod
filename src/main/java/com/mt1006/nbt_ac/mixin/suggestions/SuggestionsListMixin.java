package com.mt1006.nbt_ac.mixin.suggestions;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.mixin.fields.CommandSuggestionsFields;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class SuggestionsListMixin
{
	@Shadow @Final private Rect2i rect;
	@Shadow @Final private List<Suggestion> suggestionList;
	@Shadow private int offset;
	@Unique private Font fontToUse = null;
	@Unique private boolean addTypeNames = false;
	@Unique private int renderLoopI = 0;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void atConstructor(CommandSuggestions commandSuggestions, int x, int y, int w,
							   List<Suggestion> suggestions, boolean narrated, CallbackInfo ci)
	{
		addTypeNames = false;
		if (!NbtSuggestionManager.hasCustomSuggestions) { return; }

		if (ModConfig.showTagHints.val) { initSubtext(commandSuggestions, suggestions); }
		if (ModConfig.customSorting.val) { provideCustomSorting(suggestions); }
	}

	@Inject(method = "render", at = @At(value = "HEAD"))
	private void atRenderStart(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci)
	{
		renderLoopI = 0;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"), index = 4)
	private int modifyTextColor(int color)
	{
		if (!NbtSuggestionManager.hasCustomSuggestions || !ModConfig.grayOutIrrelevant.val || suggestionList.isEmpty())
		{
			return color;
		}

		int suggestionPos = Mth.clamp(renderLoopI + offset, 0, suggestionList.size() - 1);
		CustomSuggestion.Data data = NbtSuggestionManager.dataMap.get(suggestionList.get(suggestionPos));
		renderLoopI++;

		if (data == null || data.priority >= 0) { return color; }
		return switch (color)
		{
			case 0xFFAAAAAA -> 0xFF555555;
			case 0xFFFFFF00 -> 0xFF888800;
			default -> color;
		};
	}

	@Inject(method = "render", at = @At(value = "RETURN"))
	private void drawSubtexts(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci)
	{
		if (!addTypeNames) { return; }
		int height = rect.getHeight() / 12;

		for (int i = 0; i < height; ++i)
		{
			String subtext = NbtSuggestionManager.getSubtext(suggestionList.get(i + offset));
			if (subtext == null) { continue; }

			guiGraphics.drawString(fontToUse, subtext, rect.getX() + rect.getWidth() - fontToUse.width(subtext) - 1,
					rect.getY() + 2 + 12 * i, 0xFF555555);
		}
	}

	@Unique private void initSubtext(CommandSuggestions commandSuggestions, List<Suggestion> suggestions)
	{
		//TODO: do something with try-catch
		try
		{
			EditBox editBox = ((CommandSuggestionsFields)commandSuggestions).getInput();
			fontToUse = ((CommandSuggestionsFields)commandSuggestions).getFont();

			int newW = 0;
			for (Suggestion suggestion : suggestions)
			{
				String subtext = NbtSuggestionManager.getSubtext(suggestion);
				if (subtext == null)
				{
					// this is going to break if suggestions with and without subtext are mixed
					addTypeNames = false;
					return;
				}

				newW = Math.max(newW, fontToUse.width(suggestion.getText()) + fontToUse.width(subtext) + 3);
			}

			int newX = Mth.clamp(rect.getX(), 0, editBox.getScreenX(0) + editBox.getInnerWidth() - newW) - 1;

			addTypeNames = true;
			Fields.suggestionsListRect.set(this, new Rect2i(newX, rect.getY(), newW, rect.getHeight()));
		}
		catch (Exception ignore) {}
	}

	@Unique private void provideCustomSorting(List<Suggestion> suggestions)
	{
		boolean sortRecommended = ModConfig.recommendedAtTheTop.val;
		boolean sortIrrelevant = (ModConfig.placingOfIrrelevant.val != 0);
		boolean removeIrrelevant = (ModConfig.placingOfIrrelevant.val == 2);

		int highestNotRecommended = 0;
		if (!sortRecommended)
		{
			for (CustomSuggestion.Data data : NbtSuggestionManager.dataMap.values())
			{
				if (data.priority >= 100) { continue; }
				if (data.priority > highestNotRecommended) { highestNotRecommended = data.priority; }
			}
		}

		for (CustomSuggestion.Data data : NbtSuggestionManager.dataMap.values())
		{
			if (data.priority >= 100) { data.order = sortRecommended ? data.priority : highestNotRecommended; }
			else if (data.priority >= 0) { data.order = data.priority; }
			else { data.order = sortIrrelevant ? data.priority : 0; }
		}

		List<Pair<Suggestion, CustomSuggestion.Data>> listToSort = new ArrayList<>();
		for (Suggestion suggestion : suggestions)
		{
			CustomSuggestion.Data data = NbtSuggestionManager.dataMap.get(suggestion);
			if (data == null) { data = CustomSuggestion.Data.error(); }

			if (data.priority < 0 && removeIrrelevant) { continue; }
			listToSort.add(Pair.of(suggestion, data));
		}
		listToSort.sort((a, b) -> suggestionDataComparator(a.getRight(), b.getRight()));

		List<Suggestion> newList = new ArrayList<>();
		listToSort.forEach((pair) -> newList.add(pair.getLeft()));

		try { Fields.suggestionsListList.set(this, newList); }
		catch (Exception ignore) {}
	}

	@Unique private static int suggestionDataComparator(CustomSuggestion.Data a, CustomSuggestion.Data b)
	{
		return Integer.compare(b.order, a.order);
	}
}
