package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

import java.util.Optional;

public class LootTableType extends ComplexType
{
	public static final LootTableType INSTANCE = new LootTableType();

	private LootTableType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		MinecraftServer singlePlayerServer = Minecraft.getInstance().getSingleplayerServer();
		if (singlePlayerServer == null) { return; }

		Optional<? extends HolderLookup.RegistryLookup<LootTable>> lootTableReg =
				singlePlayerServer.reloadableRegistries().lookup().lookup(Registries.LOOT_TABLE);
		if (lootTableReg.isEmpty()) { return; }

		for (ResourceKey<LootTable> id : lootTableReg.get().listElementIds().toList())
		{
			list.add(new IdSuggestion(id.location(), null, ctx.parserType()));
		}
	}
}
