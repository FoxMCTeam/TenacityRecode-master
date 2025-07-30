package dev.tenacity.utils.render.island;

import dev.tenacity.Client;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.impl.combat.KillAura;
import dev.tenacity.module.impl.display.DynamicIsland;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.impl.display.SettingComponent;
import dev.tenacity.module.impl.exploit.Disabler;
import dev.tenacity.module.impl.movement.Scaffold;
import dev.tenacity.module.impl.movement.Speed;
import dev.tenacity.module.impl.player.ChestStealer;
import dev.tenacity.ui.notifications.Notification;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.animations.ContinualAnimation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.font.FontUtil;
import dev.tenacity.utils.player.ScaffoldUtils;
import dev.tenacity.utils.render.GradientUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.RoundedUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;

@Getter
@Setter
public class IslandRenderer implements Utils {

    public static IslandRenderer INSTANCE = new IslandRenderer();
    public ContinualAnimation animatedX = new ContinualAnimation();
    public ContinualAnimation animatedY = new ContinualAnimation();
    public float x, y, width, height;
    public String title, description;
    private ScaledResolution sr;

    public IslandRenderer() {
        this.sr = new ScaledResolution(mc);
        if (mc.theWorld == null) {
            resetDisplay();
        }
    }

    public void render(ScaledResolution sr, boolean shader) {
        Date currentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日EEE");
        String formattedDate = dateFormat.format(currentDate);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String formattedTime = timeFormat.format(currentDate);
        this.sr = sr;

        if (mc.theWorld == null) {
            resetDisplay();
            return;
        }

        // Gapple
        if (Client.INSTANCE.getModuleManager().getModule(Disabler.class).isEnabled()
                && Client.INSTANCE.getModuleManager().getModule(Disabler.class).s08PacketTickCount > 0
                && DynamicIsland.disabler.isEnabled()) {

            renderDisablerProcessInfo(shader);
            return;
        }

        // Gapple
        //if (Client.INSTANCE.getModuleManager().getModule(Gapple.class).isEnabled()
        //        && Client.INSTANCE.getModuleManager().getModule(Gapple.class).getMovingPackets() > 0
        //        && DynamicIsland.gapple.isEnabled()) {
//
        //    renderGappleInfo(shader);
        //    return;
        //}

        // Speed
        if (Client.INSTANCE.getModuleManager().getModule(Speed.class).isEnabled() && DynamicIsland.speed.isEnabled()) {
            renderSpeedInfo(shader);
            return;
        }

        // ChestStealer
        //if (ChestStealer.canRender && DynamicIsland.chestStealer.isEnabled()) {
        //    String str = switch (SettingComponent.language.getConfigValue()) {
        //        case "简体中文" -> "正在偷取";
        //        case "Русский" -> "Кража в процессе";
        //        case "繁體中文" -> "偷取中";
        //        case "日本語" -> "盗み中";
        //        case "한국인" -> "훔치는 중";
        //        default -> "Stealing";
        //    };
        //    NotificationManager.post(NotificationType.INFO, Client.INSTANCE.moduleManager.getModule(ChestStealer.class).getName(), str, 0.5F);
        //    return;
        //}

        // KillAura
        if (KillAura.target != null
                //&& !Client.INSTANCE.getModuleManager().getModule(KillAura.class).isGapple()
                && DynamicIsland.targetHUD.isEnabled()) {

            renderTargetInfo(shader);
            return;
        }

        // Scaffold
        if ((Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())
                && (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).getBlockCount() > 0 || ScaffoldUtils.getBlockCount() > 0)
                && DynamicIsland.scaffold.isEnabled()) {

            renderScaffoldInfo(shader);
            return;
        }

        // 通知系统
        Deque<Notification> notifications = NotificationManager.getNotifications();
        if (!notifications.isEmpty() && DynamicIsland.notification.isEnabled()) {
            Notification notification = notifications.getLast();
            if (!notification.getAnimation().finished(Direction.BACKWARDS)) {
                renderNotification(notification, shader);
                return;
            }
        }

