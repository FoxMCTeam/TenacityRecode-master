package net.minecraft.client.renderer.entity;

import dev.tenacity.Client;
import dev.tenacity.module.impl.combat.KillAura;
import dev.tenacity.module.impl.mods.SkinLayers3D;
import dev.tenacity.module.impl.render.BrightPlayers;
import dev.tenacity.module.impl.mods.CustomModel;
import dev.tenacity.utils.client.addons.skinlayers.SkinLayersModBase;
import dev.tenacity.utils.client.addons.skinlayers.SkinUtil;
import dev.tenacity.utils.client.addons.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tenacity.utils.client.addons.skinlayers.accessor.PlayerSettings;
import dev.tenacity.utils.client.addons.skinlayers.renderlayers.BodyLayerFeatureRenderer;
import dev.tenacity.utils.client.addons.skinlayers.renderlayers.HeadLayerFeatureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;

public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer>  implements PlayerEntityModelAccessor {
    /**
     * this field is used to indicate the 3-pixel wide arms
     */
    private boolean smallArms;
    private HeadLayerFeatureRenderer headLayer;
    private BodyLayerFeatureRenderer bodyLayer;
    public RenderPlayer(RenderManager renderManager) {
        this(renderManager, false);
    }

    public RenderPlayer(RenderManager renderManager, boolean useSmallArms) {

        super(renderManager, new ModelPlayer(0.0F, useSmallArms), 0.5F);
        this.smallArms = useSmallArms;
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
        headLayer = new HeadLayerFeatureRenderer(this);
        bodyLayer = new BodyLayerFeatureRenderer(this);
    }
    @Override
    public HeadLayerFeatureRenderer getHeadLayer() {
        return headLayer;
    }

    @Override
    public BodyLayerFeatureRenderer getBodyLayer() {
        return bodyLayer;
    }

    @Override
    public boolean hasThinArms() {
        return smallArms;
    }

    public ModelPlayer getMainModel() {
        return (ModelPlayer) super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!entity.isUser() || this.renderManager.livingPlayer == entity) {
            double d0 = y;

            if(Client.INSTANCE.isEnabled(BrightPlayers.class)){
                GlStateManager.disableLighting();
            }

            if (entity.isSneaking() && !(entity instanceof EntityPlayerSP)) {
                d0 = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            super.doRender(entity, x, d0, z, entityYaw, partialTicks);
        }
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        if (Client.INSTANCE.getModuleManager().getModule(SkinLayers3D.class).isEnabled()) {
            ModelPlayer playerModel = getMainModel();
            if (Minecraft.getMinecraft().thePlayer.getPositionVector().squareDistanceTo(clientPlayer.getPositionVector()) < SkinLayersModBase.config.renderDistanceLOD * SkinLayersModBase.config.renderDistanceLOD) {
                playerModel.bipedHeadwear.isHidden = playerModel.bipedHeadwear.isHidden || SkinLayersModBase.config.enableHat;
                playerModel.bipedBodyWear.isHidden = playerModel.bipedBodyWear.isHidden || SkinLayersModBase.config.enableJacket;
                playerModel.bipedLeftArmwear.isHidden = playerModel.bipedLeftArmwear.isHidden || SkinLayersModBase.config.enableLeftSleeve;
                playerModel.bipedRightArmwear.isHidden = playerModel.bipedRightArmwear.isHidden || SkinLayersModBase.config.enableRightSleeve;
                playerModel.bipedLeftLegwear.isHidden = playerModel.bipedLeftLegwear.isHidden || SkinLayersModBase.config.enableLeftPants;
                playerModel.bipedRightLegwear.isHidden = playerModel.bipedRightLegwear.isHidden || SkinLayersModBase.config.enableRightPants;
            } else {
                // not correct, but the correct way doesn't work cause 1.8 or whatever
                if (!clientPlayer.isSpectator()) {
                    playerModel.bipedHeadwear.isHidden = false;
                    playerModel.bipedBodyWear.isHidden = false;
                    playerModel.bipedLeftArmwear.isHidden = false;
                    playerModel.bipedRightArmwear.isHidden = false;
                    playerModel.bipedLeftLegwear.isHidden = false;
                    playerModel.bipedRightLegwear.isHidden = false;
                }
            }
        }
        ModelPlayer modelplayer = this.getMainModel();

        if (clientPlayer.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();

            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                modelplayer.heldItemRight = 1;

                // KA third person block animations
                boolean flag = clientPlayer instanceof EntityPlayerSP && KillAura.blocking;
                if (flag || clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (flag || enumaction == EnumAction.BLOCK) {
                        modelplayer.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
                    }
                }
            }
        }

    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity) {

        if (CustomModel.enabled) {
            switch (CustomModel.model.get()) {
                case "Rabbit":
                    return CustomModel.rabbitModel;
                case "Among Us":
                    return CustomModel.amongusModel;
            }
        }


        return entity.getLocationSkin();
    }

    public void transformHeldFull3DItemLayer() {
        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime) {
        float f = 0.9375F;
        GlStateManager.scale(f, f, f);
    }

    protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
        if (p_177069_10_ < 100.0D) {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null) {
                Score score = scoreboard.getValueFromObjective(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (double) ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * p_177069_9_);
            }
        }

        super.renderOffsetLivingLabel(entityIn, x, y, z, str, p_177069_9_, p_177069_10_);
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.swingProgress = 0.0F;
        modelplayer.isSneak = false;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderRightArm();
        if (Client.INSTANCE.getModuleManager().getModule(SkinLayers3D.class).isEnabled()) {
            renderFirstPersonArm(clientPlayer, 3);
        }
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0F;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderLeftArm();
        if (Client.INSTANCE.getModuleManager().getModule(SkinLayers3D.class).isEnabled()) {
            renderFirstPersonArm(clientPlayer, 2);
        }
    }
    private void renderFirstPersonArm(AbstractClientPlayer player, int layerId) {
        ModelPlayer modelplayer = getMainModel();
        float pixelScaling = SkinLayersModBase.config.baseVoxelSize;
        PlayerSettings settings = (PlayerSettings) player;
        if(settings.getSkinLayers() == null && !setupModel(player, settings)) {
            return;
        }
        GlStateManager.pushMatrix();
        modelplayer.bipedRightArm.postRender(0.0625F);
        GlStateManager.scale(0.0625, 0.0625, 0.0625);
        GlStateManager.scale(pixelScaling, pixelScaling, pixelScaling);
        if(!smallArms) {
            settings.getSkinLayers()[layerId].x = -0.998f*16f;
        } else {
            settings.getSkinLayers()[layerId].x = -0.499f*16;
        }
        settings.getSkinLayers()[layerId].render(false);
        GlStateManager.popMatrix();
    }

    private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, PlayerSettings settings) {

        if(!SkinUtil.hasCustomSkin(abstractClientPlayerEntity)) {
            return false; // default skin
        }
        SkinUtil.setup3dLayers(abstractClientPlayerEntity, settings, smallArms, null);
        return true;
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z) {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping()) {
            super.renderLivingAt(entityLivingBaseIn, x + (double) entityLivingBaseIn.renderOffsetX, y + (double) entityLivingBaseIn.renderOffsetY, z + (double) entityLivingBaseIn.renderOffsetZ);
        } else {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        if (bat.isEntityAlive() && bat.isPlayerSleeping()) {
            GlStateManager.rotate(bat.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        } else {
            super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
        }
    }
}
