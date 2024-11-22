package com.mt1006.nbt_ac;

import java.util.Set;

public interface NBTacLoaderInterface
{
	void appendModVersionIds(Set<String> mods);
	boolean isModPresent(String id);
	boolean isFabric();
}
