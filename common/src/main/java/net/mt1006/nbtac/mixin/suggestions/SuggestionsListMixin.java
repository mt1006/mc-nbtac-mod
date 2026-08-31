package net.mt1006.nbtac.mixin.suggestions;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.config.enums.PlacingOfIrrelevant;
import net.mt1006.nbtac.mixin.fields.CommandSuggestionsFields;
import net.mt1006.nbtac.utils.Fields;
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
		if (!SuggestionManager.hasCustomSuggestions) { return; }

		if (ModConfig.showTagHints.val) { initSubtext(commandSuggestions, suggestions); }
		if (ModConfig.customSorting.val) { provideCustomSorting(suggestions); }
	}

	@Inject(method = "extractRenderState", at = @At(value = "HEAD"))
	private void atRenderStart(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci)
	{
		renderLoopI = 0;
	}

	@ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"), index = 4)
	private int modifyTextColor(int color)
	{
		if (!SuggestionManager.hasCustomSuggestions || !ModConfig.grayOutIrrelevant.val || suggestionList.isEmpty())
		{
			return color;
		}

		int suggestionPos = Math.clamp(renderLoopI + offset, 0, suggestionList.size() - 1);
		CustomSuggestion.Data data = SuggestionManager.dataMap.get(suggestionList.get(suggestionPos));
		renderLoopI++;

		if (data == null || data.priority >= 0) { return color; }
		return switch (color)
		{
			case 0xFFAAAAAA -> 0xFF555555;
			case 0xFFFFFF00 -> 0xFF888800;
			default -> color;
		};
	}

	@Inject(method = "extractRenderState", at = @At(value = "RETURN"))
	private void drawSubtexts(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci)
	{
		if (!addTypeNames) { return; }
		int height = rect.getHeight() / 12;

		for (int i = 0; i < height; ++i)
		{
			String subtext = SuggestionManager.getSubtext(suggestionList.get(i + offset));
			if (subtext == null) { continue; }

			guiGraphics.text(fontToUse, subtext, rect.getX() + rect.getWidth() - fontToUse.width(subtext) - 1,
					rect.getY() + 2 + 12 * i, 0xFF555555);
		}
	}

	@Unique private void initSubtext(CommandSuggestions commandSuggestions, List<Suggestion> suggestions)
	{
		//TODO: do something with try-catch
		try
		{
			EditBox editBox = ((CommandSuggestionsFields)commandSuggestions).nbtac$getInput();
			fontToUse = ((CommandSuggestionsFields)commandSuggestions).nbtac$getFont();

			int newW = 0;
			for (Suggestion suggestion : suggestions)
			{
				String subtext = SuggestionManager.getSubtext(suggestion);
				if (subtext == null)
				{
					// this is going to break if suggestions with and without subtext are mixed
					addTypeNames = false;
					return;
				}

				newW = Math.max(newW, fontToUse.width(suggestion.getText()) + fontToUse.width(subtext) + 3);
			}

			int newX = Math.clamp(rect.getX(), 0, editBox.getScreenX(0) + editBox.getInnerWidth() - newW) - 1;

			addTypeNames = true;
			Fields.suggestionsListRect.set(this, new Rect2i(newX, rect.getY(), newW, rect.getHeight()));
		}
		catch (Exception ignore) {}
	}

	@Unique private void provideCustomSorting(List<Suggestion> suggestions)
	{
		boolean sortRecommended = ModConfig.recommendedAtTheTop.val;
		boolean sortIrrelevant = (ModConfig.placingOfIrrelevant.val != PlacingOfIrrelevant.NORMAL);
		boolean removeIrrelevant = (ModConfig.placingOfIrrelevant.val == PlacingOfIrrelevant.HIDDEN);

		int highestNotRecommended = 0;
		if (!sortRecommended)
		{
			for (CustomSuggestion.Data data : SuggestionManager.dataMap.values())
			{
				if (data.priority > highestNotRecommended && data.priority < 100)
				{
					highestNotRecommended = data.priority;
				}
			}
		}

		for (CustomSuggestion.Data data : SuggestionManager.dataMap.values())
		{
			if (data.priority >= 100) { data.order = sortRecommended ? data.priority : highestNotRecommended; }
			else if (data.priority >= 0) { data.order = data.priority; }
			else { data.order = sortIrrelevant ? data.priority : 0; }
		}

		List<Pair<Suggestion, CustomSuggestion.Data>> listToSort = new ArrayList<>();
		for (Suggestion suggestion : suggestions)
		{
			CustomSuggestion.Data data = SuggestionManager.dataMap.get(suggestion);
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
