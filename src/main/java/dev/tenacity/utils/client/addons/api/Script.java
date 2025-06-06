package dev.tenacity.utils.client.addons.api;

import dev.tenacity.Tenacity;
import dev.tenacity.config.DragManager;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.module.settings.impl.*;
import dev.tenacity.utils.client.addons.api.bindings.*;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.misc.FileUtils;
import dev.tenacity.utils.objects.Dragging;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiChat;

import javax.script.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

@Getter
public class Script implements Utils {

    private final ArrayList<Setting> settings = new ArrayList<>();
    private String name, author, description;
    private final File file;
    private final HashMap<String, Invocable> eventHashMap = new HashMap<>();
    private ScriptModule scriptModule;
    private boolean initializedSettings = false;
    @Setter
    private boolean reloadable = true;

    private ScriptEngine engine;

    public Script(File file) throws ScriptException {
        this.file = file;

    }
}
