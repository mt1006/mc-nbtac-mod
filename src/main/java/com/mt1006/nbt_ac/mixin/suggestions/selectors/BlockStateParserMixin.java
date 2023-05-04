package com.mt1006.nbt_ac.mixin.suggestions.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(BlockStateParser.class)
public class BlockStateParserMixin
{
	@Shadow @Final private StringReader reader;
	@Shadow @Nullable private BlockState state;
	@Shadow @Nullable private CompoundTag nbt;
	@Shadow private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;

	@Inject(method = "readNbt", at = @At(value = "HEAD"), cancellable = true)
	protected void atReadNbt(CallbackInfo callbackInfo) throws CommandSyntaxException
	{
		callbackInfo.cancel();
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

	private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder suggestionsBuilder)
	{
		if (state == null) { return Suggestions.empty(); }
		ResourceLocation resourceLocation = RegistryUtils.BLOCK.getKey(state.getBlock());

		String name = resourceLocation.toString();
		String tag = suggestionsBuilder.getRemaining();

		return NbtSuggestionManager.loadFromName("block/" + name, tag, suggestionsBuilder, false);
	}
}
