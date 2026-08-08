package net.mt1006.nbtac.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.mt1006.nbtac.config.ModConfig;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NBTacTest implements FabricClientGameTest
{
	private static final int MCVER = 260200;

	@Override public void runTest(ClientGameTestContext context)
	{
		TestSingleplayerContext ctx = context.worldBuilder().adjustSettings((s) -> s.setAllowCommands(true)).create();
		context.runOnClient(NBTacTest::suggestionTest);
		ctx.close();
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

		// test config options
		ctx.withConfig(ModConfig.shortBoolean, true, () -> ctx.assertAllPresent("/summon zombie ~ ~ ~ {CanBreakDoors:", List.of("1b", "0b")));

		ctx.assertPresent("/particle block{", "block_state");
		ctx.assertContains("/setblock ~ ~ ~ minecraft:test_instance_block{", "errors", MCVER >= 12109);
		ctx.assertOnlyContains("/setblock ~ ~ ~ test_instance_block{errors:[{pos:", "[I;", MCVER >= 12109);
	}
}
