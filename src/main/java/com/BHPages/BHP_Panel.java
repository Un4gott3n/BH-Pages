package com.BHPages;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.BHPages.session.SessionHandler;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.*;

import static net.runelite.api.SpriteID.TAB_COMBAT;
import static net.runelite.client.hiscore.HiscoreSkill.*;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Singleton
public class BHP_Panel extends PluginPanel
{

    private final BHP_Plugin BHPplugin;
    private final BHP_Config BHPconfig;
    private final HiscoreClient hiscoreClient;
    private final SessionHandler sessionHandler;

    //panel assets
    private final JPanel BHP_Panel = new JPanel(new GridBagLayout());
    private final IconTextField opp_nameText;
    private final JButton opp_search_button;
    private final JButton opp_save_button;
    private final JTextArea notesEditor;
    private final JPanel totalPanel;
    private final JPanel statsPanel;
    private final JLabel surgePot;
    private final JLabel surgeDash;
    private final JLabel surgeNo;
    private final JLabel surgeYes;
    private final JPanel minigamePanel;

    //Runescape character usernames are limited to 12 chars
    int MAX_USERNAME_LENGTH = 12;

    //group to hold all the skill levels & cmb level
    private final SpriteManager spriteManager;

    // Not an enummap because we need null keys for combat
    private final Map<HiscoreSkill, JLabel> skillLabels = new HashMap<>();

    //the skills to show
    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER
    );

    /* The currently selected endpoint */
    private HiscoreEndpoint selectedEndPoint;

    /* Used to prevent users from switching endpoint tabs while the results are loading */
    private boolean loading = false;

    @Inject
    BHP_Panel(Client client, BHP_Plugin plugin, BHP_Config config, SessionHandler sessionHandler, SpriteManager spriteManager, HiscoreClient hiscoreClient)  //BHP_Panel(Client client, BHP_Plugin plugin, BHP_Config config, SpriteManager spriteManager, HiscoreClient hiscoreClient)
    {
        this.BHPplugin = plugin;
        this.BHPconfig = config;
        this.sessionHandler = sessionHandler;
        this.hiscoreClient = hiscoreClient;
        this.spriteManager = spriteManager;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(0, 0, 10, 0);

        //player name / search bar
        opp_nameText = new IconTextField();
        opp_nameText.setIcon(IconTextField.Icon.SEARCH);
        opp_nameText.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20,30));
        opp_nameText.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        opp_nameText.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        opp_nameText.setMinimumSize(new Dimension(0, 30));
        opp_nameText.addActionListener(evt ->
        {
            String searchName = opp_nameText.getText();
            lookup(searchName);
        });
        opp_nameText.addClearListener(() ->
        {
            opp_nameText.setIcon(IconTextField.Icon.SEARCH);
            opp_nameText.setEditable(true);

        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        add(opp_nameText, c);

        c.insets = new Insets(0, 0, 10, 10);

        //search button to display players
        opp_search_button = new JButton("Search");
        opp_search_button.addActionListener(evt ->
        {
            String searchName = opp_nameText.getText();
            lookup(searchName);
        });
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        add(opp_search_button, c);

        c.insets = new Insets(0, 0, 10, 0);

        notesEditor = new JTextArea();  // need to define the note editor area before the save button to grab text

        //save button to store player data
        opp_save_button = new JButton("Save");
        opp_save_button.addActionListener( e ->
        {
            String note = notesEditor.getText();
            String playerName = opp_nameText.getText();

            if(!note.isEmpty() && !playerName.isEmpty())
            {
                sessionHandler.updateNote(playerName, note);
                postSaveMsg("[+]BH Pages: Note for " + playerName + " has been updated.");
            }
        });
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        add(opp_save_button, c);

        //note area for player notes - see note above for declaration.
        notesEditor.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        notesEditor.setLineWrap(true);
        notesEditor.setWrapStyleWord(true);
        notesEditor.setTabSize(2);
        notesEditor.setRows(7);
        notesEditor.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {

            }

            @Override
            public void focusLost(FocusEvent e)
            {
                String note = notesEditor.getText();
                String playerName = opp_nameText.getText();

                if(!note.isEmpty() && !playerName.isEmpty())
                {
                    sessionHandler.updateNote(playerName, note);
                    postSaveMsg("[+]BH Pages: Note for " + playerName + " has been updated.");
                }
            }
        });
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        add(notesEditor, c);

        //Display total combat level and combined levels on acc.
        totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(1, 2));
        totalPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        totalPanel.add(makeHiscorePanel(null)); //combat has no hiscore skill, referred to as null
        totalPanel.add(makeHiscorePanel(OVERALL)); //total level holder

        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        add(totalPanel, c);

        // Panel that holds skill icons
        statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(8, 3));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // For each skill on the ingame skill panel, create a Label and add it to the UI
        for (HiscoreSkill skill : SKILLS)
        {
            JPanel panel = makeHiscorePanel(skill);
            statsPanel.add(panel);
        }

        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        add(statsPanel, c);

        //Can use a Surge pot?
        surgePot = new JLabel("Can use Surge Potion?");
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        add(surgePot, c);

        //I draw all assets on screen with only dash visible on default state
        //easier to turn on and off with setVisible() true/false later on when lookup() runs.
        surgeDash = new JLabel("-", JLabel.CENTER);
        surgeDash.setForeground(Color.white);
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        add(surgeDash, c);

        surgeNo = new JLabel("No", JLabel.CENTER);
        surgeNo.setForeground(Color.red);
        surgeNo.setVisible(false);
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        add(surgeNo, c);

        surgeYes = new JLabel("Yes", JLabel.CENTER);
        surgeYes.setForeground(Color.green);
        surgeYes.setVisible(false);
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        add(surgeYes, c);


        //panel to display LMS, BH Rouge (nontarget) & BH Hunter (target)
        minigamePanel = new JPanel();
        minigamePanel.setLayout(new GridLayout(0, 3));
        minigamePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        minigamePanel.add(makeHiscorePanel(LAST_MAN_STANDING));
        minigamePanel.add(makeHiscorePanel(BOUNTY_HUNTER_ROGUE));
        minigamePanel.add(makeHiscorePanel(BOUNTY_HUNTER_HUNTER));

        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        add(minigamePanel, c);

    }

