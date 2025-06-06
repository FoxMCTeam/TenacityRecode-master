package dev.tenacity.ui.sidegui.panels.configpanel;

import dev.tenacity.Client;
import dev.tenacity.config.ConfigManager;
import dev.tenacity.config.LocalConfig;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.ui.sidegui.forms.Form;
import dev.tenacity.ui.sidegui.panels.Panel;
import dev.tenacity.ui.sidegui.utils.ActionButton;
import dev.tenacity.ui.sidegui.utils.CarouselButtons;
import dev.tenacity.ui.sidegui.utils.DropdownObject;
import dev.tenacity.ui.sidegui.utils.ToggleButton;
import dev.tenacity.utils.misc.Multithreading;
import dev.tenacity.utils.objects.Scroll;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.render.StencilUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConfigPanel extends Panel {

    private final DropdownObject sorting = new DropdownObject("Sort by", "Relevance", "Alphabetical", "Top all time", "Recently updated");

    private final CarouselButtons carouselButtons = new CarouselButtons("Local");

    private List<ToggleButton> toggleButtons;
    private ToggleButton compactMode = new ToggleButton("Compact Mode");
    private List<ActionButton> actionButtons;
    private final List<LocalConfigRect> localConfigRects = new ArrayList<>();
    private final Scroll localConfigScroll = new Scroll();

    public ConfigPanel() {
        toggleButtons = new ArrayList<>();
        toggleButtons.add(new ToggleButton("Load visuals"));
        toggleButtons.add(new ToggleButton("Only show configs from current version"));
        toggleButtons.add(new ToggleButton("Only show configs made by you"));

        actionButtons = new ArrayList<>();
        actionButtons.add(new ActionButton("Save current config"));
        refresh();
    }

    private String sortingSelection = "Relevance";
    @Setter
    private boolean refresh = false;

    @Override
    public void initGui() {
        sortingSelection = sorting.getSelection();
        localConfigScroll.setRawScroll(0);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        duckSansBoldFont40.drawString("Configs", getX() + 8, getY() + 8, getTextColor());

        boolean filterVersion = toggleButtons.get(1).isEnabled();
        boolean filterAuthor = toggleButtons.get(2).isEnabled();


        carouselButtons.setAlpha(getAlpha());
        carouselButtons.setRectWidth(50);
        carouselButtons.setRectHeight(18);
        carouselButtons.setBackgroundColor(ColorUtil.tripleColor(55));
        carouselButtons.setY(getY() + 72 - carouselButtons.getRectHeight());
        carouselButtons.setX(getX() + getWidth() / 2f - carouselButtons.getTotalWidth() / 2f);
        carouselButtons.drawScreen(mouseX, mouseY);


        float spacing = 8;

        int additionalSeparation = 0;
        for (ActionButton button : actionButtons) {
            button.setX(getX() + 10 + additionalSeparation);
            button.setWidth(85);
            button.setHeight(15);
            button.setY(carouselButtons.getY() + carouselButtons.getRectHeight() / 2f - button.getHeight() / 2f);
            button.setAlpha(getAlpha());

            button.setClickAction(() -> {
                if (button.getName().equals("Save current config")) {
                    Form saveForm = Client.INSTANCE.getSideGui().displayForm("Save Config");
                    saveForm.setUploadAction((name, description) -> Multithreading.runAsync(() -> {
                        Client.INSTANCE.getConfigManager().saveConfig(name);
                    }));
                }
            });

            button.drawScreen(mouseX, mouseY);

            additionalSeparation += button.getWidth() + spacing;
        }


        float backgroundY = carouselButtons.getY() + carouselButtons.getRectHeight() + spacing;
        float backgroundX = getX() + 8;
        float backgroundWidth = getWidth() - (spacing * 2);
        float backgroundHeight = getHeight() - ((backgroundY - getY()) + 1 + spacing);

        RoundedUtil.drawRound(getX() + spacing, backgroundY, backgroundWidth, backgroundHeight, 5, ColorUtil.tripleColor(27, getAlpha()));
        boolean hovering = SideGUI.isHovering(getX() + spacing, backgroundY, backgroundWidth, backgroundHeight, mouseX, mouseY);
        if (hovering) {
            localConfigScroll.onScroll(35);
        }

        if (refresh) {
            refresh();
            return;
        }

        if (carouselButtons.getCurrentButton().equals("Local")) {
            ToggleButton loadVisuals = toggleButtons.get(0);
            loadVisuals.setX(getX() + getWidth() - (loadVisuals.getWH() + 8));
            loadVisuals.setY(carouselButtons.getY() + carouselButtons.getRectHeight() / 2f - loadVisuals.getWH() / 2f);
            loadVisuals.setAlpha(getAlpha());
            loadVisuals.drawScreen(mouseX, mouseY);

            //6 spacing on left and right = 12
            //12 + ((12 spacing between configs) * 2 large spaces because we want 3 configs on the top)
            // This equals 36, so we deduct that from the background width and then divide by 3 to get the width of each config
            float localConfigWidth = (backgroundWidth - 36) / 3f;

            float loaclConfigHeight = 38;
            StencilUtil.initStencilToWrite();
            RoundedUtil.drawRound(getX() + spacing, backgroundY, backgroundWidth, backgroundHeight, 5, ColorUtil.tripleColor(27, getAlpha()));
            StencilUtil.readStencilBuffer(1);


            int count2 = 0;
            int rectXSeparation2 = 0;
            int rectYSeparation2 = 0;
            for (LocalConfigRect localConfigRect : localConfigRects) {


                localConfigRect.setAlpha(getAlpha());
                localConfigRect.setAccentColor(getAccentColor());
                //This changes the x and y position for showing 3 configs per line
                if (count2 > 2) {
                    rectXSeparation2 = 0;
                    rectYSeparation2 += loaclConfigHeight + 12;
                    count2 = 0;
                }

                localConfigRect.setX(backgroundX + 6 + rectXSeparation2);
                localConfigRect.setY(backgroundY + 6 + rectYSeparation2 + localConfigScroll.getScroll());
                localConfigRect.setWidth(localConfigWidth);
                localConfigRect.setHeight(loaclConfigHeight);

                if (localConfigRect.getY() + localConfigRect.getHeight() > backgroundY && localConfigRect.getY() < backgroundY + backgroundHeight) {
                    localConfigRect.setClickable(true);
                    localConfigRect.drawScreen(mouseX, mouseY);
                } else {
                    localConfigRect.setClickable(false);
                }


                rectXSeparation2 += localConfigRect.getWidth() + 12;
                count2++;
            }

            localConfigScroll.setMaxScroll(rectYSeparation2);

            StencilUtil.uninitStencilBuffer();
        }

        ConfigManager.loadVisuals = toggleButtons.get(0).isEnabled();

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

        localConfigRects.forEach(localConfigRect -> localConfigRect.mouseClicked(mouseX, mouseY, button));
        toggleButtons.get(0).mouseClicked(mouseX, mouseY, button);


        carouselButtons.mouseClicked(mouseX, mouseY, button);
        actionButtons.forEach(button1 -> button1.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }


    public void refresh() {
        Client.INSTANCE.getConfigManager().collectConfigs();

        localConfigRects.clear();
        for (LocalConfig localConfig : ConfigManager.localConfigs) {
            localConfigRects.add(new LocalConfigRect(localConfig));
        }
        localConfigRects.sort(Comparator.<LocalConfigRect>comparingLong(local -> local.getBfa().lastModifiedTime().toMillis()).reversed());

        initGui();
        refresh = false;
    }
}
