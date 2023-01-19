package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import java.util.Stack;

public class CodeStack<E> extends Stack<E>
{
	@Override
	public E pop()
	{
		try { return super.pop(); }
		catch (Exception exception) { return null; }
	}

	public void popx(int x)
	{
		for (int i = 0; i < x; i++) { pop(); }
	}

	public void poppush(int a, int b)
	{
		for (int i = 0; i < a; i++) { pop(); }
		for (int i = 0; i < b; i++) { push(null); }
	}
}
