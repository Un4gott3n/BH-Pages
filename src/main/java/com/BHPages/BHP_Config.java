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

	@ConfigItem(
			position = 3,
			keyName = "playerOption",
			name = "Player lookup option",
			description = "Add BH Lookup option to players."
	)
	default boolean playerOption()
	{
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "bossPanel",
			name = "Show boss kc on lookups",
			description = "Add the boss kc panel below LMS/BH kc"
	)
	default boolean bossPanel()
	{
		return false;
	}

}
