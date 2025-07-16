package net.mt1006.nbtac.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.config.gui.ModOptionList;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigFields
{
	private static @Nullable Map<String, String> defaultLanguageKeys = null;
	private final File file;
	private final Map<String, Field<?>> fieldMap = new HashMap<>();

	public ConfigFields(String filename)
	{
		this.file = new File(Minecraft.getInstance().gameDirectory, "config/" + filename);
	}

	public IntegerField add(String name, int val)
	{
		return addField(name, new IntegerField(name, val));
	}

	public BooleanField add(String name, boolean val)
	{
		return addField(name, new BooleanField(name, val));
	}

	public <T extends Enum<T>> EnumField<T> add(String name, T val)
	{
		return addField(name, new EnumField<>(name, val));
	}

	private <T extends Field<?>> T addField(String name, T field)
	{
		if (fieldMap.put(name, field) != null) { throw new RuntimeException("Duplicate field names!"); }
		return field;
	}

	public void save()
	{
		file.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file))))
		{
			fieldMap.values().forEach((f) -> f.save(writer));
		}
		catch (IOException ignore) {}
	}

	public void load()
	{
		int loadedCount = 0;
		boolean rewrite = false;

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
				if (field == null)
				{
					rewrite = true;
					continue;
				}

				field.load(value);
				loadedCount++;
			}
		}
		catch (IOException e) { save(); }

		if (loadedCount != fieldMap.size() || rewrite) { save(); }
	}

	public void reset()
	{
		fieldMap.values().forEach(Field::reset);
	}

	private static void loadDefaultLanguageKeys()
	{
		defaultLanguageKeys = new HashMap<>();

		try (InputStream stream = NBTac.class.getResourceAsStream(String.format("/assets/%s/lang/en_us.json", NBTac.MOD_ID)))
		{
			if (stream == null) { return; }

			JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
			Pattern replacePattern = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

			for (Map.Entry<String, JsonElement> entry : json.entrySet())
			{
				String str = replacePattern.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
				defaultLanguageKeys.put(entry.getKey(), str);
			}
		}
		catch (JsonParseException | IOException e)
		{
			NBTac.LOGGER.error("Failed to load default language keys!");
		}
	}

	public abstract static class Field<T>
	{
		private static final String NAME_KEY_PREFIX = "nbtac.options.field.";
		private static final String DESC_KEY_SUFFIX = ".desc";
		private static final String DESC_ERROR = "[failed to load description]";
		public final String name;
		private final T defVal;
		public T val;

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
			catch (NumberFormatException e) { throw new IOException(); }
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
			return Component.translatable(NAME_KEY_PREFIX + name);
		}

		public String getWidgetNameKey()
		{
			return NAME_KEY_PREFIX + name;
		}

		public Component getWidgetTooltip()
		{
			return Component.translatable("nbtac.options.common.tooltip",
					Component.translatable(getDescriptionKey()), defVal.toString());
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
			return new ModOptionList.BooleanSwitch(this, false);
		}

		public AbstractWidget createDescribedSwitch()
		{
			return new ModOptionList.BooleanSwitch(this, true);
		}
	}

	public static class EnumField<T extends Enum<T>> extends Field<T>
	{
		private final Class<T> enumClass;
		private final T[] constants;

		public EnumField(String name, T val)
		{
			super(name, val);
			enumClass = (Class<T>)val.getClass();
			constants = enumClass.getEnumConstants();
		}

		@Override public void fromString(String str)
		{
			try { val = Enum.valueOf(enumClass, str.toUpperCase()); }
			catch (Exception e) { reset(); }
		}

		public AbstractWidget createSwitch()
		{
			return new ModOptionList.EnumSwitch<>(this, constants);
		}
	}
}
