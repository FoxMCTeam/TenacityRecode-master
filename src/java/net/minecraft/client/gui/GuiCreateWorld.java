package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.lwjglx.input.Keyboard;

import java.io.IOException;
import java.util.Random;

public class GuiCreateWorld extends GuiScreen {
    /**
     * These filenames are known to be restricted on one or more OS's.
     */
    private static final String[] disallowedFilenames = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    public String chunkProviderSettingsJson = "";
    private final GuiScreen parentScreen;
    private GuiTextField worldNameField;
    private GuiTextField worldSeedField;
    private String saveDirName;
    private String gameMode = "survival";
    /**
     * Used to save away the game mode when the current "debug" world type is chosen (forcing it to spectator mode)
     */
    private String savedGameMode;
    private boolean generateStructuresEnabled = true;
    /**
     * If cheats are allowed
     */
    private boolean allowCheats;
    /**
     * User explicitly clicked "Allow Cheats" at some point
     * Prevents value changes due to changing game mode
     */
    private boolean allowCheatsWasSetByUser;
    private boolean bonusChestEnabled;
    /**
     * Set to true when "hardcore" is the currently-selected gamemode
     */
    private boolean hardCoreMode;
    private boolean alreadyGenerated;
    private boolean inMoreWorldOptionsDisplay;
    private GuiButton btnGameMode;
    private GuiButton btnMoreOptions;
    private GuiButton btnMapFeatures;
    private GuiButton btnBonusItems;
    private GuiButton btnMapType;
    private GuiButton btnAllowCommands;
    private GuiButton btnCustomizeType;
    private String gameModeDesc1;
    private String gameModeDesc2;
    private String worldSeed;
    private String worldName;
    private int selectedIndex;

    public GuiCreateWorld(GuiScreen p_i46320_1_) {
        this.parentScreen = p_i46320_1_;
        this.worldSeed = "";
        this.worldName = I18n.format("selectWorld.newWorld");
    }

