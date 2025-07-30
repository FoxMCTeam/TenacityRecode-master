package dev.tenacity.module.impl.mods;
import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.TickEvent;
import dev.tenacity.event.impl.render.Render3DEvent;
import dev.tenacity.event.impl.render.RendererLivingEntityEvent;
import dev.tenacity.Client;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.render.Chams;
import dev.tenacity.module.settings.impl.BooleanSetting;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;
import dev.tenacity.utils.client.addons.mobends.AnimatedEntity;
import dev.tenacity.utils.client.addons.mobends.client.renderer.entity.RenderBendsPlayer;
import dev.tenacity.utils.client.addons.mobends.data.Data_Player;

public class MoBends extends Module {
    public static final ResourceLocation texture_NULL = new ResourceLocation("mobends/textures/white.png");
    public static final BooleanSetting swordTrail = new BooleanSetting("Sword Trail", true);
    public static final BooleanSetting spinAttack = new BooleanSetting("Spin attack", false);
    public float ticks = 0.0f;
    public float ticksPerFrame = 0.0f;
    private boolean register;

    public MoBends() {
        super("module.render.MoBends", Category.MODS, "eee");
        addSettings(swordTrail, spinAttack);
        register = false;
    }

    @Override
    public void onEnable() {
        if (!register) {
            AnimatedEntity.register();
            register = true;
        }
        super.onEnable();
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (mc.theWorld == null) {
            return;
        }

        for (int i = 0; i < Data_Player.dataList.size(); i++) {
            Data_Player.dataList.get(i).update(event.getTicks());
        }

        if (mc.thePlayer != null) {
            float newTicks = mc.thePlayer.ticksExisted + event.getTicks();
            if (!(mc.theWorld.isRemote && mc.isGamePaused())) {
                ticksPerFrame = Math.min(Math.max(0F, newTicks - ticks), 1F);
                ticks = newTicks;
            } else {
                ticksPerFrame = 0F;
            }
        }
    }

    @EventTarget
    public void onTickEvent(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (mc.theWorld == null) {
            return;
        }

        for (int i = 0; i < Data_Player.dataList.size(); i++) {
            Data_Player data = Data_Player.dataList.get(i);
            Entity entity = mc.theWorld.getEntityByID(data.entityID);
            if (entity != null) {
                if (!data.entityType.equalsIgnoreCase(entity.getName())) {
                    Data_Player.dataList.remove(data);
                    Data_Player.add(new Data_Player(entity.getEntityId()));
                    //BendsLogger.log("Reset entity",BendsLogger.DEBUG);
                } else {

                    data.motion_prev.set(data.motion);

                    data.motion.x = (float) entity.posX - data.position.x;
                    data.motion.y = (float) entity.posY - data.position.y;
                    data.motion.z = (float) entity.posZ - data.position.z;

                    data.position = new Vector3f((float) entity.posX, (float) entity.posY, (float) entity.posZ);
                }
            } else {
                Data_Player.dataList.remove(data);
            }
        }
    }

    @EventTarget
    public void onRendererLivingEntityEvent(RendererLivingEntityEvent event) {
        if (onRenderLivingEvent(event.getRenderer(), event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getEntityYaw(), event.getPartialTicks())) {
            event.cancel();
        }
    }

    public boolean onRenderLivingEvent(RendererLivingEntity renderer, EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!this.isEnabled() || renderer instanceof RenderBendsPlayer) {
            return false;
        }

        AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(entity);

        if (animatedEntity != null && entity instanceof EntityPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            if (Client.INSTANCE.getModuleManager().getModule(Chams.class).isEnabled()) {
                GL11.glEnable(32823);
                GL11.glPolygonOffset(1.0f, -1100000.0f);
            }
            AnimatedEntity.getPlayerRenderer(player).doRender(player, x, y, z, entityYaw, partialTicks);
            if (Client.INSTANCE.getModuleManager().getModule(Chams.class).isEnabled()) {
                GL11.glDisable(32823);
                GL11.glPolygonOffset(1.0f, 1100000.0f);
            }
            return true;
        }
        return false;
    }
}
