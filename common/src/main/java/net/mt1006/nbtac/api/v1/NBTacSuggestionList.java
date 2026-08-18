package net.mt1006.nbtac.api.v1;

import java.util.List;

public record NBTacSuggestionList(List<NBTacSuggestion> suggestions, int cursor) {}
