package dev.tenacity.module.impl.movement;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.network.PacketEvent;

import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.packet.BlinkComponent;
import dev.tenacity.utils.packet.CollectorType;
import dev.tenacity.utils.packet.PacketCollector;
import dev.tenacity.utils.time.TimerUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

import java.util.Arrays;
import java.util.List;

public final class InventoryMove extends Module {

    private static final List<KeyBinding> keys = Arrays.asList(
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump
    );
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Spoof", "Delay", "Watchdog");
    private final BooleanSetting blink = new BooleanSetting("Blink", false);
    private final TimerUtil delayTimer = new TimerUtil();
    private final PacketCollector packetCollector = new PacketCollector(0, 128);
    private boolean wasInContainer;

    public InventoryMove() {
        super("module.movement.InventoryMove", Category.MOVEMENT, "lets you move in your inventory");
        addSettings(mode, blink);
    }

    public static void updateStates() {
        if (mc.currentScreen != null) {
            keys.forEach(k -> KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k)));
        }
    }

    @EventTarget
    public void onMotionEvent(MotionEvent e) {
        setSuffix(mode.get());
        boolean inContainer = mc.currentScreen instanceof GuiContainer;
        if (wasInContainer && !inContainer) {
            wasInContainer = false;
            if (mode.is("Watchdog")) {
                BlinkComponent.stopBlink(CollectorType.NO_EVENT);
                packetCollector.releasePackets(CollectorType.NO_EVENT);
            }
            updateStates();
        }
        switch (mode.get()) {
            case "Spoof":
            case "Vanilla":
                if (inContainer) {
                    wasInContainer = true;
                    updateStates();
                }
                break;
            case "Delay":
                if (e.isPre() && inContainer) {
                    if (delayTimer.hasTimeElapsed(100)) {
                        wasInContainer = true;
                        updateStates();
                        delayTimer.reset();
                    }
                }
                break;
            case "Watchdog":
                if (inContainer) {
                    wasInContainer = true;
                    BlinkComponent.startBlink();
                    updateStates();
                }
                break;
        }
    }

    @EventTarget
    public void onPacketEvent(PacketEvent e) {
        if (mode.is("Spoof") && (e.getPacket() instanceof S2DPacketOpenWindow || e.getPacket() instanceof S2EPacketCloseWindow)) {
            e.cancel();
        }

        Packet<?> packet = e.getPacket();
        if (packet instanceof C0EPacketClickWindow && wasInContainer && mode.is("Watchdog")) {
            e.cancel();
            packetCollector.add(packet);
        }
        if (packet instanceof C0DPacketCloseWindow && mode.is("Watchdog")) {
            packetCollector.releasePackets(CollectorType.NO_EVENT);
        }
    }

}
