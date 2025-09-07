package com.BHPages;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

public class BHP_changelogPanel extends JPanel
{
    public final JPanel changelogPanel = new JPanel();
    public final JLabel changelogTextArea = new JLabel();

    public BHP_changelogPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        changelogPanel.setLayout(new BorderLayout());
        changelogPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        String changelogHtml =
                "<html>" +
                        "<body style='width:180px;'>" +
                        "<b>Change Log</b><br><br>" +

                        "<b>Version 1.0</b>" +
                        "<ul style='margin-left:0; padding-left:10px;'>" +
                        "<li><font color='green'>Added:</font> Automatic Activation when target is assigned.</li>" +
                        "<li><font color='green'>Added:</font> Notepad for keeping track of opponent behaviours / traits.</li>" +
                        "<li><font color='green'>Added:</font> Combat level & Total level listed.</li>" +
                        "<li><font color='green'>Added:</font> Skill levels listed.</li>" +
                        "<li><font color='green'>Added:</font> Ability to make surge potion (81 herb - Yes/No).</li>" +
                        "<li><font color='green'>Added:</font> BH & LMS kill counts listed.</li>" +
                        "</ul>" +

                        "<b>Version 1.1</b>" +
                        "<ul style='margin-left:0; padding-left:10px;'>" +
                        "<li><font color='green'>Added:</font> Name Autocomplete to search function.</li>" +
                        "<li><font color='red'>Removed:</font> Save button in favour of FocusChanged Autosave.</li>" +
                        "<li><font color='green'>Added:</font> A note to inform users how autosave works if no player note is present.</li>" +
                        "<li><font color='orange'>Fixed:</font> Save space data corruption that comes from player names.</li>" +
                        "<li><font color='green'>Added:</font> Merged any duplicate notes using \"---\" as a splitter.</li>" +
                        "</ul>" +

                        "<b>Version 1.2</b>" +
                        "<ul style='margin-left:0; padding-left:10px;'>" +
                        "<li><font color='green'>Added:</font> Tabs for a notes panel & target history panel.</li>" +
                        "<li><font color='green'>Added:</font> Buttons for Changelog (here), Discord, Github & Donations.</li>" +
                        "<li><font color='green'>Added:</font> History Log - tiles with player name, last seen & view in notes (eye icon).</li>" +
                        "<li><font color='green'>Added:</font> Right click support \"BH Lookup\" to show note & skills of players.</li>" +
                        "<li><font color='orange'>Fixed:</font> Issue where new lines or tabs were being removed from note data.</li>" +
                        "<li><font color='orange'>Fixed:</font> Issue where names had \"-\" or \"_\" characters removed.</li>" +
                        "</ul>" +
                        "</body>" +
                        "</html>";


        changelogTextArea.setText(changelogHtml);

        changelogPanel.add(changelogTextArea);
        add(changelogPanel);

    }

    public void rebuild()
    {
        changelogPanel.removeAll();

        changelogPanel.add(changelogTextArea);
        add(changelogPanel);
    }
}
