package dev.tenacity.utils.client.addons.mobends.animation;

import dev.tenacity.utils.client.addons.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;

public abstract class Animation {
    public abstract void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData);

    public abstract String getName();

}
