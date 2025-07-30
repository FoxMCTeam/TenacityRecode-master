package dev.tenacity.event.impl.render;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

/**
 * @author cedo
 * @since 03/30/2022
 */
@Getter
@AllArgsConstructor
public class RendererLivingEntityEvent extends CancellableEvent.StateEvent {
    private final EntityLivingBase entity;
    private final RendererLivingEntity<?> renderer;
    private final float partialTicks, entityYaw;
    private final double x, y, z;
}
