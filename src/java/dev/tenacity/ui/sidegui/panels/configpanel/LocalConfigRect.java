package dev.tenacity.ui.sidegui.panels.configpanel;

import dev.tenacity.Client;
import dev.tenacity.config.LocalConfig;
import dev.tenacity.ui.Screen;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.ui.sidegui.utils.CloudDataUtils;
import dev.tenacity.ui.sidegui.utils.IconButton;
import dev.tenacity.utils.font.FontUtil;
import dev.tenacity.utils.misc.FileUtils;
import dev.tenacity.utils.misc.IOUtils;
import dev.tenacity.utils.misc.Multithreading;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class LocalConfigRect implements Screen {
    private final List<IconButton> buttons = new ArrayList<>();
    private final LocalConfig config;
    private float x, y, width, height, alpha;
    private Color accentColor;
    private boolean clickable = true;
    @Getter
    private BasicFileAttributes bfa = null;

    public LocalConfigRect(LocalConfig config) {
        this.config = config;
        buttons.add(new IconButton(FontUtil.LOAD, "Load this config"));
        buttons.add(new IconButton(FontUtil.SAVE, "Update this config"));
        buttons.add(new IconButton(FontUtil.UPLOAD, "Upload this config"));
        buttons.add(new IconButton(FontUtil.TRASH, "Delete this config"));

        try {
            bfa = Files.readAttributes(config.getFile().toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);
        RoundedUtil.drawRound(x, y, width, height, 5, ColorUtil.tripleColor(37, alpha));
        duckSansBoldFont26.drawString(config.getName(), x + 3, y + 3, textColor);

        if (bfa != null) {
            duckSansFont16.drawString(CloudDataUtils.getLastEditedTime(String.valueOf(bfa.lastModifiedTime().toMillis() / 1000)),
                    x + 4, y + 2.5f + duckSansBoldFont32.getHeight(),
                    ColorUtil.applyOpacity(textColor, .5f));
        }

        int seperationX = 0;
        for (IconButton button : buttons) {
            button.setX(x + width - (button.getWidth() + 4 + seperationX));
            button.setY(y + height - (button.getHeight() + 4));
            button.setAlpha(alpha);
            button.setIconFont(iconFont20);
            if (FontUtil.TRASH.equals(button.getIcon())) {
                button.setAccentColor(new Color(209, 56, 56));
            } else {
                button.setAccentColor(accentColor);
            }

            button.setClickAction(() -> {
                switch (button.getIcon()) {
                    case FontUtil.TRASH:
                        IOUtils.deleteFile(config.getFile());
                        Client.INSTANCE.getSideGui().getTooltips().clear();
                        Client.INSTANCE.getSideGui().getConfigPanel().setRefresh(true);
                        break;
                    case FontUtil.LOAD:
                        Multithreading.runAsync(() -> {
                            String loadData = FileUtils.readFile(config.getFile());

                            if (Client.INSTANCE.getConfigManager().loadConfig(loadData, false)) {
                                NotificationManager.post(NotificationType.SUCCESS, "Success", "Config loaded successfully!");
                            } else {
                                NotificationManager.post(NotificationType.WARNING, "Error", "The online config did not load successfully!");
                            }
                        });
                        break;
                    case FontUtil.SAVE:
                        Multithreading.runAsync(() -> {
                            String saveData = Client.INSTANCE.getConfigManager().serialize();

                            if (Client.INSTANCE.getConfigManager().saveConfig(config.getName(), saveData)) {
                                NotificationManager.post(NotificationType.SUCCESS, "Success", "Config update successfully!");
                            } else {
                                NotificationManager.post(NotificationType.WARNING, "Error", "The config did not update successfully!");
                            }

                            Client.INSTANCE.getSideGui().getTooltips().clear();
                            Client.INSTANCE.getSideGui().getConfigPanel().setRefresh(true);
                        });
                        break;
                }
            });


            button.drawScreen(mouseX, mouseY);
            seperationX += 8 + button.getWidth();
        }


    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (clickable) {
            buttons.forEach(button1 -> button1.mouseClicked(mouseX, mouseY, button));
        }

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public String getCurrentTimeStamp(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
        return sdfDate.format(date);
    }
}
