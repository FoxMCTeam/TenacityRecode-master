package dev.tenacity.ui;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.font.CustomFont;
import dev.tenacity.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SplashScreen implements Utils {
    private static Framebuffer framebuffer;
    private static int count;

    public static void continueCount() {
        continueCount(true);
    }

    public static void continueCount(boolean continueCount) {
        drawSplash();
        if (continueCount) {
            count++;
        }
    }

    /**
     * Render the splash screen background
     */
    private static void drawSplash() {

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        // Create the scale factor
        int scaleFactor = sr.getScaleFactor();
        // Bind the width and height to the framebuffer
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        // Create the projected image to be rendered
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, sr.getScaledWidth(), sr.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();


        GlStateManager.color(0, 0, 0, 0);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawSplashBackground(sr.getScaledWidth(), sr.getScaledHeight(), 1);

        RenderUtil.resetColor();
        GL11.glEnable(GL11.GL_BLEND);
        RenderUtil.setAlphaLimit(0);

        CustomFont fr = duckSansBoldFont80;

        if (count > 3) {
            count = 0;
        }

        fr.drawCenteredString("Authenticating" + ".".repeat(Math.max(0, count)), sr.getScaledWidth() / 2f, fr.getMiddleOfBox(sr.getScaledHeight()), -1);
        framebuffer.unbindFramebuffer();

        framebuffer.framebufferRender(sr.getScaledWidth() * scaleFactor, sr.getScaledHeight() * scaleFactor);

        RenderUtil.setAlphaLimit(1);

        Minecraft.getMinecraft().updateDisplay();
    }

    public static void drawSplashBackground(float width, float height, float alpha) {
        RenderUtil.resetColor();
        GlStateManager.color(1, 1, 1, alpha);
        mc.getTextureManager().bindTexture(new ResourceLocation("Tenacity/splashscreen.png"));
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
    }
}
