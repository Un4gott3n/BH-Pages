package com.BHPages;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class BHP_donatorinfoPanel extends JPanel
{
    private final JPanel donatorinfoPanel = new JPanel();

    public BHP_donatorinfoPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Main content panel
        donatorinfoPanel.setLayout(new BoxLayout(donatorinfoPanel, BoxLayout.Y_AXIS));
        donatorinfoPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        donatorinfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        addLabel("Thank you for any donations!");

        addSpacer();

        addLabel("Crypto Wallet:");
        addLabel("0x18bcfffD1656B1dA70B<br>E5f055B005c5848AEBBCC");  //added <br> to ensure its all displayed
        addCopyButton("Copy Address", "0x18bcfffD1656B1dA70BE5f055B005c5848AEBBCC");

        addSpacer();

        addLabel("Network:");
        addLabel("ERC20 (Ethereum)");
        addCopyButton("Copy Network", "ERC20");

        addSpacer();

        addLabel("Buy me a coffee?");
        addLabel("https://coff.ee/un4gott3n");
        addCopyButton("Copy Coffee Link", "https://coff.ee/un4gott3n");

        addSpacer();

        addLabel("Patreon:");
        addLabel("https://patreon.com/Un4gott3n");
        addCopyButton("Copy Patreon Link", "https://patreon.com/Un4gott3n");

        add(donatorinfoPanel, BorderLayout.CENTER);
    }

    private void addLabel(String htmlText)
    {
        JLabel label = new JLabel("<html>" + htmlText + "</html>");
        label.setForeground(Color.LIGHT_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        donatorinfoPanel.add(label);
    }

    private void addCopyButton(String text, String content)
    {
        JButton copyButton = new JButton(text);
        copyButton.setToolTipText("Copy to clipboard");
        copyButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        copyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyButton.addActionListener(e -> copyToClipboard(content));
        donatorinfoPanel.add(copyButton);
    }

    private void addSpacer()
    {
        donatorinfoPanel.add(Box.createVerticalStrut(10));
    }

    private void copyToClipboard(String text)
    {
        StringSelection str = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(str, null);
    }
}
