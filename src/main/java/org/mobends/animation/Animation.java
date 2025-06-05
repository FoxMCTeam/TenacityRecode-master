package org.mobends.animation;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import org.mobends.data.EntityData;

public abstract class Animation {
    public abstract void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData);

    public abstract String getName();

}
