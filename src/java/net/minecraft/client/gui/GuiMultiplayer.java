package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import de.florianmichael.viamcp.ViaMCP;
import dev.tenacity.utils.server.ServerUtils;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiMultiplayer extends GuiScreen implements GuiYesNoCallback {
    private static final Logger logger = LogManager.getLogger();
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private final GuiScreen parentScreen;
    private ServerSelectionList serverListSelector;
    private ServerList savedServerList;
    private GuiButton btnEditServer;
    private GuiButton btnSelectServer;
    private GuiButton btnDeleteServer;
    private boolean deletingServer;
    private boolean addingServer;
    private boolean editingServer;
    private boolean directConnect;

    /**
     * The text to be displayed when the player's cursor hovers over a server listing.
     */
    private String hoveringText;
    private ServerData selectedServer;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    private boolean initialized;

    public GuiMultiplayer(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (!this.initialized) {
            this.initialized = true;
            this.savedServerList = new ServerList(this.mc2);
            this.savedServerList.loadServerList();
            this.lanServerList = new LanServerDetector.LanServerList();

            try {
                this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
                this.lanServerDetector.start();
            } catch (Exception exception) {
                logger.warn("Unable to initClient LAN server detection: " + exception.getMessage());
            }

            this.serverListSelector = new ServerSelectionList(this, this.mc2, this.width, this.height, 32, this.height - 64, 36);
            this.serverListSelector.func_148195_a(this.savedServerList);
        } else {
            this.serverListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
        }

        this.createButtons();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    public void createButtons() {
        this.buttonList.add(this.btnEditServer = new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, I18n.format("selectServer.edit")));
        this.buttonList.add(this.btnDeleteServer = new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, I18n.format("selectServer.delete")));
        this.buttonList.add(this.btnSelectServer = new GuiButton(1, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("selectServer.select")));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("selectServer.direct")));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, I18n.format("selectServer.add")));
        this.buttonList.add(new GuiButton(8, this.width / 2 + 4, this.height - 28, 70, 20, I18n.format("selectServer.refresh")));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, I18n.format("gui.cancel")));
        this.buttonList.add(ViaMCP.INSTANCE.getAsyncVersionSlider());
        this.selectServer(this.serverListSelector.func_148193_k());
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();

        if (this.lanServerList.getWasUpdated()) {
            List<LanServerDetector.LanServer> list = this.lanServerList.getLanServers();
            this.lanServerList.setWasNotUpdated();
            this.serverListSelector.func_148194_a(list);
        }

        this.oldServerPinger.pingPendingNetworks();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.oldServerPinger.clearPendingNetworks();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

            if (button.id == 2 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                String s4 = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData().serverName;

                if (s4 != null) {
                    this.deletingServer = true;
                    String s = I18n.format("selectServer.deleteQuestion");
                    String s1 = "'" + s4 + "' " + I18n.format("selectServer.deleteWarning");
                    String s2 = I18n.format("selectServer.deleteButton");
                    String s3 = I18n.format("gui.cancel");
                    GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, this.serverListSelector.func_148193_k());
                    this.mc2.displayGuiScreen(guiyesno);
                }
            } else if (button.id == 1) {
                this.connectToSelected();
            } else if (button.id == 4) {
                this.directConnect = true;
                this.mc2.displayGuiScreen(new GuiScreenServerList(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
            } else if (button.id == 3) {
                this.addingServer = true;
                this.mc2.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
            } else if (button.id == 7 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                this.editingServer = true;
                ServerData serverdata = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData();
                this.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
                this.selectedServer.copyFrom(serverdata);
                this.mc2.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer));
            } else if (button.id == 0) {
                this.mc2.displayGuiScreen(this.parentScreen);
            } else if (button.id == 8) {
                this.refreshServerList();
            }
        }
    }

    private void refreshServerList() {
        this.mc2.displayGuiScreen(new GuiMultiplayer(this.parentScreen));
    }

    public void confirmClicked(boolean result, int id) {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

        if (this.deletingServer) {
            this.deletingServer = false;

            if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                this.savedServerList.removeServerData(this.serverListSelector.func_148193_k());
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc2.displayGuiScreen(this);
        } else if (this.directConnect) {
            this.directConnect = false;

            if (result) {
                this.connectToServer(this.selectedServer);
            } else {
                this.mc2.displayGuiScreen(this);
            }
        } else if (this.addingServer) {
            this.addingServer = false;

            if (result) {
                this.savedServerList.addServerData(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc2.displayGuiScreen(this);
        } else if (this.editingServer) {
            this.editingServer = false;

            if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                ServerData serverdata = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData();
                serverdata.serverName = this.selectedServer.serverName;
                serverdata.serverIP = this.selectedServer.serverIP;
                serverdata.copyFrom(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc2.displayGuiScreen(this);
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        int i = this.serverListSelector.func_148193_k();
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = i < 0 ? null : this.serverListSelector.getListEntry(i);

        if (keyCode == 63) {
            this.refreshServerList();
        } else {
            if (i >= 0) {
                if (keyCode == 200) {
                    if (isShiftKeyDown()) {
                        if (i > 0 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                            this.savedServerList.swapServers(i, i - 1);
                            this.selectServer(this.serverListSelector.func_148193_k() - 1);
                            this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            this.serverListSelector.func_148195_a(this.savedServerList);
                        }
                    } else if (i > 0) {
                        this.selectServer(this.serverListSelector.func_148193_k() - 1);
                        this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k()) instanceof ServerListEntryLanScan) {
                            if (this.serverListSelector.func_148193_k() > 0) {
                                this.selectServer(this.serverListSelector.getSize() - 1);
                                this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            } else {
                                this.selectServer(-1);
                            }
                        }
                    } else {
                        this.selectServer(-1);
                    }
                } else if (keyCode == 208) {
                    if (isShiftKeyDown()) {
                        if (i < this.savedServerList.countServers() - 1) {
                            this.savedServerList.swapServers(i, i + 1);
                            this.selectServer(i + 1);
                            this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            this.serverListSelector.func_148195_a(this.savedServerList);
                        }
                    } else if (i < this.serverListSelector.getSize()) {
                        this.selectServer(this.serverListSelector.func_148193_k() + 1);
                        this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k()) instanceof ServerListEntryLanScan) {
                            if (this.serverListSelector.func_148193_k() < this.serverListSelector.getSize() - 1) {
                                this.selectServer(this.serverListSelector.getSize() + 1);
                                this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            } else {
                                this.selectServer(-1);
                            }
                        }
                    } else {
                        this.selectServer(-1);
                    }
                } else if (keyCode != 28 && keyCode != 156) {
                    super.keyTyped(typedChar, keyCode);
                } else {
                    this.actionPerformed(this.buttonList.get(2));
                }
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveringText = null;
        this.drawDefaultBackground();
        this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.title"), this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.hoveringText != null) {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringText)), mouseX, mouseY);
        }
    }

    public void connectToSelected() {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

        if (guilistextended$iguilistentry instanceof ServerListEntryNormal) {
            this.connectToServer(((ServerListEntryNormal) guilistextended$iguilistentry).getServerData());
        } else if (guilistextended$iguilistentry instanceof ServerListEntryLanDetected) {
            LanServerDetector.LanServer lanserverdetector$lanserver = ((ServerListEntryLanDetected) guilistextended$iguilistentry).getLanServer();
            this.connectToServer(new ServerData(lanserverdetector$lanserver.getServerMotd(), lanserverdetector$lanserver.getServerIpPort(), true));
        }
    }

    private void connectToServer(ServerData server) {
        this.mc2.displayGuiScreen(new GuiConnecting(this, this.mc2, server));
        ServerUtils.lastServer = server;
    }

    public void selectServer(int index) {
        this.serverListSelector.setSelectedSlotIndex(index);
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = index < 0 ? null : this.serverListSelector.getListEntry(index);
        this.btnSelectServer.enabled = false;
        this.btnEditServer.enabled = false;
        this.btnDeleteServer.enabled = false;

        if (guilistextended$iguilistentry != null && !(guilistextended$iguilistentry instanceof ServerListEntryLanScan)) {
            this.btnSelectServer.enabled = true;

            if (guilistextended$iguilistentry instanceof ServerListEntryNormal) {
                this.btnEditServer.enabled = true;
                this.btnDeleteServer.enabled = true;
            }
        }
    }

    public OldServerPinger getOldServerPinger() {
        return this.oldServerPinger;
    }

    public void setHoveringText(String p_146793_1_) {
        this.hoveringText = p_146793_1_;
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.serverListSelector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.serverListSelector.mouseReleased(mouseX, mouseY, state);
    }

    public ServerList getServerList() {
        return this.savedServerList;
    }

    public boolean func_175392_a(ServerListEntryNormal p_175392_1_, int p_175392_2_) {
        return p_175392_2_ > 0;
    }

    public boolean func_175394_b(ServerListEntryNormal p_175394_1_, int p_175394_2_) {
        return p_175394_2_ < this.savedServerList.countServers() - 1;
    }

    public void func_175391_a(ServerListEntryNormal p_175391_1_, int p_175391_2_, boolean p_175391_3_) {
        int i = p_175391_3_ ? 0 : p_175391_2_ - 1;
        this.savedServerList.swapServers(p_175391_2_, i);

        if (this.serverListSelector.func_148193_k() == p_175391_2_) {
            this.selectServer(i);
        }

        this.serverListSelector.func_148195_a(this.savedServerList);
    }

    public void func_175393_b(ServerListEntryNormal p_175393_1_, int p_175393_2_, boolean p_175393_3_) {
        int i = p_175393_3_ ? this.savedServerList.countServers() - 1 : p_175393_2_ + 1;
        this.savedServerList.swapServers(p_175393_2_, i);

        if (this.serverListSelector.func_148193_k() == p_175393_2_) {
            this.selectServer(i);
        }

        this.serverListSelector.func_148195_a(this.savedServerList);
    }
}
