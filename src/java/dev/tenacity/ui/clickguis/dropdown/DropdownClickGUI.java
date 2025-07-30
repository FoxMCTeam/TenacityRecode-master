package dev.tenacity.ui.clickguis.dropdown;

import dev.tenacity.Client;
import dev.tenacity.module.Category;
import dev.tenacity.module.impl.display.ClickGUIMod;
import dev.tenacity.module.impl.movement.InventoryMove;
import dev.tenacity.ui.searchbar.SearchBar;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.EaseBackIn;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.Theme;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjglx.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class DropdownClickGUI extends GuiScreen {

    public static boolean gradient;
    private final Pair<Animation, Animation> openingAnimations = Pair.of(
            new EaseBackIn(400, 1, 2f),
            new EaseBackIn(400, .4f, 2f));
    public boolean binding;
    private List<CategoryPanel> categoryPanels;

    @Override
    public void onDrag(int mouseX, int mouseY) {
        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.onDrag(mouseX, mouseY);
        }
        Client.INSTANCE.getSideGui().onDrag(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        openingAnimations.use((fade, opening) -> {
            fade.setDirection(Direction.FORWARDS);
            opening.setDirection(Direction.FORWARDS);
        });


        if (categoryPanels == null) {
            categoryPanels = new ArrayList<>();
            for (Category category : Category.values()) {
                categoryPanels.add(new CategoryPanel(category, openingAnimations));
            }
        }

        Client.INSTANCE.getSideGui().initGui();
        Client.INSTANCE.getSearchBar().initGui();


        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && !binding) {
            if (Client.INSTANCE.getSearchBar().isFocused()) {
                Client.INSTANCE.getSearchBar().getSearchField().setText("");
                Client.INSTANCE.getSearchBar().getSearchField().setFocused(false);
                return;
            }

            if (Client.INSTANCE.getSideGui().isFocused()) {
                Client.INSTANCE.getSideGui().setFocused(false);
                return;
            }

            Client.INSTANCE.getSearchBar().getOpenAnimation().setDirection(Direction.BACKWARDS);
            openingAnimations.use((fade, opening) -> {
                fade.setDirection(Direction.BACKWARDS);
                opening.setDirection(Direction.BACKWARDS);
            });
        }
        Client.INSTANCE.getSideGui().keyTyped(typedChar, keyCode);
        Client.INSTANCE.getSearchBar().keyTyped(typedChar, keyCode);
        categoryPanels.forEach(categoryPanel -> categoryPanel.keyTyped(typedChar, keyCode));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        binding = categoryPanels.stream().anyMatch(CategoryPanel::isTyping) ||
                (Client.INSTANCE.getSideGui().isFocused() && Client.INSTANCE.getSideGui().typing) || Client.INSTANCE.getSearchBar().isTyping();


        //  Gui.drawRect2(0,0, width, height, ColorUtil.applyOpacity(0, Tenacity.INSTANCE.getSearchBar().getFocusAnimation().getOutput().floatValue() * .25f));
        if (ClickGUIMod.walk.get() && !binding) {
            InventoryMove.updateStates();
        }

        // If the closing animation finished then change the gui screen to null
        if (openingAnimations.getSecond().finished(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null);
            return;
        }

        gradient = Theme.getCurrentTheme().isGradient() || ClickGUIMod.gradient.get();


        boolean focusedConfigGui = Client.INSTANCE.getSideGui().isFocused() || Client.INSTANCE.getSearchBar().isTyping();
        int fakeMouseX = focusedConfigGui ? 0 : mouseX, fakeMouseY = focusedConfigGui ? 0 : mouseY;
        ScaledResolution sr = new ScaledResolution(mc);


        RenderUtil.scaleStart(sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f, openingAnimations.getSecond().getOutput().floatValue() + .6f);

        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.drawScreen(fakeMouseX, fakeMouseY);
        }

        RenderUtil.scaleEnd();
        categoryPanels.forEach(categoryPanel -> categoryPanel.drawToolTips(fakeMouseX, fakeMouseY));

        //Draw Side GUI

        SideGUI sideGUI = Client.INSTANCE.getSideGui();
        sideGUI.getOpenAnimation().setDirection(openingAnimations.getFirst().getDirection());
        sideGUI.drawScreen(mouseX, mouseY);

        SearchBar searchBar = Client.INSTANCE.getSearchBar();
        searchBar.setAlpha(openingAnimations.getFirst().getOutput().floatValue() * (1 - sideGUI.getClickAnimation().getOutput().floatValue()));
        searchBar.drawScreen(fakeMouseX, fakeMouseY);
    }

    public void renderEffects() {
        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtil.scaleStart(sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f, openingAnimations.getSecond().getOutput().floatValue() + .6f);
        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.renderEffects();
        }
        RenderUtil.scaleEnd();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean focused = Client.INSTANCE.getSideGui().isFocused();
        Client.INSTANCE.getSideGui().mouseClicked(mouseX, mouseY, mouseButton);
        Client.INSTANCE.getSearchBar().mouseClicked(mouseX, mouseY, mouseButton);
        if (!focused) {
            categoryPanels.forEach(cat -> cat.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        boolean focused = Client.INSTANCE.getSideGui().isFocused();
        Client.INSTANCE.getSideGui().mouseReleased(mouseX, mouseY, state);
        Client.INSTANCE.getSearchBar().mouseReleased(mouseX, mouseY, state);
        if (!focused) {
            categoryPanels.forEach(cat -> cat.mouseReleased(mouseX, mouseY, state));
        }
    }

    @Override
    public void onGuiClosed() {
        if (ClickGUIMod.rescale.get()) {
            mc.gameSettings.guiScale = ClickGUIMod.prevGuiScale;
        }
    }


}
