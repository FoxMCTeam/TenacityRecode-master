package dev.tenacity.ui.sidegui.forms;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.ui.Screen;
import dev.tenacity.utils.misc.HoveringUtil;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.tuples.Triplet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.function.BiConsumer;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class Form implements Screen {
    private final String title;
    private float x, y, width, height, alpha;
    private BiConsumer<String, String> uploadAction;
    private Triplet.TriConsumer<String, String, String> triUploadAction;


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        RoundedUtil.drawRound(x, y, width, height, 5, ColorUtil.tripleColor(37, alpha));
        duckSansBoldFont40.drawString(title, x + 5, y + 3, getTextColor());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY)) {
            Client.INSTANCE.getSideGui().displayForm(null);
        }

    }

    public float getSpacing() {
        return 8;
    }

    public Color getTextColor() {
        return ColorUtil.applyOpacity(Color.WHITE, alpha);
    }

    public Color getAccentColor() {
        return ColorUtil.applyOpacity(HUDMod.getClientColors().getFirst(), alpha);
    }

    public abstract void clear();

}
