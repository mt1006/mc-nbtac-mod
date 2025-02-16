package com.mt1006.nbt_ac.config.gui;

import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GeneratorScreen extends Screen
{
	private final Screen lastScreen;
	private Button actionButton;
	private State state;
	public volatile boolean generated = false;

	public GeneratorScreen(Screen lastScreen, Component title)
	{
		super(title);
		this.lastScreen = lastScreen;
		state = State.NOT_GENERATED;
	}

	@Override protected void init()
	{
		Button goBackButton = Button.builder(CommonComponents.GUI_BACK,
				(b) -> onGoBackPress(lastScreen)).pos(width / 2 - 155, height - 27).size(150, 20).build();
		actionButton = Button.builder(Component.translatable("nbt_ac.options.generate_suggestions.start_generating"),
				(b) -> onActionPress()).pos(width / 2 + 5, height - 27).size(150, 20).build();

		MultiLineTextWidget text = new MultiLineTextWidget(20, 40,
				Component.translatable("nbt_ac.options.generate_suggestions.description"), font);
		text.setMaxWidth(width - 40);

		addRenderableWidget(goBackButton);
		addRenderableWidget(actionButton);
		addRenderableWidget(text);
	}

	private static void onGoBackPress(Screen lastScreen)
	{
		Minecraft.getInstance().setScreen(lastScreen);
	}

	private void onActionPress()
	{
		switch (state)
		{
			case NOT_GENERATED:
				state = State.GENERATING;
				actionButton.active = false;
				actionButton.setMessage(Component.translatable("nbt_ac.options.generate_suggestions.generating"));
				generated = false;
				new Thread(() -> Loader.generate(this)).start();
				return;

			case EXIT_RECOMMENDED:
				Minecraft.getInstance().stop();
				return;

			case GENERATED:

				return;
		}
		throw new RuntimeException("Button pressed with unknown state!");
	}

	@Override public void tick()
	{
		if (generated)
		{
			state = State.EXIT_RECOMMENDED;
			actionButton.active = true;
			actionButton.setMessage(Component.translatable("menu.quit"));

			generated = false;
		}
	}

	@Override public void onClose()
	{
		Minecraft.getInstance().setScreen(lastScreen);
	}

	@Override public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215);
	}

	private enum State
	{
		NOT_GENERATED, GENERATED, GENERATING, EXIT_RECOMMENDED
	}
}
