package dev.tenacity.module.impl.player;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.player.SlowDownEvent;
import dev.tenacity.event.impl.player.UpdateEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.player.MovementUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;

public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Vanilla", "Watchdog");
    private boolean onSlab;

    public NoSlow() {
        super("module.player.NoSlow", Category.PLAYER, "prevent item slowdown");
        this.addSettings(mode);
    }

    @EventTarget
    public void onSlowDownEvent(SlowDownEvent event) {
        event.cancel();
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (event.isPost()) return;

        if (mc.thePlayer.isUsingItem() && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            if (mc.thePlayer.onGround && !onSlab) {
                event.setY(event.getY() + 1E-3);
            }
        }

        double y = event.getY();

        if (MovementUtils.getBlockAt(0.0, mc.thePlayer.posY, 0.0) != Blocks.stone_slab
                && MovementUtils.getBlockAt(0.0, mc.thePlayer.posY, 0.0) != Blocks.stone_stairs
                && !mc.thePlayer.isUsingItem()) {
            onSlab = false;
        }

        if (Math.abs(y - Math.round(y)) > 0.03 && mc.thePlayer.onGround) {
            onSlab = true;
        }
    }

    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        setSuffix(mode.get());

        if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem() == null) return;

        if (mode.is("Watchdog")) {
            if (isHoldingConsumable() && !onSlab) {
                if (mc.thePlayer.offGroundTicks == 2) {
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
                        mc.gameSettings.keyBindUseItem.pressed = true;
                    }
                }
            }

            if (mc.thePlayer.isUsingItem() && isHoldingConsumable(mc.thePlayer.getItemInUse()) && !onSlab) {
                if (mc.thePlayer.getItemInUseDuration() == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionX *= 0.2;
                    mc.thePlayer.motionZ *= 0.2;
                    mc.gameSettings.keyBindUseItem.pressed = false;
                }

                if (mc.thePlayer.offGroundTicks == 2) {
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
                        mc.gameSettings.keyBindUseItem.pressed = true;
                    }
                }
            }
        }
    }

    public boolean isHoldingConsumable() {
        return isHoldingConsumable(mc.thePlayer.getHeldItem());
    }

    public boolean isHoldingConsumable(ItemStack item) {
        return item != null && !(item.getItem() instanceof ItemSword)
                && (item.getItem() instanceof ItemFood
                || item.getItem() instanceof ItemPotion
                || item.getItem() instanceof ItemBow
                || item.getItem() instanceof ItemSkull
                || item.getItem() instanceof ItemBucketMilk);
    }
}
