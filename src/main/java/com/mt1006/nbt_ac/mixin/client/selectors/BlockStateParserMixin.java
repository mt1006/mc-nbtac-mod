package com.mt1006.nbt_ac.mixin.client.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Mixin(BlockStateParser.class)
public class BlockStateParserMixin
{
	@Shadow @Final private StringReader reader;
	@Shadow @Nullable private BlockState state;
	@Shadow @Nullable private CompoundNBT nbt;
	@Shadow private BiFunction<SuggestionsBuilder, ITagCollection<Block>, CompletableFuture<Suggestions>> suggestions;

	@Inject(method = "readNbt", at = @At(value = "HEAD"), cancellable = true)
	protected void atReadNbt(CallbackInfo callbackInfo) throws CommandSyntaxException
	{
		callbackInfo.cancel();
		int cursorPos = reader.getCursor();

		try
		{
			nbt = (new JsonToNBT(reader)).readStruct();
		}
		catch (CommandSyntaxException exception)
		{
			reader.setCursor(cursorPos);
			suggestions = this::suggestNbt;
			throw exception;
		}
	}

	private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder suggestionsBuilder, ITagCollection<Block> collection)
	{
		if (state == null) { return Suggestions.empty(); }
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(state.getBlock());

		String name = resourceLocation.toString();
		String tag = suggestionsBuilder.getRemaining();

		return NbtSuggestionManager.loadSuggestions(NbtSuggestionManager.get("block/" + name),
				"$tag/block/" + name, tag, suggestionsBuilder, false).buildFuture();
	}
}
