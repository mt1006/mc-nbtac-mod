package com.mt1006.nbt_ac.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
		list = new ModOptionList(Minecraft.getInstance(), width, height, 32, height - 32, 25, font);

		doneButton = new Button(width / 2 - 155, height - 27, 150, 20,
				CommonComponents.GUI_DONE, (b) -> onDonePress(lastScreen));
		resetButton = new Button(width / 2 + 5, height - 27, 150, 20,
				Component.translatable("nbt_ac.options.common.reset_settings"), (b) -> onResetPress(list));

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
		Minecraft.getInstance().setScreen(this.lastScreen);
	}

	@Override public void render(@NotNull PoseStack guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		list.render(guiGraphics, mouseX, mouseY, partialTick);
		doneButton.render(guiGraphics, mouseX, mouseY, partialTick);
		resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
		drawCenteredString(guiGraphics, font, title, width / 2, 20, 16777215);

		for (ModOptionList.ListWidget listWidget : list.children())
		{
			AbstractWidget widget = listWidget.widget;
			if (!widget.isMouseOver(mouseX, mouseY)) { continue; }

			List<FormattedCharSequence> tooltip;
			if (widget instanceof ModOptionList.BooleanSwitch) { tooltip = ((ModOptionList.BooleanSwitch)widget).tooltip; }
			else if (widget instanceof ModOptionList.IntegerSwitch) { tooltip = ((ModOptionList.IntegerSwitch)widget).tooltip; }
			else if (widget instanceof ModOptionList.AbstractSlider<?,?>) { tooltip = ((ModOptionList.AbstractSlider<?,?>)widget).tooltip; }
			else { break; }

			renderTooltip(guiGraphics, tooltip, mouseX, mouseY);
			break;
		}
	}
}