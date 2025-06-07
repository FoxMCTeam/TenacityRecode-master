package dev.tenacity.utils.client.addons.rise.component;

import dev.tenacity.utils.Utils;
import lombok.Getter;
import net.minecraft.item.ItemStack;


public class RenderSlotComponent implements Utils {

    private static int spoofedSlot;

    @Getter
    private static boolean spoofing;

    public static int getSpoofedSlot() {
        return spoofing ? spoofedSlot : mc.thePlayer.inventory.currentItem;
    }

    public static ItemStack getSpoofedStack() {
        return spoofing ? mc.thePlayer.inventory.getStackInSlot(spoofedSlot) : mc.thePlayer.inventory.getCurrentItem();
    }

    public void startSpoofing(int slot) {
        spoofing = true;
        spoofedSlot = slot;
    }

    public void stopSpoofing() {
        spoofing = false;
    }
}
