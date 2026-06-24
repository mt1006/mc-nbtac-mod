package com.mt1006.nbt_ac.config.gui;

import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen
{
	private final Screen lastScreen;
	private ModOptionList list;
	private Button doneButton, resetButton;

	public ConfigScreen(Screen lastScreen)
	{
		super(Component.translatable("nbt_ac.options"));
		this.lastScreen = lastScreen;
	}

	@Override public void init()
	{
		list = new ModOptionList(Minecraft.getInstance(), width, height - 64, 32, 25, font);

		doneButton = Button.builder(CommonComponents.GUI_DONE, (b) -> onDonePress(lastScreen))
				.pos(width / 2 - 155, height - 27).size(150, 20).build();
		resetButton = Button.builder(Component.translatable("nbt_ac.options.common.reset_settings"), (b) -> onResetPress(list))
				.pos(width / 2 + 5, height - 27).size(150, 20).build();

		ModConfig.initWidgets(list);

		addWidget(list);
		addWidget(doneButton);
		addWidget(resetButton);
	}

	private static void onDonePress(Screen lastScreen)
	{
		ModConfig.save();
		Minecraft.getInstance().setScreen(lastScreen);
	}

	private static void onResetPress(ModOptionList list)
	{
		ModConfig.reset();
		list.updateValues();
	}

	@Override public void onClose()
	{
		ModConfig.save();
		Minecraft.getInstance().setScreen(lastScreen);
	}

	@Override public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		list.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		doneButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		resetButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.centeredText(font, title, width / 2, 20, -1);
	}
}
