package dev.tenacity.module.impl.misc;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.MultipleBoolSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.module.settings.impl.StringSetting;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.time.TimerUtil;

public final class Spammer extends Module {

    private final StringSetting text = new StringSetting("Text");
    private final NumberSetting delay = new NumberSetting("Delay", 100, 1000, 100, 1);
    private final MultipleBoolSetting settings = new MultipleBoolSetting("Settings",
            new BooleanSetting("AntiSpam", false),
            new BooleanSetting("Bypass", false));
    private final TimerUtil delayTimer = new TimerUtil();

    public Spammer() {
        super("module.misc.spammer", Category.MISC, "Spams in chat");
        this.addSettings(text, delay, settings);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        String spammerText = text.get();

        if (spammerText != null && delayTimer.hasTimeElapsed(settings.getSetting("Bypass").get() ? 2000 : delay.get().longValue())) {

            if (settings.getSetting("AntiSpam").get()) {
                spammerText += " " + MathUtils.getRandomInRange(10, 100000);
            }

            mc.thePlayer.sendChatMessage(spammerText);
            delayTimer.reset();
        }
    }

}
