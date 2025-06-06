package dev.tenacity.event.impl.player;

import dev.tenacity.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.entity.EntityLivingBase;

@AllArgsConstructor
public class AttackEvent extends Event {

    private final EntityLivingBase targetEntity;

    
    public EntityLivingBase getTargetEntity() {
        return targetEntity;
    }

}
