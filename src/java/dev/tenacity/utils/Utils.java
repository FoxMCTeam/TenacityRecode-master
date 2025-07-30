package dev.tenacity.utils;

import dev.tenacity.utils.font.CustomFont;
import dev.tenacity.utils.font.FontUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IFontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface Utils {
    Minecraft mc = Minecraft.getMinecraft();
    IFontRenderer fr = mc.fontRendererObj;

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    FontUtil.FontType duckSansFont = FontUtil.FontType.DUCKSANS,
            iconFont = FontUtil.FontType.ICON,
            neverloseFont = FontUtil.FontType.NEVERLOSE,
            tahomaFont = FontUtil.FontType.TAHOMA,
            iconfontFont = FontUtil.FontType.ICONFONT,
            rubikFont = FontUtil.FontType.RUBIK;


    //Regular Fonts
    CustomFont duckSansFont12 = duckSansFont.size(12),
            duckSansFont14 = duckSansFont.size(14),
            duckSansFont16 = duckSansFont.size(16),
            duckSansFont18 = duckSansFont.size(18),
            duckSansFont20 = duckSansFont.size(20),
            duckSansFont22 = duckSansFont.size(22),
            duckSansFont24 = duckSansFont.size(24),
            duckSansFont26 = duckSansFont.size(26),
            duckSansFont28 = duckSansFont.size(28),
            duckSansFont32 = duckSansFont.size(32),
            duckSansFont40 = duckSansFont.size(40),
            duckSansFont80 = duckSansFont.size(80);

    //Bold Fonts
    CustomFont duckSansBoldFont14 = duckSansFont14.getBoldFont(),
            duckSansBoldFont16 = duckSansFont16.getBoldFont(),
            duckSansBoldFont18 = duckSansFont18.getBoldFont(),
            duckSansBoldFont20 = duckSansFont20.getBoldFont(),
            duckSansBoldFont22 = duckSansFont22.getBoldFont(),
            duckSansBoldFont24 = duckSansFont24.getBoldFont(),
            duckSansBoldFont26 = duckSansFont26.getBoldFont(),
            duckSansBoldFont28 = duckSansFont28.getBoldFont(),
            duckSansBoldFont32 = duckSansFont32.getBoldFont(),
            duckSansBoldFont40 = duckSansFont40.getBoldFont(),
            duckSansBoldFont80 = duckSansFont80.getBoldFont();

    //Icon Fontsor i
    CustomFont iconFont16 = iconFont.size(16),
            iconFont20 = iconFont.size(20),
            iconFont26 = iconFont.size(26),
            iconFont35 = iconFont.size(35),
            iconFont40 = iconFont.size(40);

    static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    static boolean isLobby() {
        if (mc.theWorld == null) {
            return true;
        }

        List<Entity> entities = mc.theWorld.getLoadedEntityList();
        for (Entity entity : entities) {
            if (entity != null && entity.getName().equals("§e§lCLICK TO PLAY")) {
                return true;
            }
        }

        boolean hasNetherStar = false;
        boolean hasCompass = false;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null) {
                if (stack.getItem() == Items.nether_star) {
                    hasNetherStar = true;
                }
                if (stack.getItem() == Items.compass) {
                    hasCompass = true;
                }
                if (hasNetherStar && hasCompass) {
                    return true;
                }
            }
        }
        return false;
    }
}
