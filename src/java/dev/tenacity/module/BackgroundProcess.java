package dev.tenacity.module;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.GameCloseEvent;
import dev.tenacity.event.impl.game.KeyPressEvent;
import dev.tenacity.event.impl.game.TickEvent;
import dev.tenacity.event.impl.game.WorldEvent;
import dev.tenacity.event.impl.player.ChatReceivedEvent;
import dev.tenacity.event.impl.player.UpdateEvent;
import dev.tenacity.event.impl.render.Render2DEvent;
import dev.tenacity.event.impl.render.ShaderEvent;
import dev.tenacity.Client;
import dev.tenacity.config.DragManager;
import dev.tenacity.i18n.Locale;
import dev.tenacity.module.impl.display.SettingComponent;
import dev.tenacity.module.impl.display.Statistics;
import dev.tenacity.module.impl.movement.Flight;
import dev.tenacity.module.impl.movement.Scaffold;
import dev.tenacity.ui.mainmenu.CustomMainMenu;
import dev.tenacity.utils.Utils;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.StringUtils;

import java.util.Arrays;

import static dev.tenacity.module.impl.display.HUDMod.language;

public class BackgroundProcess implements Utils {

    private final Scaffold scaffold = (Scaffold) Client.INSTANCE.getModuleManager().get(Scaffold.class);

    @EventTarget
    public void onKeyPressEvent(KeyPressEvent event) {
        // We should probably have a static arraylist of all the modules instead of creating a new on in getModules()
        for (Module module : Client.INSTANCE.getModuleManager().getModules()) {
            if (module.getKeybind().get() == event.getKey()) {
                module.toggle();
            }
        }
    }

    @EventTarget
    public void onGameCloseEvent(GameCloseEvent event) {
        Client.INSTANCE.getConfigManager().saveDefaultConfig();
        DragManager.saveDragData();
    }

    @EventTarget
    public void onChatReceivedEvent(ChatReceivedEvent event) {
        if (mc.thePlayer == null) return;
        String message = event.message.getUnformattedText(), strippedMessage = StringUtils.stripControlCodes(message);
        String messageStr = event.message.toString();
        if (!strippedMessage.contains(":") && Arrays.stream(Statistics.KILL_TRIGGERS).anyMatch(strippedMessage.replace(mc.thePlayer.getName(), "*")::contains)) {
            Statistics.killCount++;
        } else if (messageStr.contains("ClickEvent{action=RUN_COMMAND, value='/play ") || messageStr.contains("Want to play again?")) {
            Statistics.gamesPlayed++;
            if (messageStr.contains("You died!")) {
                Statistics.deathCount++;
            }
        }
    }

    @EventTarget
    public void onTickEvent(TickEvent event) {
        if (Statistics.endTime == -1 && ((!mc.isSingleplayer() && mc.getCurrentServerData() == null) || mc.currentScreen instanceof CustomMainMenu || mc.currentScreen instanceof GuiMultiplayer || mc.currentScreen instanceof GuiDisconnected)) {
            Statistics.endTime = System.currentTimeMillis();
        } else if (Statistics.endTime != -1 && (mc.isSingleplayer() || mc.getCurrentServerData() != null)) {
            Statistics.reset();
        }
    }

    @EventTarget
    public void onShaderEvent(ShaderEvent event) {
        if (mc.thePlayer != null) {
            scaffold.renderCounterBlur();
        }
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        if (mc.thePlayer != null) {
            scaffold.renderCounter();
        }
    }

    @EventTarget
    public void onWorldEvent(WorldEvent event) {
        Flight.hiddenBlocks.clear();
    }


    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        switch (language.get()) {
            case "en_US" : Client.INSTANCE.setLocale(Locale.EN_US); break;
            case "ru_RU" : Client.INSTANCE.setLocale(Locale.RU_RU); break;
            case "zh_HK" : Client.INSTANCE.setLocale(Locale.ZH_HK); break;
            case "zh_CN" : Client.INSTANCE.setLocale(Locale.ZH_CN); break;
            case "de_DE" : Client.INSTANCE.setLocale(Locale.DE_DE); break;
            case "fr_fR" : Client.INSTANCE.setLocale(Locale.FR_FR); break;
        }
        if (SettingComponent.reload.get()) {
            Client.initClient();
            SettingComponent.reload.set(false);
        }
    }

}
