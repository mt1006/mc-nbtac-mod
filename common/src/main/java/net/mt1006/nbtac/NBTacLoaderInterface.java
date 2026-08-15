package net.mt1006.nbtac;

import java.util.Set;

public interface NBTacLoaderInterface
{
	void appendModVersionIds(Set<String> mods);
	boolean isModPresent(String id);
	boolean isFabric();
}
