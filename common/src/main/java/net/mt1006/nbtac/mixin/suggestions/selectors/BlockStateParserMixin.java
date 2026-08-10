package net.mt1006.nbtac.mixin.suggestions.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
	protected void atReadNbt(CallbackInfo ci) throws Exception
	{
		ci.cancel();
		int cursorPos = reader.getCursor();

		try
		{
			nbt = TagParser.parseCompoundAsArgument(reader);
		}
		catch (Exception e)
		{
			reader.setCursor(cursorPos);
			suggestions = this::suggestNbt;
			throw e;
		}
	}

	@Unique private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder builder)
	{
		if (state == null) { return Suggestions.empty(); }
		ResourceLocation id = RegistryUtils.BLOCK.getKey(state.getBlock());
		if (id == null) { return Suggestions.empty(); }

		String str = builder.getRemaining();
		String name = id.toString();
		return SuggestionManager.loadFromName(str, "block/" + name, builder, false);
	}
}
