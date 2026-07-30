package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import org.jetbrains.annotations.Nullable;

public class EntityCompoundType extends CompoundType
{
	public final @Nullable Identifier entityId; // or block entity

	public EntityCompoundType(@Nullable NbtTagMap tagMap, String entityId)
	{
		super(tagMap);
		this.entityId = Identifier.tryParse(entityId);
	}
}
