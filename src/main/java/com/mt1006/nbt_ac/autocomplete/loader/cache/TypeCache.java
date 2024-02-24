package com.mt1006.nbt_ac.autocomplete.loader.cache;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
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

	public static boolean isEnabled()
	{
		return ModConfig.useCache.val;
	}

	public static boolean load()
	{
		directory = new File(Minecraft.getInstance().gameDirectory, DIRECTORY_NAME);
		directory.mkdirs();

		id = genInstanceId();
		idHash = getMD5(id);

		index = new CacheIndex(new File(directory, INDEX_FILENAME));
		return index.findAndLoad(directory, id, idHash);
	}

	public static void add()
	{
		int elementPos = index.getNextFilePos();
		CacheFile.save(CacheIndex.getFile(directory, elementPos), id);
		index.add(idHash, elementPos);
	}

	public static void updateIndex()
	{
		index.save();
	}

	private static String genInstanceId()
	{
		String modVersionTag = String.format("%s/%s/%s;", NBTac.FOR_LOADER, NBTac.FOR_VERSION, NBTac.VERSION);
		StringBuilder builder = new StringBuilder(modVersionTag);
		TreeSet<String> mods = new TreeSet<>();

		for (ModContainer modInfo : FabricLoader.getInstance().getAllMods())
		{
			ModMetadata metadata = modInfo.getMetadata();
			mods.add(String.format("%s@%s;", metadata.getId(), metadata.getVersion().getFriendlyString()));
		}

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
		catch (NoSuchAlgorithmException exception) { return null; }
	}
}
