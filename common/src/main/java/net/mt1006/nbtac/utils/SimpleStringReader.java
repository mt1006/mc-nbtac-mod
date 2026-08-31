package net.mt1006.nbtac.utils;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

	public int getCursor()
	{
		return pos;
	}

	public String substring(int beginPos)
	{
		return str.substring(beginPos);
	}

	public String peekSubstring(int len)
	{
		return str.substring(pos, Math.min(this.len, pos + len));
	}

	public String readFileString()
	{
		if (pos == len) { throw new ReaderException(); }
		if (chars[pos] == '\"') { return readQuotedString('\"', true).str; }

		int startPos = pos;
		while (true)
		{
			char ch = (pos != len) ? chars[pos] : '\0';

			if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')
				&& ch != '_' && ch != '-' && ch != '.' && ch != ':' && ch != '/' && ch != '$' && ch != '!')
			{
				if (startPos == pos) { throw new ReaderException(); }
				return str.substring(startPos, pos);
			}
			pos++;
		}
	}

	public void readNbtString(Consumer<StringResults> setStr)
	{
		readNbtString(setStr, true);
	}

	public void readNbtPathString(Consumer<StringResults> setStr)
	{
		readNbtString(setStr, false);
	}

	// consumer setStr is used instead of return value, so that partial value is set even if exception is thrown
	private void readNbtString(Consumer<StringResults> setStr, boolean allowDot)
	{
		if (pos == len) { throw new ReaderException(); }

		char peek = chars[pos];
		if (peek == '\"' || peek == '\'')
		{
			StringResults results = readQuotedString(peek, false);
			setStr.accept(results);
			if (!results.closed) { throw new ExpectedCharException(pos, peek); }
			return;
		}

		int startPos = pos;
		while (true)
		{
			char ch = (pos != len) ? chars[pos] : '\0';

			if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')
					&& ch != '_' && ch != '-' && ch != '+' && (ch != '.' || !allowDot))
			{
				if (startPos == pos) { throw new ReaderException(); }
				setStr.accept(new StringResults(str.substring(startPos, pos), true));
				return;
			}
			pos++;
		}
	}

	private StringResults readQuotedString(char quote, boolean requireClosed)
	{
		expect(quote);
		boolean escaped = false;
		StringBuilder builder = new StringBuilder();
		while (true)
		{
			if (pos == len)
			{
				if (requireClosed) { throw new ReaderException(); }
				return new StringResults(builder.toString(), false);
			}

			char ch = chars[pos++];
			if (escaped)
			{
				if (ch == quote) { builder.append(quote); }
				else if (ch == '\\') { builder.append('\\'); }
				else { throw new ReaderException(); }
				escaped = false;
			}
			else
			{
				if (ch == quote) { return new StringResults(builder.toString(), true); }
				else if (ch == '\\') { escaped = true; }
				else { builder.append(ch); }
			}
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

	public <T> List<T> parseList(Function<SimpleStringReader, T> parser, char startSymbol, char endSymbol)
	{
		if (peek() != startSymbol) { return List.of(); }
		skipChar(); // skip startSymbol

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
		int atPos = pos;
		if (pos == len || chars[pos++] != ch) { throw new ExpectedCharException(atPos, ch); }
	}

	public boolean biExpect(char forTrue, char forFalse)
	{
		int atPos = pos;
		char ch = (pos != len) ? chars[pos++] : 0;

		if (ch == forTrue) { return true; }
		if (ch == forFalse) { return false; }
		else { throw new ExpectedCharException(atPos, forTrue, forFalse); }
	}

	public void skipChar()
	{
		if (pos == len) { throw new ReaderException(); }
		pos++;
	}
	
	public void skipSpaces()
	{
		while (pos != len && chars[pos] == ' ') { pos++; }
	}

	public char peek()
	{
		return (pos != len) ? chars[pos] : '\0';
	}

	public void expectEnd()
	{
		if (pos != len) { throw new ReaderException(); }
	}

	public static class StringResults
	{
		public final String str;
		public final boolean closed;

		private StringResults(String str, boolean closed)
		{
			this.str = str;
			this.closed = closed;
		}
	}

	public class ReaderException extends RuntimeException
	{
		public ReaderException()
		{
			super("Error parsing \"" + str + "\" at " + pos);
		}

		public @Nullable SuggestionList asSuggestionList()
		{
			return null;
		}
	}

	public class ExpectedCharException extends ReaderException
	{
		public final char[] expected;
		private final int atPos;

		public ExpectedCharException(int atPos, char... expected)
		{
			this.expected = expected;
			this.atPos = atPos;
		}

		@Override public SuggestionList asSuggestionList()
		{
			SuggestionList list = new SuggestionList(atPos);
			for (char ch : expected)
			{
				list.addRaw(String.valueOf(ch), null);
			}
			return list;
		}
	}
}
