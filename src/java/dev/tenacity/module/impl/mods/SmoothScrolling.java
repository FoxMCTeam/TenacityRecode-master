package dev.tenacity.module.impl.mods;

import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;

public class SmoothScrolling extends Module {
    public SmoothScrolling() {
        super("module.render.SmoothScrolling", Category.MODS, "description");
        if (!enabled) this.toggleSilent();
    }

    @Override
    public void onDisable() {
        setEnabled(true);
        NotificationManager.post(NotificationType.WARNING, Localization.get(getName()), "You Can't Disable!");

        super.onDisable();
    }
}
