package net.mt1006.nbtac.autocomplete;

public enum DataSource
{
	SYNTHETIC(0), // priority shouldn't matter for them
	BUILTIN(1),
	API(2),
	USER_DEFINED(3);

	public final int priority;

	DataSource(int priority)
	{
		this.priority = priority;
	}
}
