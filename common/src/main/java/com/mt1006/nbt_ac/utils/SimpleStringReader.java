package com.mt1006.nbt_ac.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleStringReader
{
	private final String str;
	private final int len;
	private final char[] chars;
	private int pos = 0;

	public SimpleStringReader(String str)
	{
		this.str = str;
		this.len = str.length();
		this.chars = str.toCharArray();
	}

	public String readString()
	{
		if (pos == len) { throw new ReaderException(); }

		int startPos = pos;
		while (true)
		{
			char ch = (pos != len) ? chars[pos] : '\0';

			if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')
				&& ch != '_' && ch != '-' && ch != ':' && ch != '/' && ch != '$')
			{
				if (startPos == pos) { throw new ReaderException(); }
				return str.substring(startPos, pos);
			}
			pos++;
		}
	}

	public int readTabs()
	{
		int startPos = pos;
		while (pos != len && chars[pos] == '\t')
		{
			pos++;
		}
		return pos - startPos;
	}


	public <T> List<T> parseList(Function<SimpleStringReader, T> parser, char endSymbol)
	{
		List<T> list = new ArrayList<>();
		while (true)
		{
			list.add(parser.apply(this));
			if (peek() == endSymbol)
			{
				skipChar();
				break;
			}
			else if (peek() == ',')
			{
				skipChar();
				expect(' ');
			}
		}
		return list;
	}

	public void expect(char ch)
	{
		if (pos == len || chars[pos++] != ch) { throw new ReaderException(); }
	}

	public void skipChar()
	{
		if (pos == len) { throw new ReaderException(); }
		pos++;
	}

	public char peek()
	{
		return (pos != len) ? chars[pos] : '\0';
	}

	public void expectEnd()
	{
		if (pos != len) { throw new ReaderException(); }
	}

	public void throwException()
	{
		throw new ReaderException();
	}

	private class ReaderException extends RuntimeException
	{
		public ReaderException()
		{
			super("Error parsing \"" + str + "\" at " + pos);
		}
	}
}
