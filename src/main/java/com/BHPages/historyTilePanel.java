package com.BHPages;

import com.BHPages.session.IconHandler;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.function.Consumer;

public class historyTilePanel extends JPanel
{

    private final JPanel topLine;
    private final JPanel controlLine;
    private final JLabel playerName;
    private final JLabel dateTime;
    private final JButton lookupButton;

    private static final ImageIcon EYE_ICON;

    static
    {
        EYE_ICON = IconHandler.EYEICON.getIcon(img -> ImageUtil.resizeImage(img, 16, 16));
    }


    historyTilePanel(String name, String date, Consumer<String> onLookup)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(3, 2, 3, 2));

        topLine = new JPanel(new BorderLayout());
        topLine.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topLine.setBorder(new EmptyBorder(2, 4, 1, 4));
        add(topLine, BorderLayout.NORTH);

        playerName = new JLabel(name);
        playerName.setForeground(Color.WHITE);
        playerName.setFont(playerName.getFont().deriveFont(Font.BOLD));
        topLine.add(playerName, BorderLayout.WEST);

        controlLine = new JPanel(new BorderLayout());
        controlLine.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlLine.setBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR));
        add(controlLine, BorderLayout.SOUTH);

        lookupButton = new JButton();
        lookupButton.setIcon(EYE_ICON);
        lookupButton.setPreferredSize(new Dimension(22, 22));
        lookupButton.setToolTipText("Lookup player in Notes tab");
        lookupButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        lookupButton.setFocusable(false);
        lookupButton.setBorderPainted(false);
        lookupButton.setContentAreaFilled(false);
        lookupButton.setOpaque(false);
        lookupButton.addActionListener(e ->
                SwingUtilities.invokeLater(() -> onLookup.accept(name))
        );

        controlLine.add(lookupButton, BorderLayout.WEST);

        dateTime = new JLabel("Last Seen: " + date);
        dateTime.setForeground(Color.LIGHT_GRAY);
        controlLine.add(dateTime, BorderLayout.EAST);
    }

}
