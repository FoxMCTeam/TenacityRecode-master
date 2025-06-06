package dev.tenacity.module;

import com.cubk.event.annotations.EventTarget;
import dev.tenacity.Client;
import dev.tenacity.config.DragManager;
import com.cubk.event.impl.game.GameCloseEvent;
import com.cubk.event.impl.game.KeyPressEvent;
import com.cubk.event.impl.game.TickEvent;
import com.cubk.event.impl.game.WorldEvent;
import com.cubk.event.impl.player.ChatReceivedEvent;
import com.cubk.event.impl.render.Render2DEvent;
import com.cubk.event.impl.render.ShaderEvent;
import dev.tenacity.module.impl.movement.Flight;
import dev.tenacity.module.impl.movement.Scaffold;
import dev.tenacity.module.impl.display.Statistics;
import dev.tenacity.ui.mainmenu.CustomMainMenu;
import dev.tenacity.utils.Utils;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.StringUtils;

import java.util.Arrays;

public class BackgroundProcess implements Utils {

    private final Scaffold scaffold = (Scaffold) Client.INSTANCE.getModuleManager().get(Scaffold.class);

    @EventTarget
    public void onKeyPressEvent(KeyPressEvent event) {
        // We should probably have a static arraylist of all the modules instead of creating a new on in getModules()
        for (Module module : Client.INSTANCE.getModuleManager().getModules()) {
            if (module.getKeybind().getCode() == event.getKey()) {
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
        if (event instanceof WorldEvent.Load) {
            Flight.hiddenBlocks.clear();
        }
    }

}