    /**
     * Ensures that a proposed directory name doesn't collide with existing names.
     * Returns the name, possibly modified to avoid collisions.
     *
     * @param saveLoader used to check against existing names
     * @param name       the name to check, and possibly adjust (via the method's return)
     */
    public static String getUncollidingSaveDirName(ISaveFormat saveLoader, String name) {
        name = name.replaceAll("[\\./\"]", "_");

        for (String s : disallowedFilenames) {
            if (name.equalsIgnoreCase(s)) {
                name = "_" + name + "_";
            }
        }

        while (saveLoader.getWorldInfo(name) != null) {
            name = name + "-";
        }

        return name;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        this.worldNameField.updateCursorCounter();
        this.worldSeedField.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("selectWorld.create")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        this.buttonList.add(this.btnGameMode = new GuiButton(2, this.width / 2 - 75, 115, 150, 20, I18n.format("selectWorld.gameMode")));
        this.buttonList.add(this.btnMoreOptions = new GuiButton(3, this.width / 2 - 75, 187, 150, 20, I18n.format("selectWorld.moreWorldOptions")));
        this.buttonList.add(this.btnMapFeatures = new GuiButton(4, this.width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures")));
        this.btnMapFeatures.visible = false;
        this.buttonList.add(this.btnBonusItems = new GuiButton(7, this.width / 2 + 5, 151, 150, 20, I18n.format("selectWorld.bonusItems")));
        this.btnBonusItems.visible = false;
        this.buttonList.add(this.btnMapType = new GuiButton(5, this.width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType")));
        this.btnMapType.visible = false;
        this.buttonList.add(this.btnAllowCommands = new GuiButton(6, this.width / 2 - 155, 151, 150, 20, I18n.format("selectWorld.allowCommands")));
        this.btnAllowCommands.visible = false;
        this.buttonList.add(this.btnCustomizeType = new GuiButton(8, this.width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
        this.btnCustomizeType.visible = false;
        this.worldNameField = new GuiTextField(9, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.worldNameField.setFocused(true);
        this.worldNameField.setText(this.worldName);
        this.worldSeedField = new GuiTextField(10, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.worldSeedField.setText(this.worldSeed);
        this.showMoreWorldOptions(this.inMoreWorldOptionsDisplay);
        this.calcSaveDirName();
        this.updateDisplayState();
    }

    /**
     * Determine a save-directory name from the world name
     */
    private void calcSaveDirName() {
        this.saveDirName = this.worldNameField.getText().trim();

        for (char c0 : ChatAllowedCharacters.allowedCharactersArray) {
            this.saveDirName = this.saveDirName.replace(c0, '_');
        }

        if (StringUtils.isEmpty(this.saveDirName)) {
            this.saveDirName = "World";
        }

        this.saveDirName = getUncollidingSaveDirName(this.mc2.getSaveLoader(), this.saveDirName);
    }

    /**
     * Sets displayed GUI elements according to the current settings state
     */
    private void updateDisplayState() {
        this.btnGameMode.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + this.gameMode);
        this.gameModeDesc1 = I18n.format("selectWorld.gameMode." + this.gameMode + ".line1");
        this.gameModeDesc2 = I18n.format("selectWorld.gameMode." + this.gameMode + ".line2");
        this.btnMapFeatures.displayString = I18n.format("selectWorld.mapFeatures") + " ";

        if (this.generateStructuresEnabled) {
            this.btnMapFeatures.displayString = this.btnMapFeatures.displayString + I18n.format("options.on");
        } else {
            this.btnMapFeatures.displayString = this.btnMapFeatures.displayString + I18n.format("options.off");
        }

        this.btnBonusItems.displayString = I18n.format("selectWorld.bonusItems") + " ";

        if (this.bonusChestEnabled && !this.hardCoreMode) {
            this.btnBonusItems.displayString = this.btnBonusItems.displayString + I18n.format("options.on");
        } else {
            this.btnBonusItems.displayString = this.btnBonusItems.displayString + I18n.format("options.off");
        }

        this.btnMapType.displayString = I18n.format("selectWorld.mapType") + " " + I18n.format(WorldType.worldTypes[this.selectedIndex].getTranslateName());
        this.btnAllowCommands.displayString = I18n.format("selectWorld.allowCommands") + " ";

        if (this.allowCheats && !this.hardCoreMode) {
            this.btnAllowCommands.displayString = this.btnAllowCommands.displayString + I18n.format("options.on");
        } else {
            this.btnAllowCommands.displayString = this.btnAllowCommands.displayString + I18n.format("options.off");
        }
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 1) {
                this.mc2.displayGuiScreen(this.parentScreen);
            } else if (button.id == 0) {
                this.mc2.displayGuiScreen(null);

                if (this.alreadyGenerated) {
                    return;
                }

                this.alreadyGenerated = true;
                long i = (new Random()).nextLong();
                String s = this.worldSeedField.getText();

                if (!StringUtils.isEmpty(s)) {
                    try {
                        long j = Long.parseLong(s);

                        if (j != 0L) {
                            i = j;
                        }
                    } catch (NumberFormatException var7) {
                        i = s.hashCode();
                    }
                }

                WorldSettings.GameType worldsettings$gametype = WorldSettings.GameType.getByName(this.gameMode);
                WorldSettings worldsettings = new WorldSettings(i, worldsettings$gametype, this.generateStructuresEnabled, this.hardCoreMode, WorldType.worldTypes[this.selectedIndex]);
                worldsettings.setWorldName(this.chunkProviderSettingsJson);

                if (this.bonusChestEnabled && !this.hardCoreMode) {
                    worldsettings.enableBonusChest();
                }

                if (this.allowCheats && !this.hardCoreMode) {
                    worldsettings.enableCommands();
                }

                this.mc2.launchIntegratedServer(this.saveDirName, this.worldNameField.getText().trim(), worldsettings);
            } else if (button.id == 3) {
                this.toggleMoreWorldOptions();
            } else if (button.id == 2) {
                if (this.gameMode.equals("survival")) {
                    if (!this.allowCheatsWasSetByUser) {
                        this.allowCheats = false;
                    }

                    this.hardCoreMode = false;
                    this.gameMode = "hardcore";
                    this.hardCoreMode = true;
                    this.btnAllowCommands.enabled = false;
                    this.btnBonusItems.enabled = false;
                    this.updateDisplayState();
                } else if (this.gameMode.equals("hardcore")) {
                    if (!this.allowCheatsWasSetByUser) {
                        this.allowCheats = true;
                    }

                    this.hardCoreMode = false;
                    this.gameMode = "creative";
                    this.updateDisplayState();
                    this.hardCoreMode = false;
                    this.btnAllowCommands.enabled = true;
                    this.btnBonusItems.enabled = true;
                } else {
                    if (!this.allowCheatsWasSetByUser) {
                        this.allowCheats = false;
                    }

                    this.gameMode = "survival";
                    this.updateDisplayState();
                    this.btnAllowCommands.enabled = true;
                    this.btnBonusItems.enabled = true;
                    this.hardCoreMode = false;
                }

                this.updateDisplayState();
            } else if (button.id == 4) {
                this.generateStructuresEnabled = !this.generateStructuresEnabled;
                this.updateDisplayState();
            } else if (button.id == 7) {
                this.bonusChestEnabled = !this.bonusChestEnabled;
                this.updateDisplayState();
            } else if (button.id == 5) {
                ++this.selectedIndex;

                if (this.selectedIndex >= WorldType.worldTypes.length) {
                    this.selectedIndex = 0;
                }

                while (!this.canSelectCurWorldType()) {
                    ++this.selectedIndex;

                    if (this.selectedIndex >= WorldType.worldTypes.length) {
                        this.selectedIndex = 0;
                    }
                }

                this.chunkProviderSettingsJson = "";
                this.updateDisplayState();
                this.showMoreWorldOptions(this.inMoreWorldOptionsDisplay);
            } else if (button.id == 6) {
                this.allowCheatsWasSetByUser = true;
                this.allowCheats = !this.allowCheats;
                this.updateDisplayState();
            } else if (button.id == 8) {
                if (WorldType.worldTypes[this.selectedIndex] == WorldType.FLAT) {
                    this.mc2.displayGuiScreen(new GuiCreateFlatWorld(this, this.chunkProviderSettingsJson));
                } else {
                    this.mc2.displayGuiScreen(new GuiCustomizeWorldScreen(this, this.chunkProviderSettingsJson));
                }
            }
        }
    }

    /**
     * Returns whether the currently-selected world type is actually acceptable for selection
     * Used to hide the "debug" world type unless the shift key is depressed.
     */
    private boolean canSelectCurWorldType() {
        WorldType worldtype = WorldType.worldTypes[this.selectedIndex];
        return worldtype != null && worldtype.getCanBeCreated() && (worldtype != WorldType.DEBUG_WORLD || isShiftKeyDown());
    }

    /**
     * Toggles between initial world-creation display, and "more options" display.
     * Called when user clicks "More World Options..." or "Done" (same button, different labels depending on current
     * display).
     */
    private void toggleMoreWorldOptions() {
        this.showMoreWorldOptions(!this.inMoreWorldOptionsDisplay);
    }

    /**
     * Shows additional world-creation options if toggle is true, otherwise shows main world-creation elements
     */
    private void showMoreWorldOptions(boolean toggle) {
        this.inMoreWorldOptionsDisplay = toggle;

        if (WorldType.worldTypes[this.selectedIndex] == WorldType.DEBUG_WORLD) {
            this.btnGameMode.visible = !this.inMoreWorldOptionsDisplay;
            this.btnGameMode.enabled = false;

            if (this.savedGameMode == null) {
                this.savedGameMode = this.gameMode;
            }

            this.gameMode = "spectator";
            this.btnMapFeatures.visible = false;
            this.btnBonusItems.visible = false;
            this.btnMapType.visible = this.inMoreWorldOptionsDisplay;
            this.btnAllowCommands.visible = false;
            this.btnCustomizeType.visible = false;
        } else {
            this.btnGameMode.visible = !this.inMoreWorldOptionsDisplay;
            this.btnGameMode.enabled = true;

            if (this.savedGameMode != null) {
                this.gameMode = this.savedGameMode;
                this.savedGameMode = null;
            }

            this.btnMapFeatures.visible = this.inMoreWorldOptionsDisplay && WorldType.worldTypes[this.selectedIndex] != WorldType.CUSTOMIZED;
            this.btnBonusItems.visible = this.inMoreWorldOptionsDisplay;
            this.btnMapType.visible = this.inMoreWorldOptionsDisplay;
            this.btnAllowCommands.visible = this.inMoreWorldOptionsDisplay;
            this.btnCustomizeType.visible = this.inMoreWorldOptionsDisplay && (WorldType.worldTypes[this.selectedIndex] == WorldType.FLAT || WorldType.worldTypes[this.selectedIndex] == WorldType.CUSTOMIZED);
        }

        this.updateDisplayState();

        if (this.inMoreWorldOptionsDisplay) {
            this.btnMoreOptions.displayString = I18n.format("gui.done");
        } else {
            this.btnMoreOptions.displayString = I18n.format("selectWorld.moreWorldOptions");
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.worldNameField.isFocused() && !this.inMoreWorldOptionsDisplay) {
            this.worldNameField.textboxKeyTyped(typedChar, keyCode);
            this.worldName = this.worldNameField.getText();
        } else if (this.worldSeedField.isFocused() && this.inMoreWorldOptionsDisplay) {
            this.worldSeedField.textboxKeyTyped(typedChar, keyCode);
            this.worldSeed = this.worldSeedField.getText();
        }

        if (keyCode == 28 || keyCode == 156) {
            this.actionPerformed(this.buttonList.get(0));
        }

        this.buttonList.get(0).enabled = this.worldNameField.getText().length() > 0;
        this.calcSaveDirName();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.inMoreWorldOptionsDisplay) {
            this.worldSeedField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            this.worldNameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("selectWorld.create"), this.width / 2, 20, -1);

        if (this.inMoreWorldOptionsDisplay) {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);

            if (this.btnMapFeatures.visible) {
                this.drawString(this.fontRendererObj, I18n.format("selectWorld.mapFeatures.info"), this.width / 2 - 150, 122, -6250336);
            }

            if (this.btnAllowCommands.visible) {
                this.drawString(this.fontRendererObj, I18n.format("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
            }

            this.worldSeedField.drawTextBox();

            if (WorldType.worldTypes[this.selectedIndex].showWorldInfoNotice()) {
                this.fontRendererObj.drawSplitString(I18n.format(WorldType.worldTypes[this.selectedIndex].getTranslatedInfo()), this.btnMapType.xPosition + 2, this.btnMapType.yPosition + 22, this.btnMapType.getButtonWidth(), 10526880);
            }
        } else {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.resultFolder") + " " + this.saveDirName, this.width / 2 - 100, 85, -6250336);
            this.worldNameField.drawTextBox();
            this.drawString(this.fontRendererObj, this.gameModeDesc1, this.width / 2 - 100, 137, -6250336);
            this.drawString(this.fontRendererObj, this.gameModeDesc2, this.width / 2 - 100, 149, -6250336);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Set the initial values of a new world to create, from the values from an existing world.
     * <p>
     * Called after construction when a user selects the "Recreate" button.
     *
     * @param original The world we're copying from
     */
    public void recreateFromExistingWorld(WorldInfo original) {
        this.worldName = I18n.format("selectWorld.newWorld.copyOf", original.getWorldName());
        this.worldSeed = original.getSeed() + "";
        this.selectedIndex = original.getTerrainType().getWorldTypeID();
        this.chunkProviderSettingsJson = original.getGeneratorOptions();
        this.generateStructuresEnabled = original.isMapFeaturesEnabled();
        this.allowCheats = original.areCommandsAllowed();

        if (original.isHardcoreModeEnabled()) {
            this.gameMode = "hardcore";
        } else if (original.getGameType().isSurvivalOrAdventure()) {
            this.gameMode = "survival";
        } else if (original.getGameType().isCreative()) {
            this.gameMode = "creative";
        }
    }
}
