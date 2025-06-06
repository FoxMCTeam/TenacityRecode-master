package dev.tenacity.ui.sidegui.panels.searchpanel;

import dev.tenacity.Client;
import dev.tenacity.ui.sidegui.panels.Panel;
import dev.tenacity.ui.sidegui.utils.ToggleButton;
import dev.tenacity.utils.objects.Scroll;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.render.StencilUtil;
import lombok.Setter;
public class SearchPanel extends Panel {
    @Setter
    private String searchType = "";
    private String searchTypeHold = "";
    private String searchHold = "";
    private final ToggleButton compactMode = new ToggleButton("Compact Mode");

    private final Scroll searchScroll = new Scroll();

    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        tenacityFont18.drawString("Press ESC to return to the menu", getX() + 8, getY() + 8 + tenacityBoldFont40.getHeight() + 2, ColorUtil.applyOpacity(getTextColor(), .3f));

        tenacityBoldFont40.drawString("Search Results", getX() + 8, getY() + 8, getTextColor());

        float spacing = 8;
        float backgroundX = getX() + spacing, backgroundY = getY() + (45 + spacing), backgroundWidth = getWidth() - (spacing * 2), backgroundHeight = getHeight() - (45 + spacing * 2);
        RoundedUtil.drawRound(getX() + spacing, getY() + (45 + spacing), getWidth() - (spacing * 2), getHeight() - (45 + (spacing * 2)), 5, ColorUtil.tripleColor(27, getAlpha()));

        compactMode.setX(getX() + getWidth() - (compactMode.getWH() + 15));
        compactMode.setY(getY() + 33);
        compactMode.setAlpha(getAlpha());
        compactMode.drawScreen(mouseX, mouseY);

        String search = Client.INSTANCE.getSideGui().getHotbar().searchField.getText();

        if (check(search)) return;


        if (searchType.equals("Configs")) {
            drawConfigs(backgroundX, backgroundY, backgroundWidth, backgroundHeight, spacing, mouseX, mouseY, search);
        }else {
            drawScripts(backgroundX, backgroundY, backgroundWidth, backgroundHeight, spacing, mouseX, mouseY, search);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        compactMode.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public boolean check(String search) {
        if (!searchHold.equals(search)) {
            searchScroll.setRawScroll(0);
            searchHold = search;
        }

        if (!searchTypeHold.equals(searchType)) {
            searchTypeHold = searchType;
            return true;
        }
        return false;
    }


    public void drawScripts(float x, float y, float width, float height, float spacing, int mouseX, int mouseY, String search) {

        //6 spacing on left and right = 12
        //12 + ((12 spacing between configs) * 2 large spaces because we want 3 configs on the top)
        // This equals 36, so we deduct that from the background width and then divide by 3 to get the width of each config
        float scriptWidth = (width - 36) / 3f;

        float scriptHeight = compactMode.isEnabled() ? 38 : 90;
        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(x, y, width, height, 5, ColorUtil.tripleColor(27, getAlpha()));
        StencilUtil.readStencilBuffer(1);

        int rectYSeparation = 0;

        searchScroll.setMaxScroll(rectYSeparation);

        StencilUtil.uninitStencilBuffer();
    }

    public void drawConfigs(float x, float y, float width, float height, float spacing, int mouseX, int mouseY, String search) {

        //6 spacing on left and right = 12
        //12 + ((12 spacing between configs) * 2 large spaces because we want 3 configs on the top)
        // This equals 36, so we deduct that from the background width and then divide by 3 to get the width of each config

        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(x, y, width, height, 5, ColorUtil.tripleColor(27, getAlpha()));
        StencilUtil.readStencilBuffer(1);


        int rectYSeparation = 0;

        searchScroll.setMaxScroll(rectYSeparation);

        StencilUtil.uninitStencilBuffer();

    }


}
