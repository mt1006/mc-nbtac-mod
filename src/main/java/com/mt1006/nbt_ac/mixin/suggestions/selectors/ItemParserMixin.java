package com.mt1006.nbt_ac.mixin.suggestions.selectors;

import net.minecraft.commands.arguments.item.ItemParser;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemParser.class)
public class ItemParserMixin
{
	//TODO: add support for item components (1.3)
	/*@Shadow @Final private StringReader reader;
	@Shadow private Either<Holder<Item>, HolderSet<Item>> result;
	@Shadow @Nullable private CompoundTag nbt;
	@Shadow private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;

	@Inject(at = @At(value = "HEAD"), method = "readNbt", cancellable = true)
	protected void atReadNbt(CallbackInfo ci) throws CommandSyntaxException
	{
		ci.cancel();
		int cursorPos = reader.getCursor();

		try
		{
			nbt = (new TagParser(reader)).readStruct();
		}
		catch (CommandSyntaxException exception)
		{
			reader.setCursor(cursorPos);
			suggestions = this::suggestNbt;
			throw exception;
		}
	}

	@Unique private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder suggestionsBuilder)
	{
		Holder<Item> itemHolder = result.left().orElse(null);
		if (itemHolder == null) { return Suggestions.empty(); }
		Item item = itemHolder.value();
		ResourceLocation resourceLocation = RegistryUtils.ITEM.getKey(item);

		String name = resourceLocation.toString();
		String tag = suggestionsBuilder.getRemaining();

		return NbtSuggestionManager.loadFromName("item/" + name, tag, suggestionsBuilder, false);
	}*/
}
