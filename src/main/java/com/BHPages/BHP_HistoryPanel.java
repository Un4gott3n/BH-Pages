package com.BHPages;
import com.BHPages.session.IconHandler;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.List;
import java.util.LinkedList;


class BHP_HistoryPanel extends JPanel
{
    private final BHP_Plugin plugin;
    private final BHP_changelogPanel BHPchangelog = new BHP_changelogPanel();
    private final BHP_donatorinfoPanel BHPdonator = new BHP_donatorinfoPanel();

    //buttons
    private final JPanel buttonPanel;
    private final JButton changelogBtn;
    private final JButton githubpageBtn;
    private final JButton discordnameBtn;
    private final JButton donatorBtn;

    //history panel
    private final JPanel historyPanel;
    class historyEntry //for usage with our history tiles.
    {
        final String playerName;
        final String timeStamp;

        historyEntry(String name, String time)
        {
            this.playerName = name;
            this.timeStamp = time;
        }
    }
    private final List<historyEntry> historyEntryList = new LinkedList<>();

    private final JScrollPane scrollableContainer;

    private static final ImageIcon CHANGELOG_ICON;
    private static final ImageIcon GITHUB_ICON;
    private static final ImageIcon DISCORD_ICON;
    private static final ImageIcon DONATORADDR_ICON;


    static
    {
        CHANGELOG_ICON = IconHandler.CHANGELOG.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
        GITHUB_ICON = IconHandler.GITHUB.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
        DISCORD_ICON = IconHandler.DISCORD.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
        DONATORADDR_ICON = IconHandler.DONATORADDR.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
    }

    @Inject
    BHP_HistoryPanel(BHP_Plugin plugin, BHP_changelogPanel BHPchangelog, BHP_donatorinfoPanel BHPdonator, BHP_Config BHPconfig)
    {
        this.plugin = plugin;

        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout(0, 5));

        /* BUTTONS */
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 0));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        changelogBtn = new JButton();
        SwingUtil.removeButtonDecorations(changelogBtn);
        changelogBtn.setIcon(CHANGELOG_ICON);
        changelogBtn.setToolTipText("Change Log");
        changelogBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
        changelogBtn.setUI(new BasicButtonUI());
        changelogBtn.addActionListener((ev) -> {
            BHPchangelog.rebuild();

            if(changelogActive())
            {
                changelogBtn.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                changelogClose();
            }
            else
            {
                changelogBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
                changelogOpen();
            }
        });
        changelogBtn.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                changelogBtn.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                if(changelogActive())
                {
                    changelogBtn.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                }
                else
                {
                    changelogBtn.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
                }
            }
        });

        buttonPanel.add(changelogBtn);

        githubpageBtn = new JButton();
        SwingUtil.removeButtonDecorations(githubpageBtn);
        githubpageBtn.setIcon(GITHUB_ICON);
        githubpageBtn.setToolTipText("Report issues or contribute on Github");
        githubpageBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
        githubpageBtn.setUI(new BasicButtonUI());
        githubpageBtn.addActionListener((ev) -> LinkBrowser.browse("https://github.com/Un4gott3n/BH-Pages"));
        githubpageBtn.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                githubpageBtn.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                githubpageBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });

        buttonPanel.add(githubpageBtn);

        discordnameBtn = new JButton();
        SwingUtil.removeButtonDecorations(discordnameBtn);
        discordnameBtn.setIcon(DISCORD_ICON);
        discordnameBtn.setToolTipText("Join BH Pages Discord!");
        discordnameBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
        discordnameBtn.setUI(new BasicButtonUI());
        discordnameBtn.addActionListener((ev) -> LinkBrowser.browse("https://discord.gg/E8Xu3QAps7"));  //Offical BH Pages Discord
        discordnameBtn.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                discordnameBtn.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                discordnameBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });
        buttonPanel.add(discordnameBtn);

        donatorBtn = new JButton();
        SwingUtil.removeButtonDecorations(donatorBtn);
        donatorBtn.setIcon(DONATORADDR_ICON);
        donatorBtn.setToolTipText("Any tips/donations appreciated");
        donatorBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
        donatorBtn.setUI(new BasicButtonUI());
        donatorBtn.addActionListener((ev) -> {
            if(donatorActive())
            {
                donatorBtn.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                donatorClose();
            }
            else
            {
                donatorBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
                donatorOpen();
            }
        });

        donatorBtn.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                donatorBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                if(donatorActive())
                {
                    donatorBtn.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                }
                else
                {
                    donatorBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
                }
            }
        });

        buttonPanel.add(donatorBtn);
        /* BUTTONS END */

        /* HISTORY PANEL */
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        scrollableContainer = new JScrollPane();
        scrollableContainer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableContainer.getVerticalScrollBar().setUnitIncrement(12);
        scrollableContainer.setBorder(null);
        scrollableContainer.setViewportView(historyPanel); //default view
        /* HISTORY PANEL END */

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollableContainer, BorderLayout.CENTER);
    }

    private boolean changelogActive()
    {
        return scrollableContainer.getViewport().getView() == BHPchangelog;
    }
    private boolean donatorActive()
    {
        return scrollableContainer.getViewport().getView() == BHPdonator;
    }

    private void changelogClose()
    {
        scrollableContainer.setViewportView(historyPanel);
        historyPanel.setVisible(true);

        repaint();
        revalidate();
    }

    private void donatorClose()
    {
        scrollableContainer.setViewportView(historyPanel);
        historyPanel.setVisible(true);

        repaint();
        revalidate();
    }

    private void changelogOpen()
    {
        scrollableContainer.setViewportView(BHPchangelog);
        historyPanel.setVisible(false);

        repaint();
        revalidate();
    }

    private void donatorOpen()
    {
        scrollableContainer.setViewportView(BHPdonator);
        historyPanel.setVisible(false);

        repaint();
        revalidate();
    }

    public void addHistoryTile(String name, String datestamp)
    {
        if(datestamp.isEmpty())
        {
            return;
        }

        historyPanel.removeAll(); //delete all entries

        historyEntryList.add(0, new historyEntry(name, datestamp)); //Add new entry to the top of the list

        for(historyEntry entry : historyEntryList)
        {
            JPanel tile = new historyTilePanel(entry.playerName, entry.timeStamp, this::triggerLookup);
            historyPanel.add(tile);
        }
        repaint();
        revalidate();

    }

    private void triggerLookup(String name)
    {
        plugin.lookupPlayerName(name, ""); //ensure new entry isn't added to history by passing blank datetime
    }

    public JPanel getHistoryPanel()
    {
        return historyPanel;
    }


}
