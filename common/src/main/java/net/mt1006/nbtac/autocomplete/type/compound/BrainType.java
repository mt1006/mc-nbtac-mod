package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public class BrainType extends ComplexCompoundType
{
	private final @Nullable Identifier entityId;

	public BrainType(@Nullable String entityId)
	{
		this.entityId = entityId != null ? Identifier.tryParse(entityId) : null;
	}

	@Override protected void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map)
	{
		if (entityId == null || !entityId.getNamespace().equals("minecraft")) { return; }
		map.addAll(NbtTagManager.get("_entity/minecraft:_brain/" + entityId.getPath()));
	}
}
