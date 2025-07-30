package dev.tenacity.ui.altmanager.panels;

import dev.tenacity.ui.altmanager.Panel;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class MicrosoftInfoPanel extends Panel {

    private final List<Pair<String, String>> steps = new ArrayList<>();

    public MicrosoftInfoPanel() {
        setHeight(135);
        steps.add(Pair.of("1", "Type the email and password either as a combo or in each respective field"));
        steps.add(Pair.of("2", "Click the microsoft login button"));
        steps.add(Pair.of("3", "Your browser will open with a microsoft login panel"));
        steps.add(Pair.of("INFO", "Make sure that you are logged out of all microsoft accounts so that you are prompted with a login panel"));
        steps.add(Pair.of("4", "The email and password will be copied to the clipboard"));
        steps.add(Pair.of("5", "Follow all the steps Microsoft gives you to log into your account"));
        steps.add(Pair.of("6", "Enjoy! You are now logged in to your microsoft account"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        setHeight(119);
        super.drawScreen(mouseX, mouseY);
        duckSansBoldFont26.drawCenteredString("How to use Microsoft Login", getX() + getWidth() / 2f, getY() + 4, ColorUtil.applyOpacity(-1, .75f));

        float controlY = getY() + duckSansBoldFont32.getHeight() + 8;
        for (Pair<String, String> control : steps) {
            if (control.getFirst().equals("INFO")) {
                duckSansFont16.drawCenteredString("Make sure that you are logged out of all microsoft accounts so that you are",
                        getX() + getWidth() / 2f, controlY, ColorUtil.applyOpacity(-1, .75f));

                controlY += duckSansBoldFont16.getHeight() + 4;

                duckSansFont16.drawCenteredString("prompted with a new login panel",
                        getX() + getWidth() / 2f, controlY, ColorUtil.applyOpacity(-1, .75f));

                controlY += duckSansBoldFont16.getHeight() + 6;
                continue;
            }

            duckSansBoldFont16.drawString(control.getFirst() + ". ", getX() + 12, controlY, ColorUtil.applyOpacity(-1, .5f));
            duckSansFont16.drawString(control.getSecond(), getX() +
                    duckSansBoldFont16.getStringWidth(control.getFirst() + ". ") + 14, controlY, ColorUtil.applyOpacity(-1, .35f));

            controlY += duckSansBoldFont16.getHeight() + 6;
        }

    }
}
