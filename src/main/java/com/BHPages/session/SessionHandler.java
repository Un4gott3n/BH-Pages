package com.BHPages.session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.RuneLite;

import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SessionHandler
{
    private static final String FILE_NAME = "player_notes.json";
    private final File notesFile;
    private final Gson gson;
    private Map<String, String> playerNotes = new HashMap<>();

    @Inject
    public SessionHandler(Gson gson)
    {
        this.gson = gson;

        File pluginDir = new File(RuneLite.RUNELITE_DIR, "bhpages");
        if (!pluginDir.exists())
        {
            pluginDir.mkdirs();
        }

        this.notesFile = new File(pluginDir, FILE_NAME);
        loadNotes(); // Load on init
    }

    public void loadNotes()
    {
        if (!notesFile.exists()) return;

        try (Reader reader = new FileReader(notesFile))
        {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            playerNotes = gson.fromJson(reader, type);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveNotes()
    {
        try (Writer writer = new FileWriter(notesFile))
        {
            gson.toJson(playerNotes, writer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Map<String, String> getAllNotes()
    {
        return new HashMap<>(playerNotes);
    }

    public String getNoteForPlayer(String playerName)
    {
        return playerNotes.getOrDefault(playerName, "");
    }

    public void updateNote(String playerName, String note)
    {
        if (note == null || note.trim().isEmpty())
        {
            playerNotes.remove(playerName);
        }
        else
        {
            playerNotes.put(playerName, note.trim());
        }

        saveNotes();
    }

}
