package net.mt1006.nbtac.config.enums;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

public enum UnknownItemComponents
{
	RELEVANT_BY_DEFAULT,
	RELEVANT_WITHIN_NAMESPACE,
	IRRELEVANT_BY_DEFAULT;

	public int getPriority(ResourceLocation componentId, @Nullable Item item)
	{
		switch (this)
		{
			case RELEVANT_BY_DEFAULT:
				return 0;

			case IRRELEVANT_BY_DEFAULT:
				return -1;

			case RELEVANT_WITHIN_NAMESPACE:
				ResourceLocation itemId = item != null ? RegistryUtils.ITEM.getKey(item) : null;
				return (itemId != null && componentId.getNamespace().equals(itemId.getNamespace())) ? 0 : -1;

			default:
				throw new IllegalStateException("Unexpected value: " + this);
		}
	}
}
