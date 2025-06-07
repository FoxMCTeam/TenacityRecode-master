package dev.tenacity.utils.client.addons.rise.component;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.player.MotionEvent;
import com.cubk.event.impl.player.SyncCurrentItemEvent;
import dev.tenacity.utils.Utils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public final class SlotComponent implements Utils {
    public static void setSlot(final int slot, final boolean render) {
        if (slot < 0 || slot > 8) {
            return;
        }
        mc.thePlayer.inventory.alternativeCurrentItem = slot;
        mc.thePlayer.inventory.alternativeSlot = true;
    }

    public static void setSlot(final int slot) {
        setSlot(slot, true);
    }

    public static ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    public static int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.alternativeSlot ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem;
    }

    @EventTarget
    public void onSync(SyncCurrentItemEvent event) {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;

        event.setSlot(inventoryPlayer.alternativeSlot ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent e) {
        if (e.isPre()) {
            mc.thePlayer.inventory.alternativeSlot = false;
        }
    }
}