package net.mt1006.nbtac.api;

import java.util.List;

/**
 * List of suggestions to show at a given cursor position
 * @param suggestions mutable list of suggestions
 * @param cursor position of suggestions cursor
 */
public record NBTacSuggestionList(List<NBTacSuggestion> suggestions, int cursor) {}
