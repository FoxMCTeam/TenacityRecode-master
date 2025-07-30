package dev.tenacity.utils.client.addons.skinlayers.accessor;

import dev.tenacity.utils.client.addons.skinlayers.renderlayers.BodyLayerFeatureRenderer;
import dev.tenacity.utils.client.addons.skinlayers.renderlayers.HeadLayerFeatureRenderer;

/**
 * Used to expose the thinArms setting of the player model
 *
 */
public interface PlayerEntityModelAccessor {
	public boolean hasThinArms();
	public HeadLayerFeatureRenderer getHeadLayer();
	public BodyLayerFeatureRenderer getBodyLayer();
}