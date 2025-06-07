package dev.tenacity;

import com.cubk.event.EventManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import dev.tenacity.commands.CommandHandler;
import dev.tenacity.commands.impl.*;
import dev.tenacity.config.ConfigManager;
import dev.tenacity.config.DragManager;
import dev.tenacity.i18n.Locale;
import dev.tenacity.module.BackgroundProcess;
import dev.tenacity.module.Module;
import dev.tenacity.module.ModuleManager;
import dev.tenacity.ui.altmanager.GuiAltManager;
import dev.tenacity.ui.searchbar.SearchBar;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.ReleaseType;
import dev.tenacity.utils.client.addons.api.ScriptManager;
import dev.tenacity.utils.client.addons.rise.component.RenderSlotComponent;
import dev.tenacity.utils.client.addons.rise.component.RotationComponent;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.ViaMCP;
import dev.tenacity.utils.objects.Dragging;
import dev.tenacity.utils.objects.HTTPUtil;
import dev.tenacity.utils.render.Theme;
import dev.tenacity.utils.render.WallpaperEngine;
import dev.tenacity.utils.server.PingerUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.tenacity.module.impl.display.HUDMod.language;

@Getter
@Setter
public class Client implements Utils {
    public static final Client INSTANCE = new Client();
    public static final String NAME = "Tenacity";
    public static final String VERSION = "5.2";
    public static final String THANKS = "d3Ck, bzdhyp";
    public static String userName = "d3Ck";
    public static final ReleaseType RELEASE = ReleaseType.DEV;
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final File DIRECTORY = new File(mc.mcDataDir, NAME);
    public static final File BACKGROUND = new File(DIRECTORY, "background");
    public static File backGroundFile;
    public static boolean updateGuiScale;
    public static int prevGuiScale;
    private final EventManager eventManager = new EventManager();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SideGUI sideGui = new SideGUI();
    private final SearchBar searchBar = new SearchBar();
    public WallpaperEngine videoRenderer;
    private ModuleManager moduleManager;
    private ScriptManager scriptManager;
    private ConfigManager configManager;
    private GuiAltManager altManager;
    private CommandHandler commandHandler;
    private PingerUtils pingerUtils;
    private Locale locale = Locale.EN_US;
    public static void initClient() {

        INSTANCE.setModuleManager(new ModuleManager());

        INSTANCE.getModuleManager().init();
        Theme.init();

        INSTANCE.setPingerUtils(new PingerUtils());

        INSTANCE.setScriptManager(new ScriptManager());

        CommandHandler commandHandler = new CommandHandler();
        commandHandler.commands.addAll(Arrays.asList(
                new FriendCommand(), new CopyNameCommand(), new BindCommand(), new UnbindCommand(),
                new ScriptCommand(), new SettingCommand(), new HelpCommand(),
                new VClipCommand(), new ClearBindsCommand(), new ClearConfigCommand(),
                new ToggleCommand()
        ));
        INSTANCE.setCommandHandler(commandHandler);
        executorService.execute(() -> INSTANCE.getEventManager().register(new BackgroundProcess()));
        executorService.execute(() -> INSTANCE.getEventManager().register(new RotationComponent()));
        executorService.execute(() -> INSTANCE.getEventManager().register(new RenderSlotComponent()));
        INSTANCE.setConfigManager(new ConfigManager());
        ConfigManager.defaultConfig = new File(Minecraft.getMinecraft().mcDataDir + "/Tenacity/Config.json");
        INSTANCE.getConfigManager().collectConfigs();
        if (ConfigManager.defaultConfig.exists()) {
            INSTANCE.getConfigManager().loadConfig(INSTANCE.getConfigManager().readConfigData(ConfigManager.defaultConfig.toPath()), true);
        }

        DragManager.loadDragData();

        INSTANCE.setAltManager(new GuiAltManager());
        LOGGER.info("Trying download Background Video");
        INSTANCE.downloadBackGroundVideo();

        LOGGER.info("Initializing background...");
        INSTANCE.initVideoBackground();
        try {
            LOGGER.info("Starting ViaMCP...");
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (language.getMode()) {
            case "en_US" : Client.INSTANCE.setLocale(Locale.EN_US); break;
            case "ru_RU" : Client.INSTANCE.setLocale(Locale.RU_RU); break;
            case "zh_HK" : Client.INSTANCE.setLocale(Locale.ZH_HK); break;
            case "zh_CN" : Client.INSTANCE.setLocale(Locale.ZH_CN); break;
            case "de_DE" : Client.INSTANCE.setLocale(Locale.DE_DE); break;
            case "fr_fR" : Client.INSTANCE.setLocale(Locale.FR_FR); break;
        }
    }

    public String getVersion() {
        return VERSION + (RELEASE != ReleaseType.PUBLIC ? " (" + RELEASE.getName() + ")" : "");
    }

    public final Color getClientColor() {
        return new Color(236, 133, 209);
    }

    public final Color getAlternateClientColor() {
        return new Color(28, 167, 222);
    }

    public boolean isEnabled(Class<? extends Module> c) {
        Module m = INSTANCE.moduleManager.get(c);
        return m != null && m.isEnabled();
    }

    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    public void downloadBackGroundVideo() {
        LOGGER.info("Downloading background video");
        backGroundFile = new File(BACKGROUND, "background.mp4");
        if (!backGroundFile.exists()) {
            try {
                if (backGroundFile.getParentFile().mkdirs()) {
                    backGroundFile.createNewFile();
                    backGroundFile.mkdir();
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
            HTTPUtil.download("https://api.nightmaple.lol/8Dzf2af/background.mp4", backGroundFile);
        }
    }

    public void initVideoBackground() {
        if (videoRenderer != null) {
            videoRenderer.close();
        }
        videoRenderer = new WallpaperEngine();
        File videoFile;
        videoFile = new File(BACKGROUND, "background.mp4");
        videoRenderer.setup(videoFile, 60);
    }
}
