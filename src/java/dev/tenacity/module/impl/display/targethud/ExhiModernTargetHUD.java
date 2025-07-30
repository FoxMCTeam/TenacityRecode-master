package dev.tenacity.module.impl.display.targethud;

import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.GLUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.RoundedUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

public class ExhiModernTargetHUD extends TargetHUD {
    float radius = 4F;
    public ExhiModernTargetHUD() {
        super("Modern Exhibition");
    }

    @Override
    public void render(float x, float y, float alpha, EntityLivingBase target) {
        setWidth(Math.max(135, duckSansFont20.getStringWidth("Name: " + target.getName()) + 60));
        setHeight(46);

        Color darkest = ColorUtil.applyOpacity(new Color(10, 10, 10), alpha);
        Color secondDarkest = ColorUtil.applyOpacity(new Color(22, 22, 22), alpha);
        Color lightest = ColorUtil.applyOpacity(new Color(44, 44, 44), alpha);
        Color middleColor = ColorUtil.applyOpacity(new Color(34, 34, 34), alpha);
        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);
        RoundedUtil.drawRound(x - 3.5F, y - 3.5F, getWidth() + 7F, getHeight() + 7F, radius, darkest.getRGB());
        RoundedUtil.drawRound(x - 3F, y - 3F, getWidth() + 6F, getHeight() + 6F, radius, middleColor.getRGB());
        RoundedUtil.drawRound(x - 1F, y - 1F, getWidth() + 2F, getHeight() + 2F, radius, lightest.getRGB());
        RoundedUtil.drawRound(x, y, getWidth(), getHeight(), radius, secondDarkest.getRGB());


        float size = getHeight() - 6;
        //下
        //RoundedUtil.drawRound(x + 3F, y + 3F + size, size, .5F, radius, lightest.getRGB());
        //上
        RoundedUtil.drawRoundOutline(x + 3F, y + 3F, size, .5F + size, radius, 0.1F, new Color(0, 0, 0, 0), lightest);

        tahomaFont.boldSize(16).drawString(target.getName(), x + 8 + size, y + 6, textColor.getRGB());
        float healthValue = (target.getHealth() + target.getAbsorptionAmount()) / (target.getMaxHealth() + target.getAbsorptionAmount());

        Color healthColor = healthValue > .5f ? ColorUtil.interpolateColorC(new Color(255, 255, 10), new Color(10, 255, 10), (healthValue - .5f) / .5f) :
                ColorUtil.interpolateColorC(new Color(255, 10, 10), new Color(255, 255, 10), healthValue * 2);

        healthColor = ColorUtil.applyOpacity(healthColor, alpha);

        float healthBarWidth = getWidth() - (size + 12);
        Gui.drawRect2(x + 8 + size, y + 15, healthBarWidth, 5, darkest.getRGB());
        Gui.drawRect2(x + 8 + size + .5, y + 15.5F, healthBarWidth - 1, 4, ColorUtil.interpolateColor(darkest, healthColor, .2f));

        float heathBarActualWidth = healthBarWidth - 1;
        Gui.drawRect2(x + 8 + size + .5, y + 15.5F, heathBarActualWidth * healthValue, 4, healthColor.getRGB());

        float increment = heathBarActualWidth / 11;
        for (int i = 1; i < 11; i++) {
            Gui.drawRect2(x + 8 + size + (increment * i), y + 15.5F, .5f, 4, darkest.getRGB());
        }

        tahomaFont.size(12).drawString("HP: " + MathUtils.round(target.getHealth() + target.getAbsorptionAmount(), 1) + " | Dist: " + MathUtils.round(mc.thePlayer.getDistanceToEntity(target), 1),
                x + 8 + size, y + 25, textColor.getRGB());


        float separation = healthBarWidth / 5;
        GLUtil.startBlend();
        RenderUtil.color(textColor.getRGB());
        GuiInventory.drawEntityOnScreen((int) (x + 3 + size / 2f), (int) (y + size + 1), 18, target.rotationYaw, -target.rotationPitch, target);

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i <= 3; i++) {
            if (target.getCurrentArmor(i) == null) continue;
            RenderUtil.resetColor();
            GLUtil.startBlend();
            RenderUtil.color(textColor.getRGB());
            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(i), (int) (x + size + 7 + (separation * (3 - i))), (int) (y + 28));
            GLUtil.endBlend();
        }

        if (target.getHeldItem() != null) {
            GLUtil.startBlend();
            RenderUtil.resetColor();
            RenderUtil.color(textColor.getRGB());
            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getHeldItem(), (int) (x + size + 7 + (separation * 4)), (int) (y + 28));
            GLUtil.endBlend();
        }
        RenderHelper.disableStandardItemLighting();

    }


    @Override
    public void renderEffects(float x, float y, float alpha, boolean glow) {
        RoundedUtil.drawRound(x - 3.5F, y - 3.5F, getWidth() + 7F, getHeight() + 7F,
                radius, ColorUtil.applyOpacity(Color.BLACK.getRGB(), alpha));
    }
}
