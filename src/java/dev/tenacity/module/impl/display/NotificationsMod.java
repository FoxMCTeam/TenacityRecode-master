package dev.tenacity.module.impl.display;

import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.notifications.Notification;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationsMod extends Module {
    public static final ModeSetting mode = new ModeSetting("Mode", "Default", "Default", "SuicideX", "Exhibition", "Modern Exhibition", "Dynamic Island");
    public static final BooleanSetting onlyTitle = new BooleanSetting("Only Title", false);
    public static final BooleanSetting toggleNotifications = new BooleanSetting("Show Toggle", true);
    private final NumberSetting time = new NumberSetting("Time on Screen", 2, 10, 1, .5);

    public NotificationsMod() {
        super("module.display.notifications", Category.DISPLAY, "Allows you to customize the client notifications");
        onlyTitle.addParent(mode, modeSetting -> modeSetting.is("Default"));
        this.addSettings(time, mode, onlyTitle, toggleNotifications);
        if (!enabled) this.toggleSilent();
    }
    
    public void render() {
        float yOffset = 0;
        int notificationHeight = 0;
        int notificationWidth;
        int actualOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);

        NotificationManager.setToggleTime(time.get().floatValue());

        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;

            switch (mode.get()) {
                case "Default":
                    animation.setDuration(250);
                    actualOffset = 8;
                    if (onlyTitle.get()) {
                        notificationHeight = 19;
                        notificationWidth = (int) duckSansBoldFont22.getStringWidth(notification.getTitle()) + 35;
                    } else {
                        notificationHeight = 28;
                        notificationWidth = (int) Math.max(duckSansBoldFont22.getStringWidth(notification.getTitle()), duckSansFont18.getStringWidth(notification.getDescription())) + 35;
                    }

                    x = sr.getScaledWidth() - (notificationWidth + 5) * animation.getOutput().floatValue();
                    y = sr.getScaledHeight() - (yOffset + 18 + HUDMod.offsetValue + notificationHeight + (15 * GuiChat.openingAnimation.getOutput().floatValue()));

                    notification.drawDefault(x, y, notificationWidth, notificationHeight, animation.getOutput().floatValue(), onlyTitle.get());
                    break;
                case "SuicideX":
                    animation.setDuration(200);
                    actualOffset = 3;
                    notificationHeight = 16;
                    String editTitle = notification.getTitle() + (notification.getTitle().endsWith(".") || notification.getTitle().endsWith("/") ? " " : ". ") + notification.getDescription();

                    notificationWidth = (int) duckSansBoldFont22.getStringWidth(editTitle) + 5;

                    x = sr.getScaledWidth() - (notificationWidth + 5);
                    y = sr.getScaledHeight() - (yOffset + 18 + HUDMod.offsetValue + notificationHeight + (15 * GuiChat.openingAnimation.getOutput().floatValue()));

                    notification.drawSuicideX(x, y, notificationWidth, notificationHeight, animation.getOutput().floatValue());
                    break;
                case "Exhibition":
                    animation.setDuration(125);
                    actualOffset = 3;
                    notificationHeight = 25;
                    notificationWidth = (int) Math.max(tahomaFont.size(18).getStringWidth(notification.getTitle()), tahomaFont.size(14).getStringWidth(notification.getDescription())) + 30;

                    x = sr.getScaledWidth() - ((sr.getScaledWidth() / 2f + notificationWidth / 2f) * animation.getOutput().floatValue());
                    y = sr.getScaledHeight() / 2f - notificationHeight / 2f + 40 + yOffset;

                    notification.drawExhi(x, y, notificationWidth, notificationHeight);
                    break;

                case "Modern Exhibition":
                    animation.setDuration(177);
                    actualOffset = 3;
                    notificationHeight = 25;
                    notificationWidth = (int) Math.max(tahomaFont.size(18).getStringWidth(notification.getTitle()), tahomaFont.size(14).getStringWidth(notification.getDescription())) + 30;

                    x = sr.getScaledWidth() - ((sr.getScaledWidth() / 2f + notificationWidth / 2f) * animation.getOutput().floatValue());
                    y = sr.getScaledHeight() / 2f - notificationHeight / 2f + 40 + yOffset;

                    notification.drawExhiModern(x, y, notificationWidth, notificationHeight);
                    break;
            }


            yOffset += (notificationHeight + actualOffset) * animation.getOutput().floatValue();

        }
    }

    public void renderEffects(boolean glow) {
        float yOffset = 0;
        int notificationHeight = 0;
        int notificationWidth;
        int actualOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);


        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;

            switch (mode.get()) {
                case "Default":
                    actualOffset = 8;
                    if (onlyTitle.get()) {
                        notificationHeight = 19;
                        notificationWidth = (int) duckSansBoldFont22.getStringWidth(notification.getTitle()) + 35;
                    } else {
                        notificationHeight = 28;
                        notificationWidth = (int) Math.max(duckSansBoldFont22.getStringWidth(notification.getTitle()), duckSansFont18.getStringWidth(notification.getDescription())) + 35;
                    }

                    x = sr.getScaledWidth() - (notificationWidth + 5) * animation.getOutput().floatValue();
                    y = sr.getScaledHeight() - (yOffset + 18 + HUDMod.offsetValue + notificationHeight + (15 * GuiChat.openingAnimation.getOutput().floatValue()));

                    notification.blurDefault(x, y, notificationWidth, notificationHeight, animation.getOutput().floatValue(), glow);
                    break;
                case "SuicideX":
                    actualOffset = 3;
                    notificationHeight = 16;
                    String editTitle = notification.getTitle() + (notification.getTitle().endsWith(".") || notification.getTitle().endsWith("/") ? " " : ". ") + notification.getDescription();

                    notificationWidth = (int) duckSansBoldFont22.getStringWidth(editTitle) + 5;

                    x = sr.getScaledWidth() - (notificationWidth + 5);
                    y = sr.getScaledHeight() - (yOffset + 18 + HUDMod.offsetValue + notificationHeight + (15 * GuiChat.openingAnimation.getOutput().floatValue()));

                    notification.blurSuicideX(x, y, notificationWidth, notificationHeight, animation.getOutput().floatValue());
                    break;
                case "Exhibition":
                    actualOffset = 3;
                    notificationHeight = 25;
                    notificationWidth = (int) Math.max(tahomaFont.size(18).getStringWidth(notification.getTitle()), tahomaFont.size(14).getStringWidth(notification.getDescription())) + 30;

                    x = sr.getScaledWidth() - ((sr.getScaledWidth() / 2f + notificationWidth / 2f) * animation.getOutput().floatValue());
                    y = sr.getScaledHeight() / 2f - notificationHeight / 2f + 40 + yOffset;

                    notification.blurExhi(x, y, notificationWidth, notificationHeight);
                    break;

                case "Modern Exhibition":
                    actualOffset = 3;
                    notificationHeight = 25;
                    notificationWidth = (int) Math.max(tahomaFont.size(18).getStringWidth(notification.getTitle()), tahomaFont.size(14).getStringWidth(notification.getDescription())) + 30;

                    x = sr.getScaledWidth() - ((sr.getScaledWidth() / 2f + notificationWidth / 2f) * animation.getOutput().floatValue());
                    y = sr.getScaledHeight() / 2f - notificationHeight / 2f + 40 + yOffset;

                    notification.blurExhiModern(x, y, notificationWidth, notificationHeight);
                    break;
            }


            yOffset += (notificationHeight + actualOffset) * animation.getOutput().floatValue();

        }
    }


}
