package dev.tenacity.ui;

import dev.tenacity.utils.Utils;

public interface Screen extends Utils {

    default void onDrag(int mouseX, int mouseY) {

    }

    default void initGui() {

    }

    default void keyTyped(char typedChar, int keyCode) {

    }

    default void drawScreen(int mouseX, int mouseY) {

    }

    default void mouseClicked(int mouseX, int mouseY, int button) {

    }

    default void mouseReleased(int mouseX, int mouseY, int state) {

    }

}
