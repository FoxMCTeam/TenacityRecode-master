package dev.tenacity.utils.client.addons.skinlayers.accessor;

import dev.tenacity.utils.client.addons.skinlayers.render.CustomizableModelPart;

public interface PlayerSettings {

	public CustomizableModelPart getHeadLayers();
	
	public void setupHeadLayers(CustomizableModelPart box);
	
	public CustomizableModelPart[] getSkinLayers();
	
	public void setupSkinLayers(CustomizableModelPart[] box);

}
