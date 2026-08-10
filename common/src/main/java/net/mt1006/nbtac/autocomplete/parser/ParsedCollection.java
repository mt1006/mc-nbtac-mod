package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ParsedCollection<T> extends ParsedValue
{
	protected final List<T> list = new ArrayList<>();
	private boolean closed = false;
	private int lastPos = 0;

	public ParsedCollection(@Nullable ParsedTag parent, int pos)
	{
		super(parent, pos);
	}

	public <G extends T> G add(G val, int cursorPos)
	{
		list.add(val);
		lastPos = cursorPos;
		return val;
	}

	public Iterable<T> getAll()
	{
		return list;
	}

	public T getLast()
	{
		return list.getLast();
	}

	public int getLastPos()
	{
		return lastPos;
	}

	public void close()
	{
		closed = true;
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public boolean isClosed()
	{
		return closed;
	}
}
