package net.minecraft.client.gui;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;

public class GuiGameOver extends GuiScreen implements GuiYesNoCallback {
    /**
     * The integer value containing the number of ticks that have passed since the player's death
     */
    private int enableButtonsTimer;
    private final boolean field_146346_f = false;

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttonList.clear();

        if (this.mc2.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
            if (this.mc2.isIntegratedServerRunning()) {
                this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.deleteWorld")));
            } else {
                this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.leaveServer")));
            }
        } else {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (this.mc2.getSession() == null) {
                this.buttonList.get(1).enabled = false;
            }
        }

        for (GuiButton guibutton : this.buttonList) {
            guibutton.enabled = false;
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc2.thePlayer.respawnPlayer();
                this.mc2.displayGuiScreen(null);
                break;

            case 1:
                if (this.mc2.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                    this.mc2.displayGuiScreen(new GuiMainMenu());
                } else {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    this.mc2.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id) {
        if (result) {
            this.mc2.theWorld.sendQuittingDisconnectingPacket();
            this.mc2.loadWorld(null);
            this.mc2.displayGuiScreen(new GuiMainMenu());
        } else {
            this.mc2.thePlayer.respawnPlayer();
            this.mc2.displayGuiScreen(null);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = this.mc2.theWorld.getWorldInfo().isHardcoreModeEnabled();
        String s = flag ? I18n.format("deathScreen.title.hardcore") : I18n.format("deathScreen.title");
        this.drawCenteredString(this.fontRendererObj, s, this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (flag) {
            this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.hardcoreInfo"), this.width / 2, 144, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score") + ": " + EnumChatFormatting.YELLOW + this.mc2.thePlayer.getScore(), this.width / 2, 100, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();
        ++this.enableButtonsTimer;

        if (this.enableButtonsTimer == 20) {
            for (GuiButton guibutton : this.buttonList) {
                guibutton.enabled = true;
            }
        }
    }
}
