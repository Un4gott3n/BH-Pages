package com.BHPages;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.inject.Inject;

class BHP_PanelContainer extends PluginPanel
{
    private final JPanel BHP_Panel = new JPanel();

    private final MaterialTabGroup tabGroup = new MaterialTabGroup(BHP_Panel);
    private final MaterialTab historyTab;
    private final MaterialTab notesTab;


    private final BHP_NotesPanel notesPanel;
    //@Getter
    private final BHP_HistoryPanel historyPanel;

    @Inject
    private  BHP_PanelContainer(BHP_NotesPanel notesPanel, BHP_HistoryPanel historyPanel)
    {
        this.notesPanel = notesPanel;
        this.historyPanel = historyPanel;

        setLayout(new BorderLayout(0,0));

        setBackground(ColorScheme.DARK_GRAY_COLOR);

        notesTab = new MaterialTab("Notes", tabGroup, notesPanel);
        historyTab = new MaterialTab("History", tabGroup, historyPanel);

        tabGroup.setBorder(new EmptyBorder(10, 0, 0, 0));
        tabGroup.addTab(notesTab);
        tabGroup.addTab(historyTab);
        tabGroup.select(notesTab);

        add(tabGroup, BorderLayout.NORTH);
        add(BHP_Panel, BorderLayout.CENTER);
    }

    void showHistory()
    {
        if(historyPanel.isShowing())
        {
            return;
        }

        tabGroup.select(historyTab);
        revalidate();
    }

    void showNotes()
    {
        if(notesPanel.isShowing())
        {
            return;
        }

        tabGroup.select(notesTab);
        revalidate();
    }

    public BHP_NotesPanel getNotesPanel()
    {
        return notesPanel;
    }

    public BHP_HistoryPanel getHistoryPanel(){ return historyPanel; }

}
