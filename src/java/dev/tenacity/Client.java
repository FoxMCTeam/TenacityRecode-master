package dev.tenacity;

import dev.tenacity.commands.CommandHandler;
import dev.tenacity.config.ConfigManager;
import dev.tenacity.config.DragManager;
import dev.tenacity.event.EventProtocol;
import dev.tenacity.module.Module;
import dev.tenacity.module.ModuleCollection;
import dev.tenacity.utils.client.addons.api.ScriptManager;
import dev.tenacity.ui.altmanager.GuiAltManager;
import dev.tenacity.ui.searchbar.SearchBar;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.ReleaseType;
import dev.tenacity.utils.objects.Dragging;
import dev.tenacity.utils.objects.HTTPUtil;
import dev.tenacity.utils.render.WallpaperEngine;
import dev.tenacity.utils.server.PingerUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class Client implements Utils {

    public static final Client INSTANCE = new Client();

    public static final String NAME = "Tenacity";
    public static final String VERSION = "5.1";
    public static final String THANKS = "d3Ck, bzdhyp";
    public static final ReleaseType RELEASE = ReleaseType.DEV;
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final File DIRECTORY = new File(mc.mcDataDir, NAME);
    public static final File BACKGROUND = new File(DIRECTORY, "background");
    public static File backGroundFile;
    private final EventProtocol eventProtocol = new EventProtocol();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SideGUI sideGui = new SideGUI();
    private final SearchBar searchBar = new SearchBar();
    public WallpaperEngine videoRenderer;
    private ModuleCollection moduleCollection;
    private ScriptManager scriptManager;
    private ConfigManager configManager;
    private GuiAltManager altManager;
    private CommandHandler commandHandler;
    private PingerUtils pingerUtils;

    public static boolean updateGuiScale;
    public static int prevGuiScale;

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
        Module m = INSTANCE.moduleCollection.get(c);
        return m != null && m.isEnabled();
    }

    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }


    public void downloadBackGroundVideo() {
        LOGGER.info("Downloading background video");
        backGroundFile = new File(Client.BACKGROUND, "background.mp4");
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
        videoFile = new File(Client.BACKGROUND, "background.mp4");
        videoRenderer.setup(videoFile, 60);
    }


}
