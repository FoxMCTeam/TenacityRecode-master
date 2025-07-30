package dev.tenacity.module.impl.mods;

import dev.tenacity.Client;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;

/**
 * @author ChengFeng
 * @since 2024/8/8
 **/
public class Camera extends Module {
    public static final BooleanSetting clip = new BooleanSetting("Clip", false);
    public static final BooleanSetting animation = new BooleanSetting("Animation", true);
    public static final BooleanSetting motion = new BooleanSetting("Motion", false);
    public static final NumberSetting interpolation = new NumberSetting("MotionInterpolation", 0.15f, 0.5f, 0.05f, 0.05f);
    public static final BooleanSetting transform = new BooleanSetting("Transform", false);
    public static final NumberSetting x = new NumberSetting("TransformX", 0f, 5f, -5f, 0.1f);
    public static final NumberSetting y = new NumberSetting("TransformY", 0f, 5f, 0f, 0.1f);
    public static final NumberSetting z = new NumberSetting("TransformZ", 0f, 5f, -5f, 0.1f);

    public Camera() {
        super("module.render.MotionCamera", Category.MODS, "motion camera");
        if (Client.INSTANCE.loaded) {
            mc.entityRenderer.prevRenderX = mc.getRenderViewEntity().posX;
            mc.entityRenderer.prevRenderY = mc.getRenderViewEntity().posY + mc.getRenderViewEntity().getEyeHeight();
            mc.entityRenderer.prevRenderZ = mc.getRenderViewEntity().posZ;
        }
        addSettings(clip, animation, motion, interpolation, transform, x, y, z);
        if (!enabled) this.toggleSilent();
    }

    @Override
    public void onDisable() {
        setEnabled(true);
        NotificationManager.post(NotificationType.WARNING, Localization.get(getName()), "You Can't Disable!");
        super.onDisable();
    }
}
