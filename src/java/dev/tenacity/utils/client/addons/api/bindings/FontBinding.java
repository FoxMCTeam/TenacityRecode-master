package dev.tenacity.utils.client.addons.api.bindings;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.font.AbstractFontRenderer;
import dev.tenacity.utils.font.FontUtil;

import java.util.Arrays;


public class FontBinding implements Utils {

    public AbstractFontRenderer getCustomFont(String fontName, int fontSize) {
        FontUtil.FontType fontType = Arrays.stream(FontUtil.FontType.values()).filter(fontType1 -> fontType1.name().equals(fontName)).findFirst().orElse(FontUtil.FontType.DUCKSANS);
        return fontType.size(fontSize);
    }

    public AbstractFontRenderer getMinecraftFontRenderer() {
        return mc.fontRendererObj;
    }


    public AbstractFontRenderer getduckSansFont14() {
        return duckSansFont14;
    }

    public AbstractFontRenderer getduckSansFont16() {
        return duckSansFont16;
    }

    public AbstractFontRenderer getduckSansFont18() {
        return duckSansFont18;
    }

    public AbstractFontRenderer getduckSansFont20() {
        return duckSansFont20;
    }

    public AbstractFontRenderer getduckSansFont22() {
        return duckSansFont22;
    }

    public AbstractFontRenderer getduckSansFont24() {
        return duckSansFont24;
    }

    public AbstractFontRenderer getduckSansFont26() {
        return duckSansFont26;
    }

    public AbstractFontRenderer getduckSansFont28() {
        return duckSansFont28;
    }

    public AbstractFontRenderer getduckSansFont32() {
        return duckSansFont32;
    }

    public AbstractFontRenderer getduckSansFont40() {
        return duckSansFont40;
    }

    public AbstractFontRenderer getduckSansFont80() {
        return duckSansFont80;
    }
}