        // 默认持久信息
        renderPersistentInfo(shader);
    }

    /* 具体渲染方法实现 */
    private void renderTargetInfo(boolean shader) {
        title = KillAura.target.getName();
        int health = (int) KillAura.target.getHealth();
        description = "HP " + getHealthColorTag(health) + health;
        setupDisplayMetrics(20, health, shader);
    }

    private void renderDisablerProcessInfo(boolean shader) {
        String str = Localization.get("module.disabler.Process.title");//"Disabler Process"
        String str2 = Localization.get("module.disabler.Process.description");//Process
        title = str;
        int count = Client.INSTANCE.getModuleManager().getModule(Disabler.class).s08PacketTickCount;
        description = str2 + ": " + getColorTag(count, 15) + count;
        setupDisplayMetrics(20, count, shader);
    }

    //private void renderGappleInfo(boolean shader) {//Gapple Counter
    //    String str = Localization.get("module.gapple.title");//"Gapple Counter"
    //    String str2 = Localization.get("stacksize.description");//Stack Size
    //    title = str;
    //    int count = Client.INSTANCE.getModuleManager().getModule(Gapple.class).getMovingPackets();
    //    description = str2 + ": " + getColorTag(count, 20) + count;
    //    setupDisplayMetrics(32, count, shader);
    //}

    private void renderScaffoldInfo(boolean shader) {
        String str1 = Localization.get("module.scaffold.title");//"Block Counter"
        String str2 = Localization.get("stacksize.description");//Stack Size
        title = str1;
        int count = Client.INSTANCE.getModuleManager().getModule(Scaffold.class).getBlockCount();
        description = str2 + ": " + getColorTag(count, 64) + count;
        setupDisplayMetrics(64, count, shader);
    }

    private void renderNotification(Notification notification, boolean shader) {
        title = notification.getTitle();
        description = notification.getDescription();
        width = Math.max(duckSansFont16.getStringWidth(description),
                duckSansFont20.getStringWidth(title) + 10) + 10;
        height = 30;
        x = sr.getScaledWidth() / 2f;
        y = 20;

        runToXy(x, y);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawBackgroundAuto(1, shader);

        float progress = Math.min((notification.getTimerUtil().getTime() - 0.5F) / (notification.getTime() - 0.5F), 1);
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2),
                width - 12, 5f, 2.5f, new Color(255, 255, 255, 80));
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2),
                (width - 12) * progress, 5f, 2.5f, new Color(255, 255, 255, 255));

        if (!shader) {
            duckSansFont20.drawString(title, animatedX.getOutput() + 5, animatedY.getOutput() + 6, -1);
            duckSansFont16.drawString(description, animatedX.getOutput() + 5, animatedY.getOutput() + 18, -1);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderPersistentInfo(boolean shader) {
        String fps =  Localization.get("fps");//"FPS"
   

        title = (DynamicIsland.textMode.isEnabled("Client Name") ? SettingComponent.clientName.getString() : "")
                + EnumChatFormatting.WHITE + (DynamicIsland.textMode.isEnabled("Client Name") || DynamicIsland.textMode.isEnabled("Logo") ? " | " : "")
                + (DynamicIsland.textMode.isEnabled("User Name") ? Client.userName + " | " : "")
                + Minecraft.getDebugFPS() + " " + fps;
        width = duckSansBoldFont18.getStringWidth(title) + 5 + height;
        height = 15;
        x = sr.getScaledWidth() / 2f;
        y = 20;

        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        drawBackgroundAuto(0, shader);
        if (DynamicIsland.textMode.isEnabled("Logo")) {
            GradientUtil.applyGradientCornerLR(animatedX.getOutput(), animatedY.getOutput(), height, height, 0.5F, HUDMod.colorWheel.getColor1(), HUDMod.colorWheel.getColor2(), () -> {
                RenderUtil.drawImage(new ResourceLocation("Maple/Icons/logo-nocolor.png"), animatedX.getOutput() + 2.5F, animatedY.getOutput() + 0.5F, height - 1, height - 1);
            });
        }
        if (!shader) {
            duckSansBoldFont18.drawString(title, animatedX.getOutput() + 2.5F + height - 1, animatedY.getOutput() + 5, HUDMod.color(1));
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private void renderSpeedInfo(boolean shader) {
        String str = Localization.get("bps");//Block/s
        title = calculateBPS() + str;
        width = duckSansBoldFont18.getStringWidth(title) + 10;
        height = 15;
        x = sr.getScaledWidth() / 2f;
        y = 20;

        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawBackgroundAuto(0, shader);

        if (!shader) {
            duckSansBoldFont18.drawString(title, animatedX.getOutput() + 5, animatedY.getOutput() + 5, Color.WHITE);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    /* 工具方法 */
    private void resetDisplay() {
        x = sr.getScaledWidth() / 2f;
        y = 20;
        width = 0;
        height = 0;
        title = "";
    }

    private void setupDisplayMetrics(int maxValue, int currentValue, boolean shader) {
        width = Math.max(duckSansFont16.getStringWidth(description),
                duckSansFont20.getStringWidth(title) + 10) + 10;
        height = 30;
        x = sr.getScaledWidth() / 2f;
        y = 20;

        runToXy(x, y);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawBackgroundAuto(1, shader);

        float progress = (float) Math.min(currentValue, maxValue) / maxValue;
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2),
                width - 12, 5f, 2.5f, new Color(255, 255, 255, 80));
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2),
                (width - 12) * progress, 5f, 2.5f, new Color(255, 255, 255, 255));

        if (!shader) {
            duckSansFont20.drawString(title, animatedX.getOutput() + 5, animatedY.getOutput() + 6, -1);
            duckSansFont16.drawString(description, animatedX.getOutput() + 5, animatedY.getOutput() + 18, -1);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private String getHealthColorTag(int health) {
        return health > 15 ? EnumChatFormatting.GREEN.toString() :
                health > 10 ? EnumChatFormatting.YELLOW.toString() :
                        EnumChatFormatting.RED.toString();
    }

    private String getColorTag(int value, int threshold) {
        return value > threshold * 0.75 ? EnumChatFormatting.GREEN.toString() :
                value > threshold * 0.5 ? EnumChatFormatting.YELLOW.toString() :
                        EnumChatFormatting.RED.toString();
    }

    public float getRenderX(float x) {
        return x - width / 2;
    }

    public float getRenderY(float y) {
        return y - height / 2;
    }

    public void runToXy(float realX, float realY) {
        animatedX.animate(getRenderX(realX), 800);
        animatedY.animate(getRenderY(realY), 800);
    }

    private double calculateBPS() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

    public void drawBackgroundAuto(int identifier, boolean shadow) {
        float renderHeight = ((y - animatedY.getOutput()) * 2) + (identifier == 1 ? 10 : 0);
        RenderUtil.scissor(animatedX.getOutput() - 1, animatedY.getOutput() - 1,
                ((x - animatedX.getOutput()) * 2) + 2, renderHeight + 2);
        Color color = DynamicIsland.colorMode.is("Colorful") ? HUDMod.color(1) : Color.BLACK;
        RoundedUtil.drawRound(animatedX.getOutput(), animatedY.getOutput(),
                (x - animatedX.getOutput()) * 2, renderHeight, 7,
                shadow ? color : RoundedUtil.reAlpha(color, 70));
    }
}