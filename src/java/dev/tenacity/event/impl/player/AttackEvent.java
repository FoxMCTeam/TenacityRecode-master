package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

@Getter
@AllArgsConstructor
public class AttackEvent extends CancellableEvent {
    private final EntityLivingBase targetEntity;
}
