package com.mt1006.nbt_ac.mixin.suggestions.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Mixin(ItemParser.class)
public class ItemParserMixin
{
    @Shadow @Final private StringReader reader;
    @Shadow private Item item;
    @Shadow @Nullable private CompoundNBT nbt;
    @Shadow private BiFunction<SuggestionsBuilder, ITagCollection<Item>, CompletableFuture<Suggestions>> suggestions;

    @Inject(at = @At(value = "HEAD"), method = "readNbt", cancellable = true)
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

    private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder suggestionsBuilder, ITagCollection<Item> collection)
    {
        ResourceLocation resourceLocation = RegistryUtils.ITEM.getKey(item);

        String name = resourceLocation.toString();
        String tag = suggestionsBuilder.getRemaining();

        return NbtSuggestionManager.loadFromName("item/" + name, tag, suggestionsBuilder, false);
    }
}