//asset functions

    public void lookup(String username)
    {
        opp_nameText.setText(username);
        String note = sessionHandler.getNoteForPlayer(username);
        notesEditor.setText(note);
        lookup();
    }

    private void lookup()
    {


        final String lookup = sanitize(opp_nameText.getText());

        if(Strings.isNullOrEmpty(lookup))
        {
            return;
        }

        if(lookup.length() > MAX_USERNAME_LENGTH)
        {
            opp_nameText.setIcon(IconTextField.Icon.ERROR);
            loading = false;
            return;
        }

        repaint();

        opp_nameText.setEditable(false);
        opp_nameText.setIcon(IconTextField.Icon.LOADING_DARKER);
        loading = true;

        for (Map.Entry<HiscoreSkill, JLabel> entry : skillLabels.entrySet())
        {
            HiscoreSkill skill = entry.getKey();
            JLabel label = entry.getValue();
            HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

            label.setText(pad("--", skillType));
            label.setToolTipText(skill == null ? "Combat" : skill.getName());
        }

        //set endpoint to normal (change later?)
        selectedEndPoint = HiscoreEndpoint.NORMAL;

        hiscoreClient.lookupAsync(lookup, selectedEndPoint).whenCompleteAsync((result, ex) ->
                SwingUtilities.invokeLater(() ->
                {
                    if (!sanitize(opp_nameText.getText()).equals(lookup))
                    {
                        // search has changed in the meantime
                        return;
                    }

                    if (result == null || ex != null)
                    {
                        if (ex != null)
                        {
                            //not save message but you need to tell users if error.
                            postSaveMsg("Error fetching Hiscore data " + ex.getMessage());
                        }

                        opp_nameText.setIcon(IconTextField.Icon.ERROR);
                        opp_nameText.setEditable(true);
                        loading = false;
                        return;
                    }

                    //successful player search
                    opp_nameText.setIcon(IconTextField.Icon.SEARCH);
                    opp_nameText.setEditable(true);
                    loading = false;

                    applyHiscoreResult(result);
                }));

    }

    private void applyHiscoreResult(HiscoreResult result)
    {
        assert SwingUtilities.isEventDispatchThread();
        repaint();

        //to be added later (v2?) ---v
        //nameAutocompleter.addToSearchHistory(result.getPlayer().toLowerCase());

        for (Map.Entry<HiscoreSkill, JLabel> entry : skillLabels.entrySet())
        {
            HiscoreSkill skill = entry.getKey();
            JLabel label = entry.getValue();
            Skill s;

            if (skill == null)
            {
                if (result.getPlayer() != null)
                {
                    int combatLevel = Experience.getCombatLevel(
                            result.getSkill(ATTACK).getLevel(),
                            result.getSkill(STRENGTH).getLevel(),
                            result.getSkill(DEFENCE).getLevel(),
                            result.getSkill(HITPOINTS).getLevel(),
                            result.getSkill(MAGIC).getLevel(),
                            result.getSkill(RANGED).getLevel(),
                            result.getSkill(PRAYER).getLevel()
                    );
                    label.setText(Integer.toString(combatLevel));
                }
            }
            else if ((s = result.getSkill(skill)) != null)
            {
                final long exp = s.getExperience();
                final boolean isSkill = skill.getType() == HiscoreSkillType.SKILL;
                int level = -1;

                if(result.getSkill(HERBLORE).getLevel() >= 81)
                {
                    surgeDash.setVisible(false);
                    surgeNo.setVisible(false);
                    surgeYes.setVisible(true);
                }
                else
                {
                    surgeDash.setVisible(false);
                    surgeYes.setVisible(false);
                    surgeNo.setVisible(true);
                }

                if (BHPconfig.virtualLevels() && isSkill && exp > -1L)
                {
                    level = Experience.getLevelForXp((int) exp);
                }
                else if (!isSkill || exp != -1L)
                {
                    // for skills, level is only valid if exp is not -1
                    // otherwise level is always valid
                    level = s.getLevel();
                }

                if (level != -1)
                {
                    label.setText(pad(formatLevel(level), skill.getType()));
                }
            }

            label.setToolTipText(detailsHtml(result, skill));
        }
    }

    private JPanel makeHiscorePanel(HiscoreSkill skill)
    {
        HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText(pad("--", skillType));

        spriteManager.getSpriteAsync(skill == null ? TAB_COMBAT : skill.getSpriteId(), 0, (sprite) ->
                SwingUtilities.invokeLater(() ->
                {
                    // Icons are all 25x25 or smaller, so they're fit into a 25x25 canvas to give them a consistent size for
                    // better alignment. Further, they are then scaled down to 20x20 to not be overly large in the panel.
                    final BufferedImage scaledSprite = ImageUtil.resizeImage(ImageUtil.resizeCanvas(sprite, 25, 25), 20, 20);
                    label.setIcon(new ImageIcon(scaledSprite));
                }));

        boolean totalLabel = skill == OVERALL || skill == null; //overall or combat
        label.setIconTextGap(totalLabel ? 10 : 4);

        JPanel skillPanel = new JPanel();
        skillPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        skillPanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        skillLabels.put(skill, label);
        skillPanel.add(label);

        return skillPanel;
    }

    private String detailsHtml(HiscoreResult result, HiscoreSkill skill)
    {
        String openingTags = "<html><body style = 'padding: 5px;color:#989898'>";
        String closingTags = "</html><body>";

        String content = "";

        if (skill == null)
        {
            double combatLevel = Experience.getCombatLevelPrecise(
                    result.getSkill(ATTACK).getLevel(),
                    result.getSkill(STRENGTH).getLevel(),
                    result.getSkill(DEFENCE).getLevel(),
                    result.getSkill(HITPOINTS).getLevel(),
                    result.getSkill(MAGIC).getLevel(),
                    result.getSkill(RANGED).getLevel(),
                    result.getSkill(PRAYER).getLevel()
            );

            double combatExperience = result.getSkill(ATTACK).getExperience()
                    + result.getSkill(STRENGTH).getExperience() + result.getSkill(DEFENCE).getExperience()
                    + result.getSkill(HITPOINTS).getExperience() + result.getSkill(MAGIC).getExperience()
                    + result.getSkill(RANGED).getExperience() + result.getSkill(PRAYER).getExperience();

            content += "<p><span style = 'color:white'>Combat</span></p>";
            content += "<p><span style = 'color:white'>Exact Combat Level:</span> " + QuantityFormatter.formatNumber(combatLevel) + "</p>";
            content += "<p><span style = 'color:white'>Experience:</span> " + QuantityFormatter.formatNumber(combatExperience) + "</p>";
        }
        else
        {
            switch (skill)
            {
                case CLUE_SCROLL_ALL:
                {
                    content += "<p><span style = 'color:white'>Clues</span></p>";
                    content += buildClueLine(result, "All", CLUE_SCROLL_ALL);
                    content += buildClueLine(result, "Beginner", CLUE_SCROLL_BEGINNER);
                    content += buildClueLine(result, "Easy", CLUE_SCROLL_EASY);
                    content += buildClueLine(result, "Medium", CLUE_SCROLL_MEDIUM);
                    content += buildClueLine(result, "Hard", CLUE_SCROLL_HARD);
                    content += buildClueLine(result, "Elite", CLUE_SCROLL_ELITE);
                    content += buildClueLine(result, "Master", CLUE_SCROLL_MASTER);
                    break;
                }
                case BOUNTY_HUNTER_ROGUE:
                case BOUNTY_HUNTER_HUNTER:
                case PVP_ARENA_RANK:
                case LAST_MAN_STANDING:
                case SOUL_WARS_ZEAL:
                case RIFTS_CLOSED:
                case COLOSSEUM_GLORY:
                case COLLECTIONS_LOGGED:
                {
                    content += buildMinigameTooltip(result.getSkill(skill), skill);
                    break;
                }
                case LEAGUE_POINTS:
                {
                    Skill leaguePoints = result.getSkill(LEAGUE_POINTS);
                    String rank = (leaguePoints.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(leaguePoints.getRank());
                    content += "<p><span style = 'color:white'>League Points</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (leaguePoints.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Points:</span> " + QuantityFormatter.formatNumber(leaguePoints.getLevel()) + "</p>";
                    }
                    break;
                }
                case OVERALL:
                {
                    Skill requestedSkill = result.getSkill(skill);
                    String rank = (requestedSkill.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getRank());
                    String exp = (requestedSkill.getExperience() == -1L) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getExperience());
                    content += "<p><span style = 'color:white'>" + skill.getName() + "</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    content += "<p><span style = 'color:white'>Experience:</span> " + exp + "</p>";
                    break;
                }
                default:
                {
                    if (skill.getType() == HiscoreSkillType.BOSS)
                    {
                        String rank = "Unranked";
                        String lvl = null;
                        Skill requestedSkill = result.getSkill(skill);
                        if (requestedSkill != null)
                        {
                            if (requestedSkill.getRank() > -1)
                            {
                                rank = QuantityFormatter.formatNumber(requestedSkill.getRank());
                            }
                            if (requestedSkill.getLevel() > -1)
                            {
                                lvl = QuantityFormatter.formatNumber(requestedSkill.getLevel());
                            }
                        }

                        content += "<p><span style = 'color:white'>Boss:</span> " + skill.getName() + "</p>";
                        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                        if (lvl != null)
                        {
                            content += "<p><span style = 'color:white'>KC:</span> " + lvl + "</p>";
                        }
                    }
                    else
                    {
                        Skill requestedSkill = result.getSkill(skill);
                        final long experience = requestedSkill.getExperience();

                        String rank = (requestedSkill.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getRank());
                        String exp = (experience == -1L) ? "Unranked" : QuantityFormatter.formatNumber(experience);
                        String remainingXp;
                        if (experience == -1L)
                        {
                            remainingXp = "Unranked";
                        }
                        else
                        {
                            int currentLevel = Experience.getLevelForXp((int) experience);
                            remainingXp = (currentLevel + 1 <= Experience.MAX_VIRT_LEVEL) ? QuantityFormatter.formatNumber(Experience.getXpForLevel(currentLevel + 1) - experience) : "0";
                        }

                        content += "<p><span style = 'color:white'>Skill:</span> " + skill.getName() + "</p>";
                        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                        content += "<p><span style = 'color:white'>Experience:</span> " + exp + "</p>";
                        content += "<p><span style = 'color:white'>Remaining XP:</span> " + remainingXp + "</p>";
                    }
                    break;
                }
            }
        }

        // Add a html progress bar to the hover information
        if (skill != null && skill.getType() == HiscoreSkillType.SKILL)
        {
            long experience = result.getSkill(skill).getExperience();
            if (experience >= 0)
            {
                int currentXp = (int) experience;
                int currentLevel = Experience.getLevelForXp(currentXp);
                int xpForCurrentLevel = Experience.getXpForLevel(currentLevel);
                int xpForNextLevel = currentLevel + 1 <= Experience.MAX_VIRT_LEVEL ? Experience.getXpForLevel(currentLevel + 1) : -1;

                double xpGained = currentXp - xpForCurrentLevel;
                double xpGoal = xpForNextLevel != -1 ? xpForNextLevel - xpForCurrentLevel : 100;
                int progress = (int) ((xpGained / xpGoal) * 100f);

                // had to wrap the bar with an empty div, if i added the margin directly to the bar, it would mess up
                content += "<div style = 'margin-top:3px'>"
                        + "<div style = 'background: #070707; border: 1px solid #070707; height: 6px; width: 100%;'>"
                        + "<div style = 'height: 6px; width: " + progress + "%; background: #dc8a00;'>"
                        + "</div>"
                        + "</div>"
                        + "</div>";
            }
        }

        return openingTags + content + closingTags;
    }

    private static String buildMinigameTooltip(Skill s, HiscoreSkill hiscoreSkill)
    {
        String rank = (s.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(s.getRank());
        String content = "";
        content += "<p><span style = 'color:white'>" + hiscoreSkill.getName() + "</span></p>";
        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
        if (s.getLevel() > -1)
        {
            content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(s.getLevel()) + "</p>";
        }
        return content;
    }

    private static String buildClueLine(HiscoreResult result, String name, HiscoreSkill skill)
    {
        Skill sk = result.getSkill(skill);
        String count = sk.getLevel() == -1
                ? "0"
                : QuantityFormatter.formatNumber(sk.getLevel());
        String rank = sk.getRank() == -1
                ? "Unranked"
                : QuantityFormatter.formatNumber(sk.getRank());
        return "<p><span style = 'color:white'>" + name + ":</span> " + count + " <span style = 'color:white'>Rank:</span> " + rank + "</p>";
    }

    private static String sanitize(String lookup)
    {
        return lookup.replace('\u00A0', ' ');
    }

    @VisibleForTesting
    static String formatLevel(int level)
    {
        if (level < 10000)
        {
            return Integer.toString(level);
        }
        else
        {
            return (level / 1000) + "k";
        }
    }

    private static String pad(String str, HiscoreSkillType type)
    {
        // Left pad label text to keep labels aligned
        int pad = type == HiscoreSkillType.BOSS ? 4 : 2;
        return StringUtils.leftPad(str, pad);
    }


    private void postSaveMsg(String msg)
    {
        BHPplugin.chatGameMessage(msg);
    }

}
