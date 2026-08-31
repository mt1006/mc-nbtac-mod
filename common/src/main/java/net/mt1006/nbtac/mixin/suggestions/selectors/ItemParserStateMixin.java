package net.mt1006.nbtac.mixin.suggestions.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.utils.RegistryUtils;
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
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
		catch (CommandSyntaxException ignore) {}
		reader.setCursor(currentCursor);
	}

	@Inject(method = "readComponents", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/item/ItemParser$State;readComponent(Lnet/minecraft/nbt/TagParser;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/core/component/DataComponentType;)V"))
	private void atReadComponents(CallbackInfo ci)
	{
		visitor.visitSuggestions((builder) -> suggestComponentData(builder, false));
		cursorBeforeComponent = reader.getCursor();
	}

	@Inject(method = "suggestComponentAssignmentOrRemoval", at = @At(value = "HEAD"), cancellable = true)
	private void atSuggestComponentAssignment(SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		Item item = findParsedItem();
		String str = builder.getRemaining().toLowerCase(Locale.ROOT);

		SuggestionList suggestionList = new SuggestionList();
		DataComponentManager.loadSuggestions(suggestionList, str, parsedComponents, item, true);
		if (str.isEmpty() || str.equals("!")) { suggestionList.addRaw("!", "(remove component)", 80); }
		suggestionList.forEach((s) -> s.suggest(builder));

		cir.setReturnValue(builder.buildFuture());
		cir.cancel();
	}

	@Inject(method = "suggestNextOrEndComponents", at = @At(value = "HEAD"), cancellable = true)
	private void atSuggestNextOrEndComponents(SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		// this is necessary for some components like, e.g. mc component parser treats
		// item_mode=a as valid component, therefore it tries to suggest , or ]
		if (!builder.getRemaining().isEmpty()) { cir.setReturnValue(builder.buildFuture()); }
		else { cir.setReturnValue(suggestComponentData(builder.createOffset(cursorBeforeComponent), true)); }
		cir.cancel();
	}

	@Inject(method = "suggestComponent(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"), cancellable = true)
	private void atSuggestComponentRemoval(SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		Item item = findParsedItem();
		String str = builder.getRemaining().toLowerCase(Locale.ROOT);

		SuggestionList suggestionList = new SuggestionList();
		DataComponentManager.loadSuggestions(suggestionList, str, parsedComponents, item, false);
		suggestionList.forEach((s) -> s.suggest(builder));

		cir.setReturnValue(builder.buildFuture());
		cir.cancel();
	}

	@Unique private @Nullable Identifier findParsedItemId()
	{
		if (cursorBeforeItem == -1) { return null; }
		Identifier id = null;

		int currentCursor = reader.getCursor();
		reader.setCursor(cursorBeforeItem);
		try
		{
			id = Identifier.read(reader);
		}
		catch (CommandSyntaxException ignore) {}
		reader.setCursor(currentCursor);

		return id;
	}

	@Unique private @Nullable Item findParsedItem()
	{
		Identifier id = findParsedItemId();
		return id != null ? RegistryUtils.ITEM.get(id) : null;
	}

	@Unique private CompletableFuture<Suggestions> suggestComponentData(SuggestionsBuilder builder, boolean withTailSuggestions)
	{
		Identifier componentId = lastAdded != null ? RegistryUtils.DATA_COMPONENT_TYPE.getKey(lastAdded) : null;
		NbtTag component = componentId != null ? DataComponentManager.componentMap.get("item/" + componentId) : null;
		if (component == null || cursorBeforeComponent == -1) { return Suggestions.empty(); }

		String val = reader.getString().substring(cursorBeforeComponent);
		CustomTagParser parser = CustomTagParser.forDataComponentValue(val, component.getType(), findParsedItemId());

		// if everything works fine, this check probably isn't necessary, but it is used in cases
		// where data component value isn't valid for mc, but is valid for NBTac, e.g. entity_data={}
		if (withTailSuggestions)
		{
			parser.tailSuggestions = new SuggestionList(val.length());
			parser.tailSuggestions.addRaw(",", null);
			parser.tailSuggestions.addRaw("]", null);
		}

		return SuggestionManager.finishSuggestions(parser::parse, builder, Function.identity());
	}
}
