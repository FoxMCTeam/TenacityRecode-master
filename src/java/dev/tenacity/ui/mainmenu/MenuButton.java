package dev.tenacity.ui.mainmenu;

import dev.tenacity.ui.Screen;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.DecelerateAnimation;
import dev.tenacity.utils.misc.HoveringUtil;
import dev.tenacity.utils.render.RenderUtil;
import net.minecraft.util.ResourceLocation;

public class MenuButton implements Screen {

    private static final ResourceLocation rs = new ResourceLocation("Tenacity/MainMenu/menu-rect.png");
    public final String text;
    public float x, y, width, height;
    public Runnable clickAction;
    private Animation hoverAnimation;


    public MenuButton(String text) {
        this.text = text;
    }

    @Override
    public void initGui() {
        hoverAnimation = new DecelerateAnimation(200, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        boolean hovered = HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);


        RenderUtil.color(-1);
        RenderUtil.drawImage(rs, x, y, width, height);

        duckSansFont22.drawCenteredString(text, x + width / 2f, y + duckSansFont22.getMiddleOfBox(height), -1);
    }

    public void drawOutline() {
        RenderUtil.drawImage(rs, x, y, width, height);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovered = HoveringUtil.isHovering(x, y, width, height, mouseX, mouseY);
        if (hovered) {
            clickAction.run();
        }

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
