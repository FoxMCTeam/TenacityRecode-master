package dev.tenacity.ui.altmanager.panels;

import dev.tenacity.ui.altmanager.Panel;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class InfoPanel extends Panel {

    private final List<Pair<String, String>> controlInfo = new ArrayList<>();

    public InfoPanel() {
        setHeight(135);
        controlInfo.add(Pair.of("CTRL+V", "Paste a combo or combo list anywhere on the screen to import it"));
        controlInfo.add(Pair.of("DELETE", "When an alt is selected, you can delete it by pressing the delete key"));
        controlInfo.add(Pair.of("CTRL+A", "Selects the entire alt list"));
        controlInfo.add(Pair.of("CTRL+C", "Copies the combos of the currently selected alts"));
        controlInfo.add(Pair.of("SHIFT+CLICK", "Allows you to select a specfic range of alts"));
        controlInfo.add(Pair.of("DOUBLE-CLICK", "Logs into the selected alt"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        super.drawScreen(mouseX, mouseY);
        duckSansBoldFont32.drawCenteredString("Information", getX() + getWidth() / 2f, getY() + 3, ColorUtil.applyOpacity(-1, .75f));

        float controlY = getY() + duckSansBoldFont32.getHeight() + 8;
        for (Pair<String, String> control : controlInfo) {
            duckSansBoldFont18.drawString(control.getFirst() + " -", getX() + 12, controlY, ColorUtil.applyOpacity(-1, .5f));
            duckSansFont18.drawString(control.getSecond(), getX() +
                    duckSansBoldFont18.getStringWidth(control.getFirst() + " -") + 14, controlY, ColorUtil.applyOpacity(-1, .35f));

            controlY += duckSansBoldFont18.getHeight() + 6;
        }

        String text = "Combos must be formatted in the following format: ";
        String text2 = "email:password";
        float textWidth = duckSansFont18.getStringWidth(text);
        float text2Width = duckSansBoldFont18.getStringWidth(text2);
        float middleX = getX() + getWidth() / 2f - (textWidth + text2Width) / 2f;

        duckSansFont18.drawString(text, middleX, controlY + 4, ColorUtil.applyOpacity(-1, .5f));
        duckSansBoldFont18.drawString(text2, middleX + textWidth, controlY + 4, ColorUtil.applyOpacity(-1, .5f));

        duckSansFont18.drawCenteredString("Combo lists must have a new line seperating each combo",
                getX() + getWidth() / 2f, controlY + 16, ColorUtil.applyOpacity(-1, .5f));

    }
}
