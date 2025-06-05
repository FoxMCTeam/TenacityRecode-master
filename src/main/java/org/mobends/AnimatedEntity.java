package org.mobends;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.mobends.animation.Animation;
import org.mobends.animation.player.*;
import org.mobends.client.renderer.entity.RenderBendsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimatedEntity {
    public static List<AnimatedEntity> animatedEntities = new ArrayList<>();

    public static Map<String, RenderBendsPlayer> skinMap = Maps.newHashMap();
    public static RenderBendsPlayer playerRenderer;

    public String id;
    public String displayName;
    public Entity entity;

    public Class<? extends Entity> entityClass;
    public Render renderer;

    public List<Animation> animations = new ArrayList<>();

    public AnimatedEntity(String argID, String argDisplayName, Entity argEntity, Class<? extends Entity> argClass, Render argRenderer) {
        this.id = argID;
        this.displayName = argDisplayName;
        this.entityClass = argClass;
        this.renderer = argRenderer;
        this.entity = argEntity;
    }

    public AnimatedEntity add(Animation argGroup) {
        this.animations.add(argGroup);
        return this;
    }

    public static void register() {
        //BendsLogger.log("Registering Animated Entities...", BendsLogger.INFO);

        animatedEntities.clear();

        registerEntity(new AnimatedEntity("player", "Player", Minecraft.getMinecraft().thePlayer, EntityPlayer.class, new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager())).
                add(new Animation_Stand()).
                add(new Animation_Walk()).
                add(new Animation_Sneak()).
                add(new Animation_Sprint()).
                add(new Animation_Jump()).
                add(new Animation_Attack()).
                add(new Animation_Swimming()).
                add(new Animation_Bow()).
                add(new Animation_Riding()).
                add(new Animation_Mining()).
                add(new Animation_Axe()));

        playerRenderer = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
        skinMap.put("default", playerRenderer);
        skinMap.put("slim", new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager(), true));
    }

    public static void registerEntity(AnimatedEntity argEntity) {
        animatedEntities.add(argEntity);
    }

    public Animation get(String argName) {
        for (Animation animation : animations) {
            if (animation.getName().equalsIgnoreCase(argName)) {
                return animation;
            }
        }
        return null;
    }

    public static AnimatedEntity getByEntity(Entity argEntity) {
        for (AnimatedEntity animatedEntity : animatedEntities) {
            if (animatedEntity.entityClass.isInstance(argEntity)) {
                return animatedEntity;
            }
        }
        return null;
    }

    public static RenderBendsPlayer getPlayerRenderer(AbstractClientPlayer player) {
        String s = player.getSkinType();
        RenderBendsPlayer renderPlayer = skinMap.get(s);
        return renderPlayer != null ? renderPlayer : playerRenderer;
    }
}
