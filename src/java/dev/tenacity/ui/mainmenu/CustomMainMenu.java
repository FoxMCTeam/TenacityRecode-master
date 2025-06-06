package dev.tenacity.ui.mainmenu;

import dev.tenacity.Client;
import dev.tenacity.utils.misc.NetworkingUtils;
import dev.tenacity.utils.render.GLUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.render.StencilUtil;
import dev.tenacity.utils.render.blur.GaussianBlur;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomMainMenu extends GuiScreen {

    public static boolean animatedOpen = false;
    private static boolean firstInit = false;
    private final List<MenuButton> buttons = new ArrayList<>() {{
        add(new MenuButton("Singleplayer"));
        add(new MenuButton("Multiplayer"));
        add(new MenuButton("Alt Manager"));
        add(new MenuButton("Settings"));
        add(new MenuButton("Exit"));
    }};
    private final ResourceLocation blurredRect = new ResourceLocation("Tenacity/MainMenu/rect-test.png");

    @Override
    public void initGui() {
        if (!firstInit) {
            NetworkingUtils.bypassSSL();
            firstInit = true;
        }

        if (mc.gameSettings.guiScale != 2) {
            Client.prevGuiScale = mc.gameSettings.guiScale;
            Client.updateGuiScale = true;
            mc.gameSettings.guiScale = 2;
            mc.resize(mc.displayWidth - 1, mc.displayHeight);
            mc.resize(mc.displayWidth + 1, mc.displayHeight);
        }
        buttons.forEach(MenuButton::initGui);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        width = sr.getScaledWidth();
        height = sr.getScaledHeight();


        RenderUtil.resetColor();

        Client.INSTANCE.videoRenderer.render(width, height);

        float rectWidth = 277;
        float rectHeight = 275.5f;

        GaussianBlur.startBlur();
        RoundedUtil.drawRound(width / 2f - rectWidth / 2f, height / 2f - rectHeight / 2f,
                rectWidth, rectHeight, 10, Color.WHITE);
        GaussianBlur.endBlur(40, 2);


        float outlineImgWidth = 688 / 2f;
        float outlineImgHeight = 681 / 2f;
        GLUtil.startBlend();
        RenderUtil.color(-1);
        RenderUtil.drawImage(blurredRect, width / 2f - outlineImgWidth / 2f, height / 2f - outlineImgHeight / 2f,
                outlineImgWidth, outlineImgHeight);

        GL11.glEnable(GL11.GL_BLEND);


        StencilUtil.initStencilToWrite();

        RenderUtil.setAlphaLimit(13);
        buttons.forEach(MenuButton::drawOutline);

        RenderUtil.setAlphaLimit(0);
        StencilUtil.readStencilBuffer(1);


        float circleW = 174 / 2f;
        float circleH = 140 / 2f;
        ResourceLocation rs = new ResourceLocation("Tenacity/MainMenu/circle-funny.png");
        mc.getTextureManager().bindTexture(rs);
        GLUtil.startBlend();
        RenderUtil.drawImage(rs, mouseX - circleW / 2f, mouseY - circleH / 2f, circleW, circleH);

        StencilUtil.uninitStencilBuffer();


        float buttonWidth = 140;
        float buttonHeight = 25;

        int count = 0;
        for (MenuButton button : buttons) {
            button.x = width / 2f - buttonWidth / 2f;
            button.y = ((height / 2f - buttonHeight / 2f) - 25) + count;
            button.width = buttonWidth;
            button.height = buttonHeight;
            button.clickAction = () -> {
                switch (button.text) {
                    case "Singleplayer":
                        mc.displayGuiScreen(new GuiSelectWorld(this));
                        break;
                    case "Multiplayer":
                        mc.displayGuiScreen(new GuiMultiplayer(this));
                        break;
                    case "Alt Manager":
                        mc.displayGuiScreen(Client.INSTANCE.getAltManager());
                        break;
                    case "Settings":
                        mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                        break;
                    case "Exit":
                        mc.shutdown();
                        break;
                }
            };
            button.drawScreen(mouseX, mouseY);
            count += (int) (buttonHeight + 5F);
        }

        duckSansBoldFont80.drawCenteredString("Tenacity", width / 2f, height / 2f - 110, Color.WHITE.getRGB());
        duckSansFont32.drawString(Client.VERSION, width / 2f + duckSansBoldFont80.getStringWidth("Tenacity") / 2f - (duckSansFont32.getStringWidth(Client.VERSION) / 2f), height / 2f - 113, Color.WHITE.getRGB());
        duckSansFont18.drawCenteredString("by " + Client.THANKS, width / 2f, height / 2f - 68, Color.WHITE.getRGB());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void onGuiClosed() {
        if (Client.updateGuiScale) {
            mc.gameSettings.guiScale = Client.prevGuiScale;
            Client.updateGuiScale = false;
        }
    }

}
