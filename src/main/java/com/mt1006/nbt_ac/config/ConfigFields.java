package com.mt1006.nbt_ac.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.config.gui.ModOptionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigFields
{
	private static @Nullable Map<String, String> defaultLanguageKeys = null;
	private final File file;
	private final List<Field<?>> fields = new ArrayList<>();
	private final Map<String, Field<?>> fieldMap = new HashMap<>();
	private final Set<Field<?>> fieldSet = new HashSet<>();

	public ConfigFields(String filename)
	{
		this.file = new File(Minecraft.getInstance().gameDirectory, "config/" + filename);
	}

	public IntegerField add(String name, int val)
	{
		IntegerField field = new IntegerField(name, val);
		addField(field, name);
		return field;
	}

	public BooleanField add(String name, boolean val)
	{
		BooleanField field = new BooleanField(name, val);
		addField(field, name);
		return field;
	}

	private void addField(Field<?> field, String name)
	{
		fields.add(field);
		if (fieldMap.put(name, field) != null) { throw new RuntimeException("Duplicate field names!"); };
		if (!fieldSet.add(field)) { throw new RuntimeException("Duplicate fields!"); }
	}

	public void save()
	{
		file.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file))))
		{
			fields.forEach((field) -> field.save(writer));
		}
		catch (IOException ignore) {}
	}

	public void load()
	{
		int loadedCount = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty()) { continue; }
				if (line.charAt(0) == '#') { continue; }
				if (StringUtils.isBlank(line)) { continue; }

				int equalSignPos = line.indexOf('=');
				if (equalSignPos == -1) { throw new IOException(); }

				String name = line.substring(0, equalSignPos).trim();
				String value = line.substring(equalSignPos + 1).trim();

				Field<?> field = fieldMap.get(name);
				if (field == null) { throw new IOException(); }

				field.load(value);
				loadedCount++;
			}
		}
		catch (IOException exception) { save(); }

		if (loadedCount != fields.size()) { save(); }
	}

	public void reset()
	{
		fields.forEach(Field::reset);
	}

	private static void loadDefaultLanguageKeys()
	{
		defaultLanguageKeys = new HashMap<>();

		try (InputStream stream = NBTac.class.getResourceAsStream(String.format("/assets/%s/lang/en_us.json", NBTac.MOD_ID)))
		{
			if (stream == null) { return; }

			JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
			Pattern replacePattern = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

			for(Map.Entry<String, JsonElement> entry : json.entrySet())
			{
				String str = replacePattern.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
				defaultLanguageKeys.put(entry.getKey(), str);
			}
		}
		catch (JsonParseException | IOException ioexception)
		{
			NBTac.LOGGER.error("Failed to load default language keys!");
		}
	}

	public abstract static class Field<T>
	{
		private static final String NAME_KEY_PREFIX = "nbt_ac.options.field.";
		private static final String DESC_KEY_SUFFIX = ".desc";
		private static final String DESC_ERROR = "[failed to load description]";
		public final String name;
		private final T defVal;
		public volatile T val;

		protected Field(String name, T val)
		{
			this.name = name;
			this.val = val;
			this.defVal = val;
		}

		protected void save(PrintWriter writer)
		{
			String description = String.format("%s\nDefault value: %s", getDefaultDescription(), this);
			BufferedReader reader = new BufferedReader(new StringReader(description));
			reader.lines().forEach((line) -> writer.println("# " + line));

			writer.printf("%s = %s\n\n", name, this);
		}

		protected void load(String str) throws IOException
		{
			try { fromString(str); }
			catch (NumberFormatException exception) { throw new IOException(); }
		}

		public void reset()
		{
			val = defVal;
		}

		@Override public String toString()
		{
			return val.toString();
		}

		public Component getWidgetName()
		{
			return new TranslatableComponent(NAME_KEY_PREFIX + name);
		}

		public String getWidgetNameKey()
		{
			return NAME_KEY_PREFIX + name;
		}

		public Component getWidgetTooltip()
		{
			return new TranslatableComponent("nbt_ac.options.common.tooltip",
					new TranslatableComponent(getDescriptionKey()), defVal.toString());
		}

		private String getDefaultDescription()
		{
			if (defaultLanguageKeys == null) { loadDefaultLanguageKeys(); }
			return defaultLanguageKeys.getOrDefault(getDescriptionKey(), DESC_ERROR);
		}

		private String getDescriptionKey()
		{
			return NAME_KEY_PREFIX + name + DESC_KEY_SUFFIX;
		}

		abstract void fromString(String str);
	}

	public static class IntegerField extends Field<Integer>
	{
		public IntegerField(String name, Integer val)
		{
			super(name, val);
		}

		@Override public void fromString(String str)
		{
			val = Integer.valueOf(str);
		}

		public AbstractWidget createSwitch(List<Integer> options)
		{
			return new ModOptionList.IntegerSwitch(this, options);
		}

		public AbstractWidget createSlider(int min, int max, int multiplier, @Nullable List<Integer> specialValues)
		{
			return new ModOptionList.IntegerSlider(this, min, max, multiplier, specialValues);
		}
	}

	public static class BooleanField extends Field<Boolean>
	{
		public BooleanField(String name, Boolean val)
		{
			super(name, val);
		}

		@Override public void fromString(String str)
		{
			val = Boolean.valueOf(str);
		}

		public AbstractWidget createSwitch()
		{
			return new ModOptionList.BooleanSwitch(this);
		}
	}
}
