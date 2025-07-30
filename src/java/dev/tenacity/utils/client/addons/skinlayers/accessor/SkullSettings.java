package dev.tenacity.utils.client.addons.skinlayers.accessor;

import dev.tenacity.utils.client.addons.skinlayers.render.CustomizableModelPart;

public interface SkullSettings {

    public CustomizableModelPart getHeadLayers();
    
    public void setupHeadLayers(CustomizableModelPart box);
    
}
