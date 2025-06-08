package dev.tenacity.ui.clickguis.compact;

import dev.tenacity.Client;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.ModuleManager;
import dev.tenacity.module.impl.display.ClickGUIMod;
import dev.tenacity.module.impl.movement.InventoryMove;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.ui.clickguis.compact.impl.ModuleRect;
import dev.tenacity.ui.searchbar.SearchBar;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.DecelerateAnimation;
import dev.tenacity.utils.font.FontUtil;
import dev.tenacity.utils.misc.HoveringUtil;
import dev.tenacity.utils.misc.IOUtils;
import dev.tenacity.utils.objects.Drag;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.StencilUtil;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CompactClickgui extends GuiScreen {

    private final Animation openingAnimation = new DecelerateAnimation(250, 1);
    private final Drag drag = new Drag(40, 40);
    private final ModulePanel modulePanel = new ModulePanel();
    private final List<ModuleRect> searchResults = new ArrayList<>();
    private final List<String> searchTerms = new ArrayList<>();
    private final Color firstColor = Color.BLACK;
    private final Color secondColor = Color.BLACK;
    public boolean typing;
    private float rectWidth = 400;
    private float rectHeight = 300;
    private HashMap<Category, ArrayList<ModuleRect>> moduleRects;
    private String searchText;

    @Override
    public void onDrag(int mouseX, int mouseY) {
        boolean focusedConfigGui = Client.INSTANCE.getSideGui().isFocused();
        int fakeMouseX = focusedConfigGui ? 0 : mouseX, fakeMouseY = focusedConfigGui ? 0 : mouseY;

        drag.onDraw(fakeMouseX, fakeMouseY);
        Client.INSTANCE.getSideGui().onDrag(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        openingAnimation.setDirection(Direction.FORWARDS);
        rectWidth = 500;
        rectHeight = 350;
        if (moduleRects != null) {
            moduleRects.forEach((cat, list) -> list.forEach(ModuleRect::initGui));
        }
        modulePanel.initGui();
        Client.INSTANCE.getSideGui().initGui();
    }

    public void bloom() {
        float x = drag.getX(), y = drag.getY();
        if (!openingAnimation.isDone()) {
            x -= width + rectWidth / 2f;
            x += (width + rectWidth / 2f) * openingAnimation.getOutput().floatValue();
        }
        Gui.drawRect2(x, y, rectWidth, rectHeight, new Color(20, 20, 20).getRGB());

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            if (Client.INSTANCE.getSearchBar().isFocused()) {
                Client.INSTANCE.getSearchBar().getSearchField().setText("");
                Client.INSTANCE.getSearchBar().getSearchField().setFocused(false);
                return;
            }

            if (Client.INSTANCE.getSideGui().isFocused()) {
                Client.INSTANCE.getSideGui().setFocused(false);
                return;
            }

            openingAnimation.setDirection(Direction.BACKWARDS);
        }
        modulePanel.keyTyped(typedChar, keyCode);
        Client.INSTANCE.getSideGui().keyTyped(typedChar, keyCode);
        Client.INSTANCE.getSearchBar().keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ModuleManager.reloadModules || moduleRects == null) {
            if (moduleRects == null) {
                moduleRects = new HashMap<>();
            } else moduleRects.clear();
            for (Category category : Category.values()) {
                ArrayList<ModuleRect> modules = new ArrayList<>();
                for (Module module : Client.INSTANCE.getModuleManager().getModulesInCategory(category)) {
                    modules.add(new ModuleRect(module));
                }

                moduleRects.put(category, modules);
            }
            moduleRects.forEach((cat, list) -> list.forEach(ModuleRect::initGui));
            ModuleManager.reloadModules = false;
            return;
        }


        typing = modulePanel.typing || (Client.INSTANCE.getSideGui().isFocused() && Client.INSTANCE.getSideGui().isTyping()) || Client.INSTANCE.getSearchBar().isTyping();

        if (ClickGUIMod.walk.isEnabled() && !typing) {
            InventoryMove.updateStates();
        }

        boolean focusedConfigGui = Client.INSTANCE.getSideGui().isFocused();
        int fakeMouseX = focusedConfigGui ? 0 : mouseX, fakeMouseY = focusedConfigGui ? 0 : mouseY;

        float x = drag.getX(), y = drag.getY();

        if (!openingAnimation.isDone()) {
            x -= width + rectWidth / 2f;
            x += (width + rectWidth / 2f) * openingAnimation.getOutput().floatValue();
        } else if (openingAnimation.getDirection().equals(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null);
            return;
        }

        rectWidth = 475;
        rectHeight = 300;


        Gui.drawRect2(x, y, rectWidth, rectHeight, new Color(27, 27, 27).getRGB());


        Gui.drawRect2(x, y, 90, rectHeight, new Color(39, 39, 39).getRGB());

        GlStateManager.color(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(new ResourceLocation("Tenacity/modernlogo.png"));
        Gui.drawModalRectWithCustomSizedTexture(x + 5, y + 5, 0, 0, 20.5f, 20.5f, 20.5f, 20.5f);

        FontUtil.duckSansBoldFont22.drawString(Client.NAME, x + 33, y + 7, -1);
        FontUtil.duckSansFont16.drawCenteredString(Client.VERSION,
                x + 31 + FontUtil.duckSansBoldFont22.getStringWidth(Client.NAME) / 2f, y + 19, -1);

        boolean searching = Client.INSTANCE.getSearchBar().isFocused();

        float bannerHeight = 75 / 2f;
        Gui.drawRect2(x + 5, y + 31, 80, .5, new Color(110, 110, 110).getRGB());

        Gui.drawRect2(x + 5, y + rectHeight - (bannerHeight + 3), 80, .5, new Color(110, 110, 110).getRGB());

        float minus = (bannerHeight + 3) + 33;
        ClickGUIMod clickGUIMod = Client.INSTANCE.getModuleManager().getModule(ClickGUIMod.class);
        float catHeight = ((rectHeight - minus) / (Category.values().length));
        float seperation = 0;
        for (Category category : Category.values()) {
            float catY = y + 33 + seperation;
            boolean hovering = HoveringUtil.isHovering(x, catY + 8, 90, catHeight - 16, fakeMouseX, fakeMouseY);

            Color categoryColor = hovering ? ColorUtil.tripleColor(110).brighter() : ColorUtil.tripleColor(110);
            Color selectColor = (clickGUIMod.getActiveCategory() == category) ? Color.WHITE : categoryColor;

            if (!searching && (clickGUIMod.getActiveCategory() == category)) {
                Gui.drawRect2(x, catY, 90, catHeight, new Color(27, 27, 27).getRGB());
            }

            RenderUtil.resetColor();
            duckSansBoldFont22.drawString(Localization.get(category.name), x + 8, catY + duckSansFont22.getMiddleOfBox(catHeight), selectColor.getRGB());
            RenderUtil.resetColor();
            seperation += catHeight;
        }

        modulePanel.currentCat = searching ? null : clickGUIMod.getActiveCategory();
        modulePanel.moduleRects = getModuleRects(clickGUIMod.getActiveCategory());
        modulePanel.x = x;
        modulePanel.y = y;
        modulePanel.rectHeight = rectHeight;
        modulePanel.rectWidth = rectWidth;

        StencilUtil.initStencilToWrite();
        Gui.drawRect2(x, y, rectWidth, rectHeight, -1);
        StencilUtil.readStencilBuffer(1);
        modulePanel.drawScreen(fakeMouseX, fakeMouseY);
        StencilUtil.uninitStencilBuffer();

        modulePanel.drawTooltips(fakeMouseX, fakeMouseY);

        SideGUI sideGUI = Client.INSTANCE.getSideGui();
        sideGUI.getOpenAnimation().setDirection(openingAnimation.getDirection());
        sideGUI.drawScreen(mouseX, mouseY);

        SearchBar searchBar = Client.INSTANCE.getSearchBar();
        searchBar.setAlpha(openingAnimation.getOutput().floatValue() * (1 - sideGUI.getClickAnimation().getOutput().floatValue()));
        searchBar.drawScreen(fakeMouseX, fakeMouseY);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!Client.INSTANCE.getSideGui().isFocused()) {
            drag.onClick(mouseX, mouseY, mouseButton, HoveringUtil.isHovering(drag.getX(), drag.getY(), rectWidth, 10, mouseX, mouseY));
            float bannerWidth = 180 / 2f;
            float bannerHeight = 75 / 2f;

            ClickGUIMod clickGUIMod = Client.INSTANCE.getModuleManager().getModule(ClickGUIMod.class);

            //If hovering the discord thing lol
            if (HoveringUtil.isHovering(drag.getX(), drag.getY() + rectHeight - bannerHeight, bannerWidth, bannerHeight, mouseX, mouseY)) {
                if (RandomUtils.nextBoolean()) {
                    IOUtils.openLink("https://www.youtube.com/channel/UC2tPaPIMGeDETMTr1FQuMSA?sub_confirmation=1");
                } else {
                    IOUtils.openLink("https://www.youtube.com/channel/UCC5eswf_s4GMyH4W-K0RUuA?sub_confirmation=1");
                }
            }

            int separation = 0;
            float minus = (bannerHeight + 3) + 33;
            float catHeight = ((rectHeight - minus) / (Category.values().length));
            for (Category category : Category.values()) {
                float catY = drag.getY() + 33 + separation;
                boolean hovering = HoveringUtil.isHovering(drag.getX(), catY + 8, 90, catHeight - 16, mouseX, mouseY);
                if (hovering) {
                    clickGUIMod.setActiveCategory(category);
                }
                separation += catHeight;
            }

            modulePanel.mouseClicked(mouseX, mouseY, mouseButton);
            Client.INSTANCE.getSearchBar().mouseClicked(mouseX, mouseY, mouseButton);
        }
        Client.INSTANCE.getSideGui().mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (!Client.INSTANCE.getSideGui().isFocused()) {
            drag.onRelease(state);
            modulePanel.mouseReleased(mouseX, mouseY, state);
        }
        Client.INSTANCE.getSideGui().mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public List<ModuleRect> getModuleRects(Category category) {
        if (!Client.INSTANCE.getSearchBar().isFocused()) {
            return moduleRects.get(category);
        }

        String search = Client.INSTANCE.getSearchBar().getSearchField().getText();

        if (search.equals(searchText)) {
            return searchResults;
        } else {
            searchText = search;
        }

        List<ModuleRect> moduleRects1 = moduleRects.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        searchResults.clear();
        moduleRects1.forEach(moduleRect -> {
            searchTerms.clear();
            Module module = moduleRect.module;

            searchTerms.add(Localization.get(module.getName()));
            searchTerms.add(Localization.get(module.getCategory().name));
            if (!module.getAuthor().isEmpty()) {
                searchTerms.add(module.getAuthor());
            }
            for (Setting setting : module.getSettingsList()) {
                searchTerms.add(setting.name);
            }

            moduleRect.setSearchScore(FuzzySearch.extractOne(search, searchTerms).getScore());
        });

        searchResults.addAll(moduleRects1.stream().filter(moduleRect -> moduleRect.getSearchScore() > 60)
                .sorted(Comparator.comparingInt(ModuleRect::getSearchScore).reversed()).collect(Collectors.toList()));

        return searchResults;
    }
}
