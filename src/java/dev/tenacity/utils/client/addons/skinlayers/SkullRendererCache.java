package dev.tenacity.utils.client.addons.skinlayers;

import java.util.WeakHashMap;

import dev.tenacity.utils.client.addons.skinlayers.render.CustomizableModelPart;
import dev.tenacity.utils.client.addons.skinlayers.accessor.SkullSettings;
import net.minecraft.item.ItemStack;

public class SkullRendererCache {

    public static boolean renderNext = false;
    public static SkullSettings lastSkull = null;
    public static WeakHashMap<ItemStack, SkullSettings> itemCache = new WeakHashMap<>();
    
    public static class ItemSettings implements SkullSettings {

        private CustomizableModelPart hatModel = null;
        
        @Override
        public CustomizableModelPart getHeadLayers() {
            return hatModel;
        }

        @Override
        public void setupHeadLayers(CustomizableModelPart box) {
            this.hatModel = box;
        }
        
    }
    
}
