package dev.tenacity.ui;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.PostProcessing;
import dev.tenacity.utils.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Mouse;

import java.awt.*;

public class GuiMaterialScreen extends GuiScreen {

    private MaterialButton md3Button;

    @Override
    public void initGui() {
        super.initGui();
        md3Button = new MaterialButton(1, width / 2 - 100, height / 2 - 20, 200, 40, "Material Design 3 按钮");
        buttonList.clear();
        buttonList.add(md3Button);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == md3Button) {
            Client.LOGGER.info("sb");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 更新按钮状态
        md3Button.update(mouseX, mouseY, partialTicks);
        // 绘制按钮
        md3Button.drawButton(mc, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static class MaterialButton extends GuiButton {

        private float hoverAnim = 0f;
        private float pressAnim = 0f;

        // 点击水波纹动画参数
        private boolean rippleActive = false;
        private float rippleRadius = 0f;
        private float rippleMaxRadius;
        private int rippleAlpha = 90; // 初始透明度
        private int rippleX, rippleY;

        private static final int COLOR_PRIMARY = 0xFF6750A4;
        private static final int COLOR_HOVER = 0xFF7F62B9;
        private static final int COLOR_PRESS = 0xFF512DA8;
        private static final int COLOR_TEXT = 0xFFFFFFFF;

        public MaterialButton(int buttonId, int x, int y, int width, int height, String text) {
            super(buttonId, x, y, width, height, text);
            enabled = true;
            visible = true;
            rippleMaxRadius = Math.max(width, height) * 1.2f;
        }

        public void update(int mouseX, int mouseY, float partialTicks) {
            boolean hovering = mouseX >= xPosition && mouseX < xPosition + width
                    && mouseY >= yPosition && mouseY < yPosition + height;

            float targetHover = hovering ? 1f : 0f;
            hoverAnim += (targetHover - hoverAnim) * 0.2f * partialTicks;
            hoverAnim = clamp(hoverAnim, 0f, 1f);

            boolean mouseDown = Mouse.isButtonDown(0);
            float targetPress = (hovering && mouseDown) ? 1f : 0f;
            pressAnim += (targetPress - pressAnim) * 0.3f * partialTicks;
            pressAnim = clamp(pressAnim, 0f, 1f);

            // 水波纹动画更新
            if (rippleActive) {
                rippleRadius += 300f * partialTicks;  // 扩散速度
                rippleAlpha -= 400 * partialTicks;    // 透明度逐渐减小
                if (rippleRadius > rippleMaxRadius || rippleAlpha <= 0) {
                    rippleActive = false;
                    rippleRadius = 0;
                    rippleAlpha = 90;
                }
            }
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                // 点击按钮时激活水波纹，记录点击相对坐标
                rippleActive = true;
                rippleRadius = 0;
                rippleAlpha = 90;

                rippleX = mouseX;
                rippleY = mouseY;

                return true;
            }
            return false;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;

            hovered = mouseX >= xPosition && mouseX < xPosition + width
                    && mouseY >= yPosition && mouseY < yPosition + height;

            int baseColor = COLOR_PRIMARY;
            int hoverColor = blendColor(baseColor, COLOR_HOVER, hoverAnim);
            int finalColor = blendColor(hoverColor, COLOR_PRESS, pressAnim);

            // 绘制阴影
            PostProcessing.runBloom(() -> {
                RoundedUtil.drawRound(xPosition + 2, yPosition + 2, width, height, 10, 0x44000000);
            }, false, true, 10);

            // 绘制按钮背景
            RoundedUtil.drawRound(xPosition, yPosition, width, height, 10, finalColor);

            // 绘制水波纹（如果激活）
            if (rippleActive) {
                // 颜色为白色，带透明度
                int rippleColor = (clampAlpha(rippleAlpha) << 24) | 0xFFFFFF;
                // 因为RoundedUtil.drawRound是圆角矩形，用宽高相同且半径大的圆角矩形模拟圆
                int size = (int) (rippleRadius * 2);
                int drawX = rippleX - (int) rippleRadius;
                int drawY = rippleY - (int) rippleRadius;
                RoundedUtil.drawRound(drawX, drawY, size, size, (int) rippleRadius, rippleColor);
            }

            // 绘制文字
            GlStateManager.enableBlend();
            mc.fontRendererObj.drawString(displayString, xPosition + (width - mc.fontRendererObj.getStringWidth(displayString)) / 2, yPosition + (height - 8) / 2, COLOR_TEXT);
            GlStateManager.disableBlend();
        }

        private int clampAlpha(int alpha) {
            if (alpha < 0) return 0;
            if (alpha > 255) return 255;
            return alpha;
        }

        private int blendColor(int c1, int c2, float ratio) {
            float r1 = ((c1 >> 16) & 0xFF) / 255f;
            float g1 = ((c1 >> 8) & 0xFF) / 255f;
            float b1 = (c1 & 0xFF) / 255f;
            float a1 = ((c1 >> 24) & 0xFF) / 255f;

            float r2 = ((c2 >> 16) & 0xFF) / 255f;
            float g2 = ((c2 >> 8) & 0xFF) / 255f;
            float b2 = (c2 & 0xFF) / 255f;
            float a2 = ((c2 >> 24) & 0xFF) / 255f;

            float r = r1 + (r2 - r1) * ratio;
            float g = g1 + (g2 - g1) * ratio;
            float b = b1 + (b2 - b1) * ratio;
            float a = a1 + (a2 - a1) * ratio;

            return ((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
        }

        private float clamp(float val, float min, float max) {
            return val < min ? min : (val > max ? max : val);
        }
    }

}
