package dev.tenacity.module.impl.player;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;

public final class FastPlace extends Module {

    private final NumberSetting ticks = new NumberSetting("Ticks", 0, 4, 0, 1);
    private final BooleanSetting blocks = new BooleanSetting("Blocks", true);
    private final BooleanSetting projectiles = new BooleanSetting("Projectiles", true);

    public FastPlace() {
        super("module.player.FastPlace", Category.PLAYER, "place blocks fast");
        this.addSettings(ticks, blocks, projectiles);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (canFastPlace()) {
            mc.rightClickDelayTimer = Math.min(mc.rightClickDelayTimer, ticks.get().intValue());
        }
    }

    @Override
    public void onDisable() {
        mc.rightClickDelayTimer = 4;
        super.onDisable();
    }

    private boolean canFastPlace() {
        if (mc.thePlayer == null || mc.thePlayer.getCurrentEquippedItem() == null || mc.thePlayer.getCurrentEquippedItem().getItem() == null)
            return false;
        Item heldItem = mc.thePlayer.getCurrentEquippedItem().getItem();
        return (blocks.get() && heldItem instanceof ItemBlock) || (projectiles.get() && (heldItem instanceof ItemSnowball || heldItem instanceof ItemEgg));
    }

}
