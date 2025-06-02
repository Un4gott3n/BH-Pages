package com.BHPages;

import javax.inject.Inject;
import javax.swing.*;

import com.BHPages.session.SessionHandler;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.ChatMessageType.GAMEMESSAGE;


@Slf4j
@PluginDescriptor(
	name = "BH Pages"
)
public class BHP_Plugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BHP_Config config;

	@Inject
	private SessionHandler sessionHandler;

	private NavigationButton navButton;
	private BHP_Panel BHPpanel;


	//regex for opponent player name extraction (onChatMessage)
	private static final Pattern BOUNTY_PATTERN = Pattern.compile("You have been assigned a new target: <col=[0-9a-f]+>(.*)</col>");

	@Provides
	BHP_Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BHP_Config.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		BHPpanel = injector.getInstance(BHP_Panel.class);
		navButton = NavigationButton.builder()
				.tooltip("BH Pages")
				.icon(ImageUtil.loadImageResource(getClass(), "/BHP_Panel_Icon.png"))
				.priority(99)
				.panel(BHPpanel)
				.build();
		clientToolbar.addNavigation(navButton);

		sessionHandler.loadNotes();
	}

	@Override
	protected void shutDown() throws Exception {

		sessionHandler.saveNotes();
		clientToolbar.removeNavigation(navButton);
	}


	@Subscribe(priority = -2) // run after ChatMessageManager
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == GAMEMESSAGE)
		{
			String message = chatMessage.getMessage();
			Matcher match = BOUNTY_PATTERN.matcher(message);
			if (match.matches())
			{
				lookupPlayerName(match.group(1));
			}
		}
	}

	void lookupPlayerName(String playerName)
	{
		SwingUtilities.invokeLater(() ->
		{
			clientToolbar.openPanel(navButton);
			BHPpanel.lookup(playerName);
		});
	}

	HiscoreEndpoint getWorldEndpoint()
	{
		return HiscoreEndpoint.fromWorldTypes(client.getWorldType());
	}

	void chatGameMessage(String message)
	{
		clientThread.invokeLater(() ->
		{
			client.addChatMessage(GAMEMESSAGE, "", message, null);
		});

	}

}