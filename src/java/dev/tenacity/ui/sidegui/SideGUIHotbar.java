package dev.tenacity.ui.sidegui;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.ui.Screen;
import dev.tenacity.ui.sidegui.utils.CarouselButtons;
import dev.tenacity.ui.sidegui.utils.DropdownObject;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.DecelerateAnimation;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.objects.TextField;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import dev.tenacity.utils.time.TimerUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import org.lwjglx.input.Keyboard;

import java.awt.*;

public class SideGUIHotbar implements Screen {

    public final TextField searchField = new TextField(duckSansFont20);
    public final DropdownObject searchType = new DropdownObject("Type", "Configs", "Scripts");
    private final Animation searchAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);
    @Getter
    private final CarouselButtons carouselButtons = new CarouselButtons("Scripts", "Configs", "Info");
    private final TimerUtil refreshTimer = new TimerUtil();
    public float x, y, width, height, alpha;
    int ticks = 0;
    @Getter
    @Setter
    private String currentPanel;

    @Override
    public void initGui() {
        currentPanel = carouselButtons.getCurrentButton();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            searchField.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        RoundedUtil.drawRound(x + .625f, y + .625f, width - 1.25f, height - 1.25f, 5, ColorUtil.tripleColor(25, alpha));
        Gui.drawRect2(x, y + height - 4, width, 4, ColorUtil.tripleColor(25, alpha * alpha * alpha).getRGB());

        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);


        duckSansBoldFont32.drawString("Tenacity", x + 9.5f, y + duckSansBoldFont32.getMiddleOfBox(height), textColor);
        duckSansFont18.drawString(Client.VERSION, x + 9.5f + duckSansBoldFont32.getStringWidth("Tenacity") - 2,
                y + duckSansBoldFont32.getMiddleOfBox(height) - 2.5f, ColorUtil.applyOpacity(textColor, .5f));

        searchAnimation.setDirection(searchField.isFocused() || !searchField.getText().equals("") ? Direction.FORWARDS : Direction.BACKWARDS);
        float searchAnim = searchAnimation.getOutput().floatValue();
        float carouselAlpha = alpha * (1 - searchAnim);

        carouselButtons.setAlpha(carouselAlpha);
        carouselButtons.setRectWidth(75);
        carouselButtons.setRectHeight(20.5f);
        carouselButtons.setX(x + width / 2f - carouselButtons.getTotalWidth() / 2f);
        carouselButtons.setY(y + height / 2f - carouselButtons.getRectHeight() / 2f);
        carouselButtons.drawScreen(mouseX, mouseY);

        searchField.setRadius(5);
        searchField.setFill(ColorUtil.tripleColor(17, alpha));
        searchField.setOutline(ColorUtil.applyOpacity(Color.WHITE, 0));

        searchField.setHeight(carouselButtons.getRectHeight());
        searchField.setWidth(145.5f + (200 * searchAnim));
        float searchX = x + width - (searchField.getRealWidth() + 11);

        searchField.setXPosition(MathUtils.interpolateFloat(searchX, x + width / 2f - searchField.getRealWidth() / 2f, searchAnim));
        searchField.setYPosition(y + height / 2f - (searchField.getHeight() / 2f));
        searchField.setBackgroundText("Search");
        searchField.drawTextBox();

        if (!searchAnimation.isDone() || searchAnimation.finished(Direction.FORWARDS)) {

            searchType.setWidth(75);
            searchType.setHeight(carouselButtons.getRectHeight() - 5.5f);
            searchType.setX(x + width - (searchType.getWidth() + 11));
            searchType.setY(y + height / 2f - (searchType.getHeight() / 2f));
            searchType.setAlpha(alpha * searchAnim);
            searchType.setAccentColor(ColorUtil.applyOpacity(HUDMod.getClientColors().getFirst(), searchType.getAlpha()));
            searchType.drawScreen(mouseX, mouseY);
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        searchField.mouseClicked(mouseX, mouseY, button);

        if (searchField.isFocused() || !searchField.getText().equals("")) {
            searchType.mouseClicked(mouseX, mouseY, button);
            currentPanel = "Search";
            return;
        }

        carouselButtons.mouseClicked(mouseX, mouseY, button);
        currentPanel = carouselButtons.getCurrentButton();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}
