package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Fields
{
	public static Field suggestionsBuilderList = null;
	public static Field suggestionsBuilderInt = null;
	public static Field suggestionsListRect = null;
	public static Field commandContextArguments = null;
	public static List<Field> suggestionsBuilderStrings = null;

	public static void init()
	{
		suggestionsBuilderList = getField(SuggestionsBuilder.class, List.class);
		suggestionsBuilderInt = getField(SuggestionsBuilder.class, int.class);
		suggestionsListRect = getField(CommandSuggestions.SuggestionsList.class, Rect2i.class);
		commandContextArguments = getField(CommandContext.class, Map.class);

		suggestionsBuilderStrings = getFields(SuggestionsBuilder.class, String.class);
	}

	private static Field getField(Class<?> declaringClass, Class<?> fieldType)
	{
		Field[] fields = declaringClass.getDeclaredFields();

		for (Field field : fields)
		{
			if (Modifier.isStatic(field.getModifiers())) { continue; }
			if (fieldType.isAssignableFrom(field.getType()))
			{
				field.setAccessible(true);
				return field;
			}
		}

		return null;
	}

	private static List<Field> getFields(Class<?> declaringClass, Class<?> fieldType)
	{
		List<Field> fields = new ArrayList<>(Arrays.asList(declaringClass.getDeclaredFields()));

		fields.removeIf(field -> !fieldType.isAssignableFrom(field.getType()));
		fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));

		fields.forEach(field -> field.setAccessible(true));
		return fields;
	}
}
