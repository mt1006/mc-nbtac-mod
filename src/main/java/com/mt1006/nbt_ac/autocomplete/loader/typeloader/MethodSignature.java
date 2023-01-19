package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import java.util.ArrayList;

public class MethodSignature
{
	public final String signature;
	public final ArrayList<String> arguments = new ArrayList<>();
	public final String retType;

	public MethodSignature(String signature)
	{
		this.signature = signature;
		int pos = 1;

		while (signature.charAt(pos) != ')')
		{
			int offset = (signature.charAt(pos) == '[') ? 1 : 0;

			if (signature.charAt(pos + offset) == 'L')
			{
				int endIndex = signature.indexOf(';', pos);
				arguments.add(signature.substring(pos, endIndex));
				pos = endIndex + 1;
			}
			else
			{
				arguments.add(signature.substring(pos, pos + offset + 1));
				pos += offset + 1;
			}
		}

		retType = signature.substring(pos + 1);
	}

	public int argumentCount()
	{
		return arguments.size();
	}

	public boolean returnsValue()
	{
		return !retType.equals("V");
	}
}
