package com.BHPages.session;

import com.BHPages.BHP_Plugin;
import net.runelite.client.util.ImageUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;

public enum IconHandler
{
    CHANGELOG("/changelog.png"),
    GITHUB("/github.png"),
    DISCORD("/discord.png"),
    DONATORADDR("/bitcoin_clear.png"),
    EYEICON("/eye_icon.png");

    private final String file;

    IconHandler(String file)
    {
        this.file = file;
    }

    public BufferedImage getImage()
    {
        return ImageUtil.loadImageResource(BHP_Plugin.class, file);
    }

    public ImageIcon getIcon()
    {
        return getIcon(UnaryOperator.identity());
    }

    public ImageIcon getIcon(@Nonnull UnaryOperator<BufferedImage> func)
    {
        BufferedImage img = func.apply(getImage());
        return new ImageIcon(img);
    }
}
