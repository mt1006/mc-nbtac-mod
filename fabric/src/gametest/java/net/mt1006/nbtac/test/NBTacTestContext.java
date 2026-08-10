package net.mt1006.nbtac.test;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.config.ConfigFields;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NBTacTestContext
{
	private final Field pendingSuggestionsField;
	private final Field suggestionsField;
	private final ChatScreen chatScreen;
	private final CommandSuggestions commandSuggestions;
	private final Method insertTextMethod;

	public NBTacTestContext(ChatScreen chatScreen)
	{
		this.pendingSuggestionsField = getField(CommandSuggestions.class, "pendingSuggestions", CompletableFuture.class);
		this.suggestionsField = getField(Suggestions.class, "suggestions", List.class);
		this.chatScreen = chatScreen;

		Field commandSuggestionsField = getField(ChatScreen.class, "commandSuggestions", CommandSuggestions.class);
		this.commandSuggestions = (CommandSuggestions)getFieldVal(commandSuggestionsField, chatScreen);

		try
		{
			insertTextMethod = ChatScreen.class.getDeclaredMethod("insertText", String.class, boolean.class);
			insertTextMethod.setAccessible(true);
		}
		catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}

	public void assertPresent(String command, String suggestion)
	{
		assertContains(command, suggestion, true);
	}

	public void assertAbsent(String command, String suggestion)
	{
		assertContains(command, suggestion, false);
	}

	public void assertContains(String command, String suggestion, boolean present)
	{
		insertText(chatScreen, command, true);
		Set<String> suggestionSet = getSuggestionSet();
		if (suggestionSet.contains(suggestion) != present)
		{
			NBTac.LOGGER.error("Expected: {}, Got: {}", suggestion, suggestionSet);
			throw new RuntimeException(command + " -> " + suggestion);
		}
	}

	public void assertOnlyContains(String command, String suggestion, boolean present)
	{
		insertText(chatScreen, command, true);
		Set<String> suggestionSet = getSuggestionSet();
		if (suggestionSet.size() != (present ? 1 : 0) || suggestionSet.contains(suggestion) != present)
		{
			NBTac.LOGGER.error("Expected: {}, Got: {}", suggestion, suggestionSet);
			throw new RuntimeException(command + " -> " + suggestion);
		}
	}

	public void assertAllPresent(String command, List<String> suggestion)
	{
		insertText(chatScreen, command, true);
		Set<String> suggestionSet = getSuggestionSet();
		if (suggestionSet.size() != suggestion.size() || !suggestionSet.containsAll(suggestion))
		{
			NBTac.LOGGER.error("Expected: {}, Got: {}", suggestion, suggestionSet);
			throw new RuntimeException(command + " -> " + suggestion);
		}
	}

	public <T> void withConfig(ConfigFields.Field<T> field, T newVal, Runnable assertion)
	{
		T oldVal = field.val;
		field.val = newVal;
		assertion.run();
		field.val = oldVal;
	}

	private Set<String> getSuggestionSet()
	{
		Suggestions suggestions = ((CompletableFuture<Suggestions>)getFieldVal(pendingSuggestionsField, commandSuggestions)).join();
		List<Suggestion> suggestionList = (List<Suggestion>)getFieldVal(suggestionsField, suggestions);

		Set<String> suggestionSet = new HashSet<>();
		for (Suggestion s : suggestionList)
		{
			if (!suggestionSet.add(s.getText())) { throw new RuntimeException("Found duplicate suggestions!"); }
		}
		return suggestionSet;
	}

	private static Field getField(Class<?> ofClass, String fieldName, Class<?> fieldType)
	{
		try
		{
			Field field = ofClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			if (field.getType() != fieldType) { throw new RuntimeException(); }

			return field;
		}
		catch (NoSuchFieldException e) { throw new RuntimeException(e); }
	}

	private static Object getFieldVal(Field field, Object obj)
	{
		try
		{
			return Objects.requireNonNull(field.get(obj));
		}
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
	}

	private void insertText(ChatScreen chatScreen, String command, boolean replace)
	{
		try
		{
			insertTextMethod.invoke(chatScreen, command, replace);
		}
		catch (IllegalAccessException | InvocationTargetException e) { throw new RuntimeException(e); }
	}
}
