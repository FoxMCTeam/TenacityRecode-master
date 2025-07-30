package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
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
