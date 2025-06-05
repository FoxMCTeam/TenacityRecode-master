package dev.tenacity.utils.client.addons.api;

import dev.tenacity.Tenacity;
import dev.tenacity.config.DragManager;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.module.settings.impl.*;
import dev.tenacity.utils.client.addons.api.bindings.*;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.misc.FileUtils;
import dev.tenacity.utils.objects.Dragging;
import netscape.javascript.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
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
    private final HashMap<String, JSObject> eventHashMap = new HashMap<>();
    private ScriptModule scriptModule;
    private boolean initializedSettings = false;
    @Setter
    private boolean reloadable = true;


    public Script(File file) throws ScriptException {
        this.file = file;

        final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        final ScriptEngine scriptEngine = factory.getScriptEngine(new ScriptFilter());
        final Bindings manager = new SimpleBindings();

        manager.put("notification", new NotificationBinding());
        manager.put("client", new ClientBinding());
        manager.put("player", new PlayerBinding());
        manager.put("packet", new PacketBinding());
        manager.put("render", new RenderBinding());
        manager.put("world", new WorldBinding());
        manager.put("user", new UserBinding());
        manager.put("font", new FontBinding());
        manager.put("potion", new PotionBinding());
        manager.put("initScript", new InitializeScript());
        manager.put("color", new ColorConstructor());
        manager.put("facing", new EnumFacingBinding());
        manager.put("action", new ActionBinding());
        manager.put("stats", new StatsBinding());
        manager.put("createDrag", new CreateDrag());

        scriptEngine.setBindings(manager, ScriptContext.GLOBAL_SCOPE);

        String scriptContent = FileUtils.readFile(file);

        scriptEngine.eval(scriptContent);

        if (name == null || author == null || description == null) {
            throw new ScriptException("Script is missing name, author, or description");
        }

        registerModule(name, description, author, file);

        //Eval the script a second time so that we can can create the Dragging objects.
        scriptEngine.eval(scriptContent);
    }

    
    public void overrideReload() {
        reloadable = false;
        scriptModule.setReloadable(false);
    }

    private void registerModule(String name, String description, String author, File file) {
        scriptModule = new ScriptModule(name, description, eventHashMap, author, file);
        settings.forEach(setting -> scriptModule.addSettings(setting));
        // Make this true so that we do not assign different instances of the settings in the javascript code when we eval the script for a second time.
        initializedSettings = true;
    }

    
    public BooleanSetting booleanSetting(String name, boolean initialValue) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof BooleanSetting).map(setting -> (BooleanSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }
        BooleanSetting booleanSetting = new BooleanSetting(name, initialValue);
        settings.add(booleanSetting);
        return booleanSetting;
    }

    
    public ModeSetting modeSetting(String name, String startMode, String... modes) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof ModeSetting).map(setting -> (ModeSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }
        ModeSetting modeSetting = new ModeSetting(name, startMode, modes);
        settings.add(modeSetting);
        return modeSetting;

    }

    
    public NumberSetting numberSetting(String name, float defaultValue, float minValue, float maxValue, float increment) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof NumberSetting).map(setting -> (NumberSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }
        NumberSetting numberSetting = new NumberSetting(name, defaultValue, maxValue, minValue, increment);
        settings.add(numberSetting);
        return numberSetting;
    }

    
    public ColorSetting colorSetting(String name, Color color) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof ColorSetting).map(setting -> (ColorSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }
        ColorSetting colorSetting = new ColorSetting(name, color);
        settings.add(colorSetting);
        return colorSetting;
    }

    
    public StringSetting stringSetting(String name, String string) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof StringSetting).map(setting -> (StringSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }
        StringSetting stringSetting = new StringSetting(name, string);
        settings.add(stringSetting);
        return stringSetting;
    }

    
    public MultipleBoolSetting multiBoolSetting(String name, String... options) {
        if (initializedSettings) {
            return scriptModule.getSettingsList().stream().filter(setting -> setting instanceof MultipleBoolSetting).map(setting -> (MultipleBoolSetting) setting)
                    .filter(setting -> setting.name.equals(name)).findFirst().orElse(null);
        }

        MultipleBoolSetting multiBoolSetting = new MultipleBoolSetting(name, options);
        settings.add(multiBoolSetting);
        return multiBoolSetting;
    }

    
    public void onAttack(JSObject handle) {
        eventHashMap.put("attack", handle);
    }

    
    public void onRenderModel(JSObject handle) {
        eventHashMap.put("renderModel", handle);
    }

    
    public void onRender2D(JSObject handle) {
        eventHashMap.put("render", handle);
    }

    
    public void onRender3D(JSObject handle) {
        eventHashMap.put("render3D", handle);
    }

    
    public void onMotion(JSObject handle) {
        eventHashMap.put("motion", handle);
    }

    
    public void onTick(JSObject handle) {
        eventHashMap.put("tick", handle);
    }

    
    public void onMove(JSObject handle) {
        eventHashMap.put("move", handle);
    }

    
    public void onPacketSend(JSObject handle) {
        eventHashMap.put("packetSend", handle);
    }

    
    public void onPacketReceive(JSObject handle) {
        eventHashMap.put("packetReceive", handle);
    }

    
    public void onShader(JSObject handle) {
        eventHashMap.put("shader", handle);
    }

    
    public void onChatReceived(JSObject handle) {
        eventHashMap.put("chat", handle);
    }

    
    public void onPlayerSendMessage(JSObject handle) {
        eventHashMap.put("playerMessage", handle);
    }

    
    public void onWorldLoad(JSObject handle) {
        eventHashMap.put("worldLoad", handle);
    }

    
    public void onSafeWalk(JSObject handle) {
        eventHashMap.put("safewalk", handle);
    }

    
    public void onCustomBlockRender(JSObject handle) {
        eventHashMap.put("customBlockRender", handle);
    }

    
    public void onEnable(JSObject handle) {
        eventHashMap.put("enable", handle);
    }

    
    public void onDisable(JSObject handle) {
        eventHashMap.put("disable", handle);
    }

    private class CreateDrag implements Function<JSObject, Dragging> {
        @Override
        public Dragging apply(JSObject jsObject) {
            int initialX = (int) jsObject.getMember("initialX");
            int initialY = (int) jsObject.getMember("initialY");
            if (mc.currentScreen instanceof GuiChat) {
                mc.displayGuiScreen(null);
            }
            Dragging dragging = Tenacity.INSTANCE.createDrag(scriptModule, "script" + description, initialX, initialY);
            DragManager.loadDragData();
            return dragging;
        }
    }

    private static class ColorConstructor implements Function<JSObject, Color> {
        @Override
        public Color apply(JSObject jsObject) {

            if (jsObject.hasMember("hex")) {
                return new Color((int) jsObject.getMember("hex"));
            }

            int red = (int) jsObject.getMember("red");
            int green = (int) jsObject.getMember("green");
            int blue = (int) jsObject.getMember("blue");
            int alpha = 255;
            if (jsObject.hasMember("alpha")) {
                alpha = (int) jsObject.getMember("alpha");
            }
            return new Color(red, green, blue, alpha);
        }
    }

    private class InitializeScript implements Function<JSObject, Script> {
        @Override
        public Script apply(JSObject jsObject) {
            name = (String) jsObject.getMember("name");
            description = (String) jsObject.getMember("description");
            author = (String) jsObject.getMember("author");
            return Script.this;
        }
    }
}
