package dev.tenacity.ui.notifications;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.NotificationsMod;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();
    @Getter
    @Setter
    private static float toggleTime = 2;

    public static void post(NotificationType type, String title, String description) {
        post(new Notification(type, title, description));
    }

    public static void post(NotificationType type, String title, String description, float time) {
        post(new Notification(type, title, description, time));
    }

    private static void post(Notification notification) {
        if (Client.INSTANCE.isEnabled(NotificationsMod.class)) {
            notifications.add(notification);
        }
    }

}
