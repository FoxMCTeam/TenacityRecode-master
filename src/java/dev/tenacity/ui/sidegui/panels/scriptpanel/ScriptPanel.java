package dev.tenacity.ui.sidegui.panels.scriptpanel;

import dev.tenacity.Client;
import dev.tenacity.utils.client.addons.api.Script;
import dev.tenacity.ui.sidegui.SideGUI;
import dev.tenacity.ui.sidegui.panels.Panel;
import dev.tenacity.ui.sidegui.utils.ActionButton;
import dev.tenacity.ui.sidegui.utils.CarouselButtons;
import dev.tenacity.ui.sidegui.utils.DropdownObject;
import dev.tenacity.ui.sidegui.utils.ToggleButton;
import dev.tenacity.utils.misc.IOUtils;
import dev.tenacity.utils.misc.Multithreading;
import dev.tenacity.utils.objects.Scroll;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.render.StencilUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ScriptPanel extends Panel {

    private final DropdownObject sorting = new DropdownObject("Sort by", "Relevance", "Alphabetical", "Top all time", "Recently updated");
    private final CarouselButtons carouselButtons = new CarouselButtons("Local");
    private final ToggleButton compactMode = new ToggleButton("Compact Mode");
    private final List<LocalScriptRect> localScriptRects = new ArrayList<>();
    private final Scroll localScriptScroll = new Scroll();

    private final List<ActionButton> actionButtons;

    @Setter
    private boolean refresh;
    private boolean firstRefresh = true;

    public ScriptPanel() {
        actionButtons = new ArrayList<>();
        actionButtons.add(new ActionButton("Open documentation"));
        actionButtons.add(new ActionButton("Open folder"));
        Multithreading.runAsync(() -> {
            refresh();
            firstRefresh = false;
        });
    }

    private String sortingSelection = "Relevance";

    @Override
    public void initGui() {
        sortingSelection = sorting.getSelection();
        localScriptScroll.setRawScroll(0);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        duckSansBoldFont40.drawString("Scripts", getX() + 8, getY() + 8, getTextColor());


        carouselButtons.setAlpha(getAlpha());
        carouselButtons.setRectWidth(50);
        carouselButtons.setRectHeight(18);
        carouselButtons.setBackgroundColor(ColorUtil.tripleColor(55));
        carouselButtons.setY(getY() + 72 - carouselButtons.getRectHeight());
        carouselButtons.setX(getX() + getWidth() / 2f - carouselButtons.getTotalWidth() / 2f);
        carouselButtons.drawScreen(mouseX, mouseY);

        float spacing = 8;

        float backgroundY = carouselButtons.getY() + carouselButtons.getRectHeight() + spacing;
        float backgroundX = getX() + spacing;
        float backgroundWidth = getWidth() - (spacing * 2);
        float backgroundHeight = getHeight() - ((backgroundY - getY()) + 1 + spacing);

        int additionalSeparation = 0;
        for (ActionButton button : actionButtons) {
            button.setX(getX() + 10 + additionalSeparation);
            button.setWidth(additionalSeparation == 0 ? 90 : 70);
            button.setHeight(15);
            button.setY(carouselButtons.getY() + carouselButtons.getRectHeight() / 2f - button.getHeight() / 2f);
            button.setAlpha(getAlpha());

            button.setClickAction(() -> {
                //TODO: Add clickaction for the forms
                switch (button.getName()) {
                    case "Open documentation":
                        IOUtils.openLink("https://scripting.tenacity.dev");
                        break;
                    case "Open folder":
                        IOUtils.openFolder(Client.INSTANCE.getScriptManager().getScriptDirectory());
                        break;
                }
            });

            button.drawScreen(mouseX, mouseY);

            additionalSeparation += button.getWidth() + spacing;
        }

        boolean hovering = SideGUI.isHovering(getX() + spacing, backgroundY, backgroundWidth, backgroundHeight, mouseX, mouseY);
        if (hovering) {
            localScriptScroll.onScroll(35);
        }

        compactMode.setX(getX() + 69);
        compactMode.setY(getY() + 33);
        compactMode.setAlpha(getAlpha());
        compactMode.drawScreen(mouseX, mouseY);


        RoundedUtil.drawRound(backgroundX, backgroundY, backgroundWidth, backgroundHeight, 5, ColorUtil.tripleColor(27, getAlpha()));

        if (firstRefresh) return;

        if (refresh) {
            refresh();
            return;
        }


        //6 spacing on left and right = 12
        //12 + ((12 spacing between configs) * 2 large spaces because we want 3 configs on the top)
        // This equals 36, so we deduct that from the background width and then divide by 3 to get the width of each config
        float localScriptWidth = (backgroundWidth - 36) / 3f;

        float localScriptHeight = compactMode.isEnabled() ? 38 : 90;
        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(getX() + spacing, backgroundY, backgroundWidth, backgroundHeight, 5, ColorUtil.tripleColor(27, getAlpha()));
        StencilUtil.readStencilBuffer(1);

        int count2 = 0;
        int rectXSeparation2 = 0;
        int rectYSeparation2 = 0;

        for (LocalScriptRect localScriptRect : localScriptRects) {
            localScriptRect.setAlpha(getAlpha());
            localScriptRect.setAccentColor(getAccentColor());
            //This changes the x and y position for showing 3 configs per line
            if (count2 > 2) {
                rectXSeparation2 = 0;
                rectYSeparation2 += localScriptHeight + 12;
                count2 = 0;
            }

            localScriptRect.setX(backgroundX + 6 + rectXSeparation2);
            localScriptRect.setY(backgroundY + 6 + rectYSeparation2 + localScriptScroll.getScroll());
            localScriptRect.setWidth(localScriptWidth);
            localScriptRect.setHeight(localScriptHeight);
            localScriptRect.setCompact(compactMode.isEnabled());

            if (localScriptRect.getY() + localScriptRect.getHeight() > backgroundY && localScriptRect.getY() < backgroundY + backgroundHeight) {
                localScriptRect.setClickable(true);
                localScriptRect.drawScreen(mouseX, mouseY);
            } else {
                localScriptRect.setClickable(false);
            }


            rectXSeparation2 += localScriptRect.getWidth() + 12;
            count2++;
        }

        localScriptScroll.setMaxScroll(rectYSeparation2);

        StencilUtil.uninitStencilBuffer();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        carouselButtons.mouseClicked(mouseX, mouseY, button);

        localScriptRects.forEach(localScriptRect -> localScriptRect.mouseClicked(mouseX, mouseY, button));


        compactMode.mouseClicked(mouseX, mouseY, button);
        actionButtons.forEach(actionButton -> actionButton.mouseClicked(mouseX, mouseY, button));

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public void refresh() {
        Client.INSTANCE.getScriptManager().reloadScripts();


        localScriptRects.clear();
        for (Script script : Client.INSTANCE.getScriptManager().getScripts()) {
            localScriptRects.add(new LocalScriptRect(script));
        }


        initGui();
        refresh = false;
    }
}
