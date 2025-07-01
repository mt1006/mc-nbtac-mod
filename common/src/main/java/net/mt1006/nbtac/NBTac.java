package net.mt1006.nbtac;

import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NBTac
{
	public static final String MOD_ID = "nbtac";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static NBTacLoaderInterface loaderInterface = null;

	public static boolean init(boolean isDedicatedServer, NBTacLoaderInterface loaderInterface)
	{
		if (isDedicatedServer)
		{
			NBTac.LOGGER.info("Dedicated server detected - mod setup stopped!");
			return false;
		}

		NBTac.loaderInterface = loaderInterface;
		ModConfig.load();
		Fields.init();
		return true;
	}
}
