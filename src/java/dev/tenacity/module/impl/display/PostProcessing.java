package dev.tenacity.module.impl.display;

import dev.tenacity.Client;
import dev.tenacity.event.impl.render.ShaderEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.ParentAttribute;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.MultipleBoolSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.clickguis.modern.ModernClickGui;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.blur.GaussianBlur;
import dev.tenacity.utils.render.blur.KawaseBloom;
import dev.tenacity.utils.render.blur.KawaseBlur;
import dev.tenacity.utils.render.blur.MipmapKawaseBlur;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

public class PostProcessing extends Module {

    public static MultipleBoolSetting glowOptions = new MultipleBoolSetting("Glow Options",
            new BooleanSetting("Arraylist", true),
            new BooleanSetting("ClickGui", false),
            new BooleanSetting("Watermark", true),
            new BooleanSetting("Statistics", true),
            new BooleanSetting("Radar", true),
            new BooleanSetting("TargetHud", true),
            new BooleanSetting("Spotify", true),
            new BooleanSetting("Notifications", false),
            new BooleanSetting("Keystrokes", false));
    public final BooleanSetting blur = new BooleanSetting("Blur", true);
    private final NumberSetting iterations = new NumberSetting("Blur Iterations", 2, 8, 1, 1);
    private final NumberSetting offset = new NumberSetting("Blur Offset", 3, 10, 1, 1);
    private static final NumberSetting mipmapLevel = new NumberSetting("Blur Mipmap Level", 2, 4, 0, 1);
    private final BooleanSetting bloom = new BooleanSetting("Bloom", true);
    private final NumberSetting shadowRadius = new NumberSetting("Bloom Iterations", 3, 8, 1, 1);
    private final NumberSetting shadowOffset = new NumberSetting("Bloom Offset", 1, 10, 1, 1);
    private static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public PostProcessing() {
        super("module.display.postProcessing", Category.DISPLAY, "blurs shit");
        shadowRadius.addParent(bloom, ParentAttribute.BOOLEAN_CONDITION);
        shadowOffset.addParent(bloom, ParentAttribute.BOOLEAN_CONDITION);
        glowOptions.addParent(bloom, ParentAttribute.BOOLEAN_CONDITION);
        addSettings(blur, iterations, offset, mipmapLevel, bloom, glowOptions, shadowRadius, shadowOffset);
    }

    public static void runBloom(Runnable runnable, boolean blur, boolean bloom, int bloomRadius) {
        if (blur) {
            MipmapKawaseBlur.setMipmapLevel(mipmapLevel.getValue().intValue()); // 使用 1/4 尺寸

            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            runnable.run();
            stencilFramebuffer.unbindFramebuffer();

            MipmapKawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, 2, 2);
        }

        if (bloom) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            runnable.run();
            stencilFramebuffer.unbindFramebuffer();
            KawaseBloom.renderBlur(stencilFramebuffer.framebufferTexture, bloomRadius, 1);
        }
    }

    public void stuffToBlur(boolean bloom) {

        ScaledResolution sr = new ScaledResolution(mc);


        if (mc.currentScreen instanceof GuiChat) {
            Gui.drawRect2(2, sr.getScaledHeight() - (14 * GuiChat.openingAnimation.getOutput().floatValue()), sr.getScaledWidth() - 4, 12, Color.BLACK.getRGB());
        }

        if (mc.currentScreen == ClickGUIMod.dropdownClickGui) {
            ClickGUIMod.dropdownClickGui.renderEffects();
        }
        if (mc.currentScreen == ClickGUIMod.dropdownClickGui || mc.currentScreen == ClickGUIMod.modernClickGui || mc.currentScreen == ClickGUIMod.compactClickgui) {
            Client.INSTANCE.getSideGui().drawForEffects(bloom);
            Client.INSTANCE.getSearchBar().drawEffects();
        }


        RenderUtil.resetColor();
        mc.ingameGUI.getChatGUI().renderChatBox();
        RenderUtil.resetColor();
        NotificationsMod notificationsMod = Client.INSTANCE.getModuleManager().getModule(NotificationsMod.class);
        if (notificationsMod.isEnabled()) {
            notificationsMod.renderEffects(glowOptions.getSetting("Notifications").isEnabled());
        }

        if (bloom) {
            if (mc.currentScreen instanceof ModernClickGui) {
                ClickGUIMod.modernClickGui.drawBigRect();
            }
        }

    }

    public void blurScreen() {
        if (!enabled) return;
        if (blur.isEnabled()) {
            MipmapKawaseBlur.setMipmapLevel(mipmapLevel.getValue().intValue()); // 使用 1/4 尺寸

            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);

            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            Client.INSTANCE.getEventManager().call(new ShaderEvent(false, glowOptions));
            stuffToBlur(false);
            stencilFramebuffer.unbindFramebuffer();


            MipmapKawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations.getValue().intValue(), offset.getValue().intValue());

        }


        if (bloom.isEnabled()) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            Client.INSTANCE.getEventManager().call(new ShaderEvent(true, glowOptions));
            stuffToBlur(true);

            stencilFramebuffer.unbindFramebuffer();

            KawaseBloom.renderBlur(stencilFramebuffer.framebufferTexture, shadowRadius.getValue().intValue(), shadowOffset.getValue().intValue());

        }
    }


}