package dev.tenacity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.tenacity.Client;
import dev.tenacity.utils.objects.Dragging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

public class DragManager {
    private static final File DRAG_DATA = new File(Client.DIRECTORY, "Drag.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().setLenient().create();
    public static HashMap<String, Dragging> draggables = new HashMap<>();

    public static void saveDragData() {
        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
        }
        try {
            Files.write(DRAG_DATA.toPath(), GSON.toJson(draggables.values()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            ex.printStackTrace();
            Client.LOGGER.error("Failed to save draggables");
        }
    }

    public static void loadDragData() {
        if (!DRAG_DATA.exists()) {
            Client.LOGGER.error("No drag data found");
            return;
        }

        Dragging[] draggings;
        try {
            draggings = GSON.fromJson(Files.readString(DRAG_DATA.toPath()), Dragging[].class);
        } catch (IOException ex) {
            ex.printStackTrace();
            Client.LOGGER.error("Failed to load draggables");
            return;
        }

        for (Dragging dragging : draggings) {
            if (!draggables.containsKey(dragging.getName())) continue;
            Dragging currentDrag = draggables.get(dragging.getName());
            currentDrag.setX(dragging.getX());
            currentDrag.setY(dragging.getY());
            draggables.put(dragging.getName(), currentDrag);
        }
    }

}
