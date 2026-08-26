package com.BHPages;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;

import com.BHPages.session.SessionHandler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.events.ConfigChanged;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.ChatMessageType.GAMEMESSAGE;


@Slf4j
@PluginDescriptor(
		name = "BH Pages",
		description = "Keep track of your opponents in Bounty Hunter.",
		tags = {"Bounty", "Hunter", "bounty", "hunter", "BH", "bh", "pvp"}
)

public class BHP_Plugin extends Plugin {

	private static final String LOOKUP = "BH Lookup";

	//temp list for right-click menu name preservation, thx again HiScores :)
	private final Map<Integer,String> playernameIndex = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private Provider<MenuManager> menuManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BHP_Config config;

	@Inject
	private SessionHandler sessionHandler;

	private NavigationButton navButton;
	private BHP_NotesPanel BHPNotesPanel;
	private BHP_PanelContainer BHPPanelContainer;
	private BHP_HistoryPanel BHPHistoryPanel;

	//regex for opponent player name extraction (onChatMessage). 26/08 - changed from <col=> to @mes_hl_red@
	private static final Pattern BOUNTY_PATTERN = Pattern.compile("You have been assigned a new target: @mes_hl_red@(.*)</col>");

	@Provides
	BHP_Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BHP_Config.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		BHPPanelContainer = injector.getInstance(BHP_PanelContainer.class);
		BHPNotesPanel = BHPPanelContainer.getNotesPanel();
		BHPHistoryPanel = BHPPanelContainer.getHistoryPanel();

		navButton = NavigationButton.builder()
				.tooltip("BH Pages")
				.icon(ImageUtil.loadImageResource(getClass(), "/BHP_Panel_Icon.png"))
				.priority(99)
				.panel(BHPPanelContainer)
				.build();

		clientToolbar.addNavigation(navButton);

		if (config.playerOption())
		{
			menuManager.get().addPlayerMenuItem(LOOKUP);
		}

		sessionHandler.loadNotes();

		if(config.bossPanel())
		{
			BHPNotesPanel.showBossPanel(true);
		}
	}

	@Override
	protected void shutDown() throws Exception {

		sessionHandler.saveNotes();
		BHPNotesPanel.shutdown();
		clientToolbar.removeNavigation(navButton);
		playernameIndex.clear();
		menuManager.get().removePlayerMenuItem(LOOKUP);

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("BH Pages"))
		{
			//remove these options to avoid duplicates
			menuManager.get().removePlayerMenuItem(LOOKUP);

			if (config.playerOption())
			{
				menuManager.get().addPlayerMenuItem(LOOKUP);
			}

			if(config.bossPanel())
			{
				BHPNotesPanel.showBossPanel(true);
			}
			else
			{
				BHPNotesPanel.showBossPanel(false);
			}
		}
	}

	@Subscribe
	private void onMenuOpened(MenuOpened event)
	{
		playernameIndex.clear();
		for (MenuEntry entry : event.getMenuEntries())
		{
			if (entry.getType() != MenuAction.RUNELITE_PLAYER || !entry.getOption().equals(LOOKUP))
			{
				continue;
			}

			final Player player = entry.getPlayer();
			if (player == null)
			{
				continue;
			}

			playernameIndex.put(entry.getIdentifier(), player.getName());
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if ((event.getType() != MenuAction.CC_OP.getId() && event.getType() != MenuAction.CC_OP_LOW_PRIORITY.getId()) || !config.playerOption())
		{
			return;
		}

		final String option = event.getOption();
		final int componentId = event.getActionParam1();
		final int groupId = WidgetUtil.componentToInterface(componentId);

		if (groupId == InterfaceID.FRIENDS && option.equals("Delete")
				|| groupId == InterfaceID.CHATCHANNEL_CURRENT && (option.equals("Add ignore") || option.equals("Remove friend"))
				|| groupId == InterfaceID.CHATBOX && (option.equals("Add ignore") || option.equals("Message"))
				|| groupId == InterfaceID.IGNORE && option.equals("Delete")
				|| (componentId == InterfaceID.ClansSidepanel.PLAYERLIST || componentId == InterfaceID.ClansGuestSidepanel.PLAYERLIST) && (option.equals("Add ignore") || option.equals("Remove friend"))
				|| groupId == InterfaceID.PM_CHAT && (option.equals("Add ignore") || option.equals("Message"))
				|| groupId == InterfaceID.GIM_SIDEPANEL && (option.equals("Add friend") || option.equals("Remove friend") || option.equals("Remove ignore"))
		)
		{
			client.createMenuEntry(-2)
					.setOption(LOOKUP)
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.setIdentifier(event.getIdentifier())
					.onClick(e ->
					{
						String target = Text.removeTags(e.getTarget());
						if(target.isEmpty())
						{
							return;
						}

						lookupPlayerName(target, "");

					});
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() == MenuAction.RUNELITE_PLAYER && event.getMenuOption().equals(LOOKUP))
		{
			Player player = event.getMenuEntry().getPlayer();
			final String target;
			if (player != null)
			{
				target = player.getName();
			}
			else
			{
				target = playernameIndex.get(event.getId());
			}
			playernameIndex.clear();
			
			lookupPlayerName(target, ""); //blank datetime to ensure history is NOT added.
		}
	}

	@Subscribe(priority = -2) // run after ChatMessageManager
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == GAMEMESSAGE)
		{
			String date_t = getCurrentTimestamp();
			String message = chatMessage.getMessage();
			Matcher match = BOUNTY_PATTERN.matcher(message);
			if (match.matches())
			{
				lookupPlayerName(match.group(1), date_t);
			}
		}
	}

	void lookupPlayerName(String playerName, String datetime)
	{
		SwingUtilities.invokeLater(() ->
		{
			clientToolbar.openPanel(navButton);
			BHPPanelContainer.showNotes();
			BHPNotesPanel.lookup(playerName);
			BHPHistoryPanel.addHistoryTile(playerName, datetime);
		});
	}

	HiscoreEndpoint getWorldEndpoint()
	{
		return HiscoreEndpoint.fromWorldTypes(client.getWorldType());
	}

	void chatGameMessage(String message) //function without
	{
		clientThread.invokeLater(() ->
		{
			client.addChatMessage(GAMEMESSAGE, "", message, null);
		});

	}

	private String getCurrentTimestamp()
	{
		String format = "MMM/dd HH:mm";
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(new Date());
	}
}