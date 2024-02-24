package com.mt1006.nbt_ac.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mt1006.nbt_ac.config.ConfigFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModOptionList extends ContainerObjectSelectionList<ModOptionList.ListWidget>
{
	protected static final int ELEMENT_WIDTH = 310;
	protected static final int ELEMENT_HEIGHT = 20;
	private final Font font;
	private final List<MutableWidget> mutableWidgets = new ArrayList<>();

	public ModOptionList(Minecraft minecraft, int w, int h, int yTop, int yBottom, int itemHeight, Font font)
	{
		super(minecraft, w, h, yTop, yBottom, itemHeight);
		this.font = font;
	}

	public void addLabel(String key)
	{
		addWidget(new Label(width, 9, new TranslatableComponent("nbt_ac.options." + key), font));
	}

	public void add(AbstractWidget widget)
	{
		widget.x = width / 2 - 155;
		addWidget(widget);
	}

	private void addWidget(AbstractWidget widget)
	{
		addEntry(new ListWidget(widget));
		if (widget instanceof MutableWidget) { mutableWidgets.add((MutableWidget)widget); }
	}

	public void updateValues()
	{
		mutableWidgets.forEach(MutableWidget::update);
	}

	@Override public int getRowWidth()
	{
		return 400;
	}

	@Override protected int getScrollbarPosition()
	{
		return super.getScrollbarPosition() + 32;
	}

	public static class ListWidget extends Entry<ListWidget>
	{
		public final AbstractWidget widget;

		private ListWidget(AbstractWidget widget)
		{
			this.widget = widget;
		}

		@Override public @NotNull List<? extends GuiEventListener> children()
		{
			return List.of(widget);
		}

		@Override public @NotNull List<? extends NarratableEntry> narratables()
		{
			return List.of(widget);
		}

		@Override public void render(@NotNull PoseStack guiGraphics, int a, int b, int c, int d, int e,
									 int mouseX, int mouseY, boolean h, float i)
		{
			widget.y = b;
			widget.render(guiGraphics, mouseX, mouseY, i);
		}
	}

	private static class Label extends AbstractWidget
	{
		private final Font font;
		private final int yOffset;

		public Label(int w, int yOffset, Component component, Font font)
		{
			super(0, 0, w, 9, component);
			this.font = font;
			this.yOffset = yOffset;
		}

		@Override public void render(PoseStack guiGraphics, int i, int j, float f)
		{
			int textY = y + yOffset + (height - font.lineHeight) / 2;
			drawCenteredString(guiGraphics, font, getMessage(), width / 2, textY, 0xFFFFFF);
		}

		@Override public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	public static class BooleanSwitch extends AbstractButton implements MutableWidget
	{
		private final ConfigFields.BooleanField field;
		private final Component component;
		public final List<FormattedCharSequence> tooltip;

		public BooleanSwitch(ConfigFields.BooleanField field)
		{
			super(0, 0, ELEMENT_WIDTH, ELEMENT_HEIGHT, new TextComponent(""));
			this.field = field;
			this.component = field.getWidgetName();

			updateText();
			tooltip = Minecraft.getInstance().font.split(field.getWidgetTooltip(), 200);
		}

		public void updateText()
		{
			Component val = field.val ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
			setMessage(component.copy().append(": ").append(val));
		}

		@Override public void onPress()
		{
			this.field.val = !this.field.val;
			updateText();
		}

		@Override public void updateNarration(@NotNull NarrationElementOutput narrationOutput)
		{
			defaultButtonNarrationText(narrationOutput);
		}

		@Override public void update()
		{
			updateText();
		}
	}

	public static class IntegerSwitch extends AbstractButton implements MutableWidget
	{
		private static final Component UNDEFINED_OPTION = new TranslatableComponent("nbt_ac.options.common.undefined");
		private final ConfigFields.IntegerField field;
		private final String key;
		private final List<Integer> options;
		private final Component component;
		public final List<FormattedCharSequence> tooltip;

		public IntegerSwitch(ConfigFields.IntegerField field, List<Integer> options)
		{
			super(0, 0, ELEMENT_WIDTH, ELEMENT_HEIGHT, new TextComponent(""));
			this.field = field;
			this.key = field.getWidgetNameKey();
			this.options = options;
			this.component = new TranslatableComponent(key);

			updateText();
			tooltip = Minecraft.getInstance().font.split(field.getWidgetTooltip(), 200);
		}

		public void updateText()
		{
			Component optionComponent = options.contains(field.val)
					? new TranslatableComponent(String.format("%s.%d", key, field.val))
					: UNDEFINED_OPTION;

			setMessage(component.copy().append(": ").append(optionComponent));
		}

		@Override public void onPress()
		{
			int pos = options.indexOf(field.val);
			int newIndex = (pos != options.size() - 1) ? (pos + 1) : 0;
			field.val = options.get(newIndex);
			updateText();
		}

		@Override public void updateNarration(@NotNull NarrationElementOutput narrationOutput)
		{
			defaultButtonNarrationText(narrationOutput);
		}

		@Override public void update()
		{
			updateText();
		}
	}

	public static abstract class AbstractSlider<T, V extends Comparable<V>> extends AbstractSliderButton implements MutableWidget
	{
		protected final ConfigFields.Field<T> field;
		protected final Component component;
		protected final V min, max;
		protected final Supplier<String> widgetNameKey;
		public final List<FormattedCharSequence> tooltip;

		public AbstractSlider(ConfigFields.Field<T> field, V min, V max)
		{
			super(0, 0, ELEMENT_WIDTH, ELEMENT_HEIGHT, new TextComponent(""), 0.0);
			this.field = field;
			this.component = field.getWidgetName();
			this.min = min;
			this.max = max;
			this.widgetNameKey = field::getWidgetNameKey;

			if (min.compareTo(max) > 0) { throw new RuntimeException("Slider - min bigger than max!"); }
			tooltip = Minecraft.getInstance().font.split(field.getWidgetTooltip(), 200);
		}

		@Override public void update()
		{
			updateSliderValue();
			updateMessage();
		}

		abstract protected void updateSliderValue();
	}

	public static class IntegerSlider extends AbstractSlider<Integer, Integer>
	{
		private final int multiplier;
		private final @Nullable List<Integer> specialValues;

		public IntegerSlider(ConfigFields.IntegerField field, int min, int max, int multiplier, @Nullable List<Integer> specialValues)
		{
			super(field, min, max);
			this.multiplier = multiplier;
			this.specialValues = specialValues;
			update();
		}

		@Override protected void updateSliderValue()
		{
			float diff = max - min;
			int valPos = field.val / multiplier;
			value = (valPos < max) ? Mth.clamp(((float)valPos - (float)min) / diff, 0.0f, 1.0f) : 1.0;
		}

		@Override protected void applyValue()
		{
			int steps = max - min + 1;
			int pos = Math.min((int)(value * (double)steps), steps - 1);
			field.val = (min + pos) * multiplier;
		}

		@Override protected void updateMessage()
		{
			int valPos = field.val / multiplier;
			Component subcomponent = (specialValues != null && specialValues.contains(valPos))
					? new TranslatableComponent(String.format("%s.%d", widgetNameKey.get(), valPos))
					: new TextComponent(Integer.toString(field.val));

			setMessage(component.copy().append(": ").append(subcomponent));
		}

		@Override public void update()
		{
			updateSliderValue();
			updateMessage();
		}
	}

	public interface MutableWidget
	{
		void update();
	}
}
