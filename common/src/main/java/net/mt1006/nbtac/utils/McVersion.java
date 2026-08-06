package net.mt1006.nbtac.utils;

import net.minecraft.SharedConstants;
import net.mt1006.nbtac.NBTac;
import org.jetbrains.annotations.Nullable;

public class McVersion
{
	private static boolean initialized = false;
	private static int[] mcv;

	public static int compare(String ver)
	{
		if (!initialized) { initMcVersion(); }

		int[] v = parseSimpleVersion(ver);
		if (v == null)
		{
			NBTac.LOGGER.error("Failed to compare mc version with \"{}\"!", ver);
			return 0;
		}

		if (mcv[0] != v[0]) { return mcv[0] - v[0]; }
		if (mcv[1] != v[1]) { return mcv[1] - v[1]; }
		if (mcv[2] != v[2]) { return mcv[2] - v[2]; }
		return 0;
	}

	private static void initMcVersion()
	{
		String version = SharedConstants.getCurrentVersion().id();

		// remove suffix after '-' if present, e.g. -snapshot-x
		int dashPos = version.indexOf('-');
		if (dashPos != -1) { version = version.substring(0, dashPos); }

		int[] v = parseSimpleVersion(version);
		if (v == null)
		{
			NBTac.LOGGER.error("Failed to parse mc version! Old snapshot naming?");
			mcv = new int[]{999, 0, 0};
		}
		else
		{
			mcv = v;
		}

		initialized = true;
	}

	private static int @Nullable[] parseSimpleVersion(String version)
	{
		String[] parts = version.split("\\.");
		if (parts.length != 2 && parts.length != 3) { return null; }

		int[] v = new int[3];
		try
		{
			v[0] = Integer.parseInt(parts[0]);
			v[1] = Integer.parseInt(parts[1]);
			v[2] = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
		}
		catch (NumberFormatException e) { return null; }
		return v;
	}
}
