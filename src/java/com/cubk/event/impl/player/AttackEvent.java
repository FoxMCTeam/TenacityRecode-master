package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

@Getter
@AllArgsConstructor
public class AttackEvent extends CancellableEvent {
    private final EntityLivingBase targetEntity;
}
