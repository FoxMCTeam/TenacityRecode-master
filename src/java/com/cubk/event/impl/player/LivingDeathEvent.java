package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

@Getter
@AllArgsConstructor
public class LivingDeathEvent extends CancellableEvent {

    private final EntityLivingBase entity;
    private final DamageSource source;

}
