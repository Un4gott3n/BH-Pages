package com.BHPages;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("BH Pages")
public interface BHP_Config extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "virtualLevels",
			name = "Display virtual levels",
			description = "Display levels over 99 in the hiscore panel."
	)
	default boolean virtualLevels()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "autocomplete",
			name = "Autocomplete",
			description = "Predict names when typing a name to lookup."
	)
	default boolean autocomplete()
	{
		return true;
	}

}
