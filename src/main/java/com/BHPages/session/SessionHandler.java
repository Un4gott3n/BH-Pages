package com.BHPages.session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.RuneLite;

import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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

        try (Reader reader = new InputStreamReader(new FileInputStream(notesFile), StandardCharsets.UTF_8))
        {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> rawNotes = gson.fromJson(reader, type);

            boolean changed = false;
            Map<String, String> cleaned = new HashMap<>();

            for (Map.Entry<String, String> entry : rawNotes.entrySet())
            {
                String rawName = entry.getKey();
                String rawNote = entry.getValue();

                String cleanName = sanitize(rawName);
                String cleanNote = sanitizeNote(rawNote);

                if (cleaned.containsKey(cleanName))
                {
                    String existingNote = cleaned.get(cleanName);

                    // Merge if note is different and not already part of the current one
                    if (!existingNote.equals(cleanNote) && !cleanNote.isEmpty())
                    {
                        if (!existingNote.toLowerCase().contains(cleanNote.toLowerCase()))
                        {
                            String mergedNote = existingNote + " --- " + cleanNote;
                            cleaned.put(cleanName, mergedNote);
                            changed = true;
                        }
                    }
                }
                else
                {
                    cleaned.put(cleanName, cleanNote);
                    if (!cleanName.equals(rawName) || !cleanNote.equals(rawNote))
                    {
                        changed = true;
                    }
                }
            }

            this.playerNotes = cleaned;

            if (changed)
            {
                saveNotes(); // Re-save fixed file
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void saveNotes()
    {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(notesFile), StandardCharsets.UTF_8))
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

    private String sanitize(String input)
    {
        if (input == null)
        {
            return "";
        }

        return input
                .replace('\uFFFD', ' ')             // Replace the replacement character with a space
                .replace('\u00A0', ' ')             // Replace non-breaking spaces with regular space
                .replaceAll("[^A-Za-z0-9 ]", "")     // Keep A-Z, a-z, 0-9, and spaces only
                .replaceAll("\\s{2,}", " ")          // Collapse multiple spaces into a single space
                .trim();
    }

    private String sanitizeNote(String note)
    {
        if (note == null)
        {
            return "";
        }

        return note
                .replace('\u00A0', ' ')   // Replace non-breaking space
                .replaceAll("\\p{C}", "") // Remove control characters
                .trim();
    }


    public void updateNote(String playerName, String note)
    {
        playerName = sanitize(playerName);
        note = sanitizeNote(note);

        if (note.isEmpty())
        {
            playerNotes.remove(playerName);
        }
        else
        {
            playerNotes.put(playerName, note);
        }

        saveNotes();
    }

}
