package com.mt1006.nbt_ac.autocomplete.loader.cache;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeSet;

public class TypeCache
{
	private static final String DIRECTORY_NAME = "cache/nbt_ac";
	private static final String INDEX_FILENAME = "_index.txt";
	private static File directory = null;
	private static String id, idHash;
	private static CacheIndex index = null;

	public static Results load()
	{
		directory = new File(Minecraft.getInstance().gameDirectory, DIRECTORY_NAME);

		id = genInstanceId();
		idHash = getMD5(id);

		index = new CacheIndex(new File(directory, INDEX_FILENAME));
		boolean cacheFromFileLoaded = index.findAndLoad(directory, id, idHash);

		if (!cacheFromFileLoaded)
		{
			NbtSuggestionManager.clearSuggestionMap();
			return CacheFile.loadFromJar() ? Results.FROM_JAR : Results.ERROR;
		}
		else
		{
			return Results.FROM_FILE;
		}
	}

	public static void add()
	{
		int elementPos = index.getNextFilePos();
		directory.mkdirs();
		CacheFile.save(CacheIndex.getFile(directory, elementPos), id);
		index.add(idHash, elementPos);
	}

	public static void updateIndex()
	{
		index.save();
	}

	private static String genInstanceId()
	{
		StringBuilder builder = new StringBuilder(String.format("%d;", CacheFile.MAX_RADIX));

		TreeSet<String> mods = new TreeSet<>();
		NBTac.loaderInterface.appendModVersionIds(mods);

		mods.forEach(builder::append);
		return builder.toString();
	}

	private static @Nullable String getMD5(String str)
	{
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes(StandardCharsets.UTF_8));
			byte[] bytes = md5.digest();

			StringBuilder builder = new StringBuilder();
			for (byte b : bytes)
			{
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) { return null; }
	}

	public enum Results
	{
		NOT_LOADED, ERROR, FROM_JAR, FROM_FILE, REPLACED
	}
}
