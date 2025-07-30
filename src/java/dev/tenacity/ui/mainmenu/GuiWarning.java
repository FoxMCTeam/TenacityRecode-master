//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.tenacity.ui.mainmenu;
import java.awt.Color;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.RoundedUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class GuiWarning extends GuiScreen {
    GuiScreen ui;


    public void initGui() {
        this.ui = new CustomMainMenu();

        if (mc.gameSettings.guiScale != 2) {
            Client.prevGuiScale = mc.gameSettings.guiScale;
            Client.updateGuiScale = true;
            mc.gameSettings.guiScale = 2;
            mc.resize(mc.displayWidth - 1, mc.displayHeight);
            mc.resize(mc.displayWidth + 1, mc.displayHeight);
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        this.width = sr.getScaledWidth();
        this.height = sr.getScaledHeight();
        RenderUtil.drawGradientRect(0.0, 0.0, this.width, (double)this.height, ((Color)HUDMod.getClientColors().getFirst()).getRGB(), ((Color) HUDMod.getClientColors().getSecond()).getRGB());
        RoundedUtil.drawRound(0.0F, 0.0F, (float)this.width, (float)this.height, 0.0F, new Color(0, 0, 0, 100));
        duckSansFont12.drawString("多明尼克 用户端 © 2022-2024 版權所有", (float)this.width / 64.0F - 5.0F, (float)this.height / 64.0F, new Color(255, 255, 255, 100));
        duckSansFont40.drawString("此用戶端使用了大量渲染效果", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F, Color.WHITE.getRGB());
        duckSansFont20.drawString("請確保您的PC能運行這類大量渲染", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight(), Color.lightGray.getRGB());
        duckSansFont14.drawString("(*您可以在尋找PostProcessing模組進行調整)", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight() + (float)duckSansFont20.getHeight() + 2F, Color.gray.getRGB());
        RenderUtil.hoveredText(duckSansFont32, "好的", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight() + (float)duckSansFont20.getHeight() + 50.0F, (float)mouseX, (float)mouseY, Color.WHITE, Color.GRAY);
        RenderUtil.hoveredText(duckSansFont32, "我不在乎", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight() + (float)duckSansFont20.getHeight() + 20.0F, (float)mouseX, (float)mouseY, Color.WHITE, Color.GRAY);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        RenderUtil.hoveredTextWithRun(duckSansFont32, "好的", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight() + (float)duckSansFont20.getHeight() + 50.0F, (float)mouseX, (float)mouseY, Color.WHITE, Color.GRAY, () -> {
            mc.displayGuiScreen(this.ui);
        });
        RenderUtil.hoveredTextWithRun(duckSansFont32, "我不在乎", (float)this.width / 2.0F, (float)this.height / 2.0F - 55.0F + (float)duckSansFont40.getHeight() + (float)duckSansFont20.getHeight() + 20.0F, (float)mouseX, (float)mouseY, Color.WHITE, Color.GRAY, () -> {
            mc.displayGuiScreen(this.ui);
        });
    }

    public void onGuiClosed() {
        if (Client.updateGuiScale) {
            mc.gameSettings.guiScale = Client.prevGuiScale;
            Client.updateGuiScale = false;
        }

    }
}
