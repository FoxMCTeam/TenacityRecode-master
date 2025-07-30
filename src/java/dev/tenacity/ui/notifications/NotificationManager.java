package dev.tenacity.ui.notifications;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.NotificationsMod;
import dev.tenacity.utils.render.DynamicIslandManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    @Getter
    private static final Deque<Notification> notifications = new ConcurrentLinkedDeque<>();
    @Getter
    @Setter
    private static float toggleTime = 2;

    public static void post(NotificationType type, String title, String description) {
        Client.INSTANCE.getDynamicIslandManager().addContent(trans(type), title, description, (long) toggleTime * 1000);
        post(new Notification(type, title, description));
    }

    public static void post(NotificationType type, String title, String description, float time) {
        Client.INSTANCE.getDynamicIslandManager().addContent(trans(type), title, description, (long) toggleTime * 1000);
        post(new Notification(type, title, description, time));
    }

    private static void post(Notification notification) {
        if (Client.INSTANCE.isEnabled(NotificationsMod.class)) {
            notifications.add(notification);
        }
    }

    private static DynamicIslandManager.ContentType trans(NotificationType type) {
        return switch (type) {
            case DISABLE, WARNING -> DynamicIslandManager.ContentType.WARNING;
            case INFO -> DynamicIslandManager.ContentType.INFO;
            case SUCCESS -> DynamicIslandManager.ContentType.SUCCESS;
        };
    }

}
