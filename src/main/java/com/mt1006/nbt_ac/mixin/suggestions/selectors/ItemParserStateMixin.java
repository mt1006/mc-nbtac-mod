package com.mt1006.nbt_ac.mixin.suggestions.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.DataComponentManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.commands.arguments.item.ItemParser$State")
public abstract class ItemParserStateMixin
{
	@Shadow @Final private StringReader reader;
	@Shadow @Final private ItemParser.Visitor visitor;
	@Unique private int cursorBeforeItem = -1, cursorBeforeComponent = -1;
	@Unique private final Set<DataComponentType<?>> parsedComponents = new HashSet<>();
	@Unique private @Nullable DataComponentType<?> lastAdded = null;

	@Shadow public static DataComponentType<?> readComponentType(StringReader reader) throws CommandSyntaxException { return null; }

	@Inject(method = "readItem", at = @At(value = "HEAD"))
	private void atReadItem(CallbackInfo ci)
	{
		// capturing ID of an item could also be done with @Redirect but this is probably better in terms of compatibility
		cursorBeforeItem = reader.getCursor();
	}

	@Inject(method = "readComponents", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/item/ItemParser$State;readComponentType(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/core/component/DataComponentType;"))
	private void captureComponentId(CallbackInfo ci)
	{
		// also could be done with @Redirect
		int currentCursor = reader.getCursor();
		try
		{
			DataComponentType<?> componentType = readComponentType(reader);
			parsedComponents.add(componentType);
			lastAdded = componentType;
		}
		catch (CommandSyntaxException ignored) {}
		reader.setCursor(currentCursor);
	}

	@Inject(method = "readComponents", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/item/ItemParser$State;readComponent(Lnet/minecraft/core/component/DataComponentType;)V"))
	private void atReadComponents(CallbackInfo ci)
	{
		visitor.visitSuggestions(this::suggestComponentData);
		cursorBeforeComponent = reader.getCursor();
	}

	@Inject(method = "suggestComponentAssignmentOrRemoval", at = @At(value = "HEAD"), cancellable = true)
	private void atSuggestComponentAssignment(SuggestionsBuilder suggestionsBuilder,
											  CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		//TODO: add setting to disable it?
		Item item = findParsedItem();
		String str = suggestionsBuilder.getRemaining().toLowerCase();

		SuggestionList suggestionList = new SuggestionList();
		DataComponentManager.loadSuggestions(suggestionList, str, parsedComponents, item, null, true);
		if (str.isEmpty() || str.equals("!")) { suggestionList.addRaw("!", "(remove component)", 200); }
		suggestionList.forEach((s) -> s.suggest(suggestionsBuilder));

		cir.setReturnValue(suggestionsBuilder.buildFuture());
		cir.cancel();
	}

	@Inject(method = "suggestComponent(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"), cancellable = true)
	private void atSuggestComponentRemoval(SuggestionsBuilder suggestionsBuilder,
											  CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		Item item = findParsedItem();
		String str = suggestionsBuilder.getRemaining().toLowerCase();

		SuggestionList suggestionList = new SuggestionList();
		DataComponentManager.loadSuggestions(suggestionList, str, parsedComponents, item, null, false);
		suggestionList.forEach((s) -> s.suggest(suggestionsBuilder));

		cir.setReturnValue(suggestionsBuilder.buildFuture());
		cir.cancel();
	}

	@Unique private @Nullable ResourceLocation findParsedItemId()
	{
		if (cursorBeforeItem == -1) { return null; }
		ResourceLocation resLoc = null;

		int currentCursor = reader.getCursor();
		reader.setCursor(cursorBeforeItem);
		try
		{
			resLoc = ResourceLocation.read(reader);
		}
		catch (CommandSyntaxException ignored) {}
		reader.setCursor(currentCursor);

		return resLoc;
	}

	@Unique private @Nullable Item findParsedItem()
	{
		ResourceLocation resLoc = findParsedItemId();

		//TODO: remove null check (set RegistryUtils arguments as nullable if safe on older versions)
		if (resLoc == null) { return null; }
		return RegistryUtils.ITEM.get(resLoc);
	}

	@Unique private CompletableFuture<Suggestions> suggestComponentData(SuggestionsBuilder suggestionsBuilder)
	{
		ResourceLocation resLoc = lastAdded != null ? RegistryUtils.DATA_COMPONENT_TYPE.getKey(lastAdded) : null;
		NbtSuggestion component = resLoc != null ? DataComponentManager.componentMap.get("item/" + resLoc) : null;
		if (component == null || cursorBeforeComponent == -1) { return suggestionsBuilder.buildFuture(); }

		String tag = reader.getString().substring(cursorBeforeComponent);
		ResourceLocation itemId = findParsedItemId();
		SuggestionList suggestionList = new SuggestionList();
		CustomTagParser tagParser = new CustomTagParser(tag, CustomTagParser.Type.COMPONENT);
		CustomTagParser.Suggestion suggestion =
				tagParser.read(suggestionList, component, itemId != null ? itemId.toString() : null);

		//TODO: make it safer
		int cursorShift = cursorBeforeComponent + tagParser.getCursor() - suggestionsBuilder.getStart();
		return NbtSuggestionManager.finishSuggestions(suggestionList, suggestionsBuilder, suggestion, cursorShift);
	}
}
