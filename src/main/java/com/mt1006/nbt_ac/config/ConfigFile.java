package com.mt1006.nbt_ac.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigFile
{
	/*
		Accepted field types:
		-MutableInt
		-MutableByte
		-MutableShort
		-MutableLong
		-MutableBoolean
		-StringBuffer
	 */

	private final File file;
	private final List<Field> fields = new ArrayList<>();
	private final Map<String, Field> fieldMap = new HashMap<>();

	public ConfigFile(File file)
	{
		this.file = file;
	}

	public void addValue(String name, Object reference, String description)
	{
		Field newField = new Field(name, reference, description);
		fields.add(newField);
		fieldMap.put(name, newField);
	}

	public void save()
	{
		file.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file))))
		{
			for (Field field : fields)
			{
				field.saveDescription(writer);
				writer.printf("%s = %s\n\n", field.name, field.getValue());
			}
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
				if (line.length() == 0) { continue; }
				if (line.charAt(0) == '#') { continue; }
				if (StringUtils.isBlank(line)) { continue; }

				int equalSignPos = line.indexOf('=');
				if (equalSignPos == -1) { throw new IOException(); }

				String name = line.substring(0, equalSignPos).trim();
				String value = line.substring(equalSignPos + 1).trim();

				Field field = fieldMap.get(name);
				if (field == null) { throw new IOException(); }

				field.load(value);
				loadedCount++;
			}
		}
		catch (IOException exception) { save(); }

		if (loadedCount != fields.size()) { save(); }
	}

	private static class Field
	{
		String name;
		Object ref;
		String desc;

		public Field(String name, Object reference, String description)
		{
			this.name = name;
			this.ref = reference;
			this.desc = description + "\nDefault value: " + getValue();
		}

		public String getValue()
		{
			if (ref instanceof StringBuffer) { return "\"" + ref + "\""; }
			else { return ref.toString(); }
		}

		public void saveDescription(PrintWriter writer) throws IOException
		{
			if (desc != null)
			{
				BufferedReader reader = new BufferedReader(new StringReader(desc));

				String line;
				while ((line = reader.readLine()) != null)
				{
					writer.println("# " + line);
				}
			}
		}

		public void load(String value) throws IOException
		{
			try
			{
				if (ref instanceof MutableInt) { ((MutableInt)ref).setValue(Integer.parseInt(value)); }
				else if (ref instanceof MutableByte) { ((MutableByte)ref).setValue(Byte.parseByte(value)); }
				else if (ref instanceof MutableShort) { ((MutableShort)ref).setValue(Short.parseShort(value)); }
				else if (ref instanceof MutableLong) { ((MutableLong)ref).setValue(Long.parseLong(value)); }
				else if (ref instanceof MutableBoolean) { ((MutableBoolean)ref).setValue(Boolean.valueOf(value)); }
				else if (ref instanceof StringBuffer) { parseStringBuffer((StringBuffer)ref, value); }
			}
			catch (NumberFormatException exception) { throw new IOException(); }
		}

		private void parseStringBuffer(StringBuffer stringBuffer, String value) throws IOException
		{
			if (value.charAt(0) != '\"' || value.charAt(value.length() - 1) != '\"')
			{
				throw new IOException();
			}

			stringBuffer.setLength(0);
			stringBuffer.append(value.substring(1, value.length() - 1));
		}
	}
}
