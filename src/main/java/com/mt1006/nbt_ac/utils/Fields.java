package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	public static Field suggestionsListList = null;
	public static Field commandContextArguments = null;
	public static List<Field> suggestionsBuilderStrings = null;
	public static @Nullable Pair<String, Class<?>[]> itemUseOnMethodData = null;
	public static @Nullable Pair<String, Class<?>[]> itemAppendHoverTextMethodData = null;

	public static void init()
	{
		suggestionsBuilderList = getField(SuggestionsBuilder.class, List.class);
		suggestionsBuilderInt = getField(SuggestionsBuilder.class, int.class);
		suggestionsListRect = getField(CommandSuggestions.SuggestionsList.class, Rect2i.class);
		suggestionsListList = getField(CommandSuggestions.SuggestionsList.class, List.class);
		commandContextArguments = getField(CommandContext.class, Map.class);

		suggestionsBuilderStrings = getFields(SuggestionsBuilder.class, String.class);

		itemUseOnMethodData = findMethodName(Item.class, false, InteractionResult.class, new Class[]{UseOnContext.class});
		itemAppendHoverTextMethodData = findMethodName(Item.class, false, void.class,
				new Class[]{ItemStack.class, Item.TooltipContext.class, List.class, TooltipFlag.class});
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

		fields.removeIf((field) -> !fieldType.isAssignableFrom(field.getType()));
		fields.removeIf((field) -> Modifier.isStatic(field.getModifiers()));

		fields.forEach((field) -> field.setAccessible(true));
		return fields;
	}

	private static @Nullable Pair<String, Class<?>[]> findMethodName(Class<?> declaringClass, boolean isStatic,
																	 Class<?> returnType, Class<?>[] arguments)
	{
		Method[] methods = declaringClass.getDeclaredMethods();

		for (Method method : methods)
		{
			int modifiers = method.getModifiers();
			if (Modifier.isStatic(modifiers) != isStatic) { continue; }
			if (method.getReturnType() != returnType) { continue; }
			if (method.getParameterCount() != arguments.length) { continue; }

			Class<?>[] declaredArguments = method.getParameterTypes();
			for (int i = 0; i < arguments.length; i++)
			{
				if (declaredArguments[i] != arguments[i]) { return null; }
			}
			return Pair.of(method.getName(), arguments);
		}
		return null;
	}

	public static boolean isMethodOverridden(@Nullable Pair<String, Class<?>[]> data, Object obj, Class<?> superClass)
	{
		try
		{
			return data != null && obj.getClass().getMethod(data.getLeft(), data.getRight()).getDeclaringClass() != superClass;
		}
		catch (Exception ignore) {}
		return false;
	}

	public static <T> List<T> getStaticFields(Class<?> fromClass, Class<T> ofClass)
	{
		try
		{
			List<T> list = new ArrayList<>();

			for (Field field : fromClass.getDeclaredFields())
			{
				if (!Modifier.isStatic(field.getModifiers())) { continue; }
				if (!Modifier.isPublic(field.getModifiers())) { continue; }

				Object obj = field.get(null);
				if (ofClass.isAssignableFrom(obj.getClass())) { list.add((T)obj); }
			}
			return list;
		}
		catch (Exception exception) { return List.of(); }
	}
}
