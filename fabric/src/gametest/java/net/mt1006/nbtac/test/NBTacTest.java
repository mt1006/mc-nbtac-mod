package net.mt1006.nbtac.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.mt1006.nbtac.api.NBTacAPI;
import net.mt1006.nbtac.api.NBTacSuggestion;
import net.mt1006.nbtac.api.NBTacSuggestionList;
import net.mt1006.nbtac.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class NBTacTest implements FabricClientGameTest
{
	private static final int MCVER = 260102;

	@Override public void runTest(ClientGameTestContext gameCtx)
	{
		TestSingleplayerContext singleplayer = gameCtx.worldBuilder().adjustSettings((s) -> s.setAllowCommands(true)).create();
		gameCtx.runOnClient(NBTacTest::suggestionTest);
		gameCtx.runOnClient(NBTacTest::apiTest);
		singleplayer.close();
	}

	@SuppressWarnings("ConstantValue")
	private static void suggestionTest(Minecraft mc)
	{
		ModConfig.reset();

		mc.openChatScreen(ChatComponent.ChatMethod.MESSAGE);
		if (!(mc.screen instanceof ChatScreen chatScreen)) { throw new RuntimeException(); }
		NBTacTestContext ctx = new NBTacTestContext(chatScreen);

		// basic functionality tests
		ctx.assertPresent("/summon zombie ~ ~ ~ {", "CanBreakDoors"); // basic /summon tag suggestions
		ctx.assertAbsent("/summon aaa:zombie ~ ~ ~ {", "CanBreakDoors"); // if non-mc version isn't confused with mc
		ctx.assertPresent("/summon zombie ~ ~ ~ {CanBreakDoors", ":"); // colon suggestions
		ctx.assertAllPresent("/summon zombie ~ ~ ~ {CanBreakDoors:true", List.of(",", "}")); // comma and '}' suggestions

		// tag value suggestion tests
		ctx.assertAllPresent("/summon zombie ~ ~ ~ {CanBreakDoors:", List.of("true", "false")); // boolean suggestions
		ctx.assertAllPresent("/summon zombie ~ ~ ~ {CanBreakDoors:tr", List.of("true")); // partially entered value suggestions
		ctx.assertPresent("/summon llama ~ ~ ~ {Variant:", "1"); // DescribedEnum (at least main part)

		// NBT path tests
		ctx.assertAllPresent("/data get entity @p Inventory", List.of("["));
		ctx.assertAllPresent("/data get entity @p Inventory[", List.of("{"));
		ctx.assertPresent("/data get entity @p Inventory[{", "id");
		ctx.assertPresent("/data get entity @p Inventory[{id:dirt,", "count");
		ctx.assertPresent("/data get entity @p Inventory[].", "count");
		ctx.assertPresent("/data get entity @p Inventory[5].", "count");
		ctx.assertPresent("/data get entity @p Inventory[5].co", "count");
		ctx.assertPresent("/data get entity @p Inventory[{id:\"minecraft:dirt\"}].components.", "\"minecraft:enchantments\"");

		// /data modify tests
		ctx.assertPresent("/data modify entity @p Inventory[1] set value {id:", "\"minecraft:dirt\"");
		ctx.assertAllPresent("/data modify entity @p Inventory[0] merge value {slot", List.of(":"));
		ctx.assertPresent("/data modify entity @p Inventory[1] merge from entity @p CustomName.", "keybind");

		// test ItemModel and item data component suggestions for Identifier.CODEC
		ctx.assertContains("/give @p minecraft:acacia_button[item_model=aca", "\"minecraft:acacia_boat\"", MCVER >= 12102);
		ctx.assertContains("/give @p minecraft:acacia_button[item_model=acacia_boat", "]", MCVER >= 12102);

		// test config options
		ctx.withConfig(ModConfig.shortBoolean, true, () -> ctx.assertAllPresent("/summon zombie ~ ~ ~ {CanBreakDoors:", List.of("1b", "0b")));

		ctx.assertPresent("/particle block{", "block_state");
		ctx.assertContains("/setblock ~ ~ ~ minecraft:test_instance_block{", "errors", MCVER >= 12109);
		ctx.assertOnlyContains("/setblock ~ ~ ~ test_instance_block{errors:[{pos:", "[I;", MCVER >= 12109);
	}

	private static void apiTest(Minecraft mc)
	{
		//TODO: test addCustomSuggestions

		// basic tests without processor
		NBTacTestContext.assertPresent(NBTacAPI.getNbtSuggestions("{", "entity/minecraft:zombie", null, false, null), "CanBreakDoors");
		NBTacTestContext.assertPresent(NBTacAPI.getNbtSuggestions("", "entity/minecraft:zombie", null, true, null), "CanBreakDoors");
		NBTacTestContext.assertPresent(NBTacAPI.getItemDataSuggestions("", "item/minecraft:intangible_projectile", null, null, null), "{}");

		// test processor that doesn't do anything
		NBTacTestContext.assertPresent(NBTacAPI.getNbtSuggestions("{", "entity/minecraft:zombie", null, false, Function.identity()), "CanBreakDoors");

		// test basic processor
		NBTacTestContext.assertPresent(NBTacAPI.getNbtSuggestions("{", "block/minecraft:furnace", null, false, (sl) -> {
			sl.suggestions().clear();
			sl.suggestions().add(NBTacSuggestion.createRaw("x", "y", -100));
			return sl;
		}), "x");

		// test processor returning new suggestion list instance
		NBTacTestContext.assertPresent(NBTacAPI.getNbtSuggestions("{", "block/minecraft:furnace", null, false, (sl) -> {
			NBTacSuggestionList newSl = new NBTacSuggestionList(new ArrayList<>(), sl.cursor());
			newSl.suggestions().add(NBTacSuggestion.createRaw("a", "b", 0));
			return newSl;
		}), "a");
	}
}
