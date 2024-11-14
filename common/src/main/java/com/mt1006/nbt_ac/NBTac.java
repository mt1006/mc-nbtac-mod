package com.mt1006.nbt_ac;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NBTac
{
	public static final String MOD_ID = "nbt_ac";
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
		NbtSuggestion.Type.init();
		return true;
	}
}
