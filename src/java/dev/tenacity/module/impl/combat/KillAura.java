package dev.tenacity.module.impl.combat;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.player.AttackEvent;
import com.cubk.event.impl.player.KeepSprintEvent;
import com.cubk.event.impl.player.MotionEvent;
import com.cubk.event.impl.render.Render3DEvent;
import dev.tenacity.Client;
import dev.tenacity.commands.impl.FriendCommand;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.impl.movement.Scaffold;
import dev.tenacity.module.settings.impl.*;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.DecelerateAnimation;
import dev.tenacity.utils.client.addons.rise.AimSimulator;
import dev.tenacity.utils.client.addons.rise.MovementFix;
import dev.tenacity.utils.client.addons.rise.component.RotationComponent;
import dev.tenacity.utils.client.addons.vector.Vector2f;
import dev.tenacity.utils.client.addons.vector.Vector3d;
import dev.tenacity.utils.client.addons.viamcp.viamcp.fixes.AttackOrder;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.player.InventoryUtils;
import dev.tenacity.utils.player.RotationUtils;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.server.PacketUtils;
import dev.tenacity.utils.time.TimerUtil;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KillAura extends Module {

    public static final List<EntityLivingBase> targets = new ArrayList<>();
    public static final ModeSetting movementFix = new ModeSetting("Movement fix Mode", "Traditional", "Off", "Normal", "Traditional", "Backwards Sprint");
    public static boolean attacking;
    public static boolean blocking;
    public static boolean wasBlocking;
    public static EntityLivingBase target;
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private final MultipleBoolSetting targetsSetting = new MultipleBoolSetting("Targets",
            new BooleanSetting("Players", true),
            new BooleanSetting("Animals", false),
            new BooleanSetting("Mobs", false),
            new BooleanSetting("Invisibles", false));
    private final ModeSetting mode = new ModeSetting("Mode", "Single", "Single", "Multi");
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 50, 500, 1, 1);
    private final NumberSetting maxTargetAmount = new NumberSetting("Max Target Amount", 3, 50, 2, 1);
    private final NumberSetting minCPS = new NumberSetting("Min CPS", 10, 20, 1, 1);
    private final NumberSetting maxCPS = new NumberSetting("Max CPS", 10, 20, 1, 1);
    private final NumberSetting reach = new NumberSetting("Reach", 4, 6, 3, 0.1);
    private final BooleanSetting autoblock = new BooleanSetting("Autoblock", false);
    private final ModeSetting autoblockMode = new ModeSetting("Autoblock Mode", "Watchdog", "Fake", "Verus", "Watchdog");
    private final BooleanSetting rotations = new BooleanSetting("Rotations", true);
    private final ModeSetting rotationMode = new ModeSetting("Rotation Mode", "Vanilla", "Vanilla", "Smooth");
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation speed", 5, 10, 2, 0.1);
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", "Range", "Range", "Hurt Time", "Health", "Armor");
    private final MultipleBoolSetting addons = new MultipleBoolSetting("Addons",
            new BooleanSetting("Keep Sprint", true),
            new BooleanSetting("Through Walls", true),
            new BooleanSetting("Allow Scaffold", false),
            new BooleanSetting("Movement Fix", false),
            new BooleanSetting("Ray Cast", false));
    private final MultipleBoolSetting auraESP = new MultipleBoolSetting("Target ESP",
            new BooleanSetting("Circle", true),
            new BooleanSetting("Tracer", false),
            new BooleanSetting("Box", false),
            new BooleanSetting("Custom Color", false));
    private final ColorSetting customColor = new ColorSetting("Custom Color", Color.WHITE);
    private final Animation auraESPAnim = new DecelerateAnimation(300, 1);
    private final float yaw = 0;
    private int cps;
    private EntityLivingBase auraESPTarget;

    public KillAura() {
        super("KillAura", Category.COMBAT, "Automatically attacks players");
        autoblockMode.addParent(autoblock, a -> autoblock.isEnabled());
        rotationMode.addParent(rotations, r -> rotations.isEnabled());
        rotationSpeed.addParent(rotations, r -> rotations.isEnabled());
        switchDelay.addParent(mode, m -> mode.is("Switch"));
        maxTargetAmount.addParent(mode, m -> mode.is("Multi"));
        customColor.addParent(auraESP, r -> r.isEnabled("Custom Color"));
        movementFix.addParent(addons, r -> r.isEnabled("Movement Fix"));
        this.addSettings(targetsSetting, mode, maxTargetAmount, switchDelay, minCPS, maxCPS, reach, autoblock, autoblockMode,
                rotations, rotationMode, rotationSpeed, sortMode, addons, movementFix, auraESP, customColor);
    }

    @Override
    public void onDisable() {
        target = null;
        targets.clear();
        blocking = false;
        attacking = false;
        if (wasBlocking) {
            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
        wasBlocking = false;
        super.onDisable();
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        this.setSuffix(mode.getMode());
        final double minRotationSpeed = rotationSpeed.getValue();
        final double maxRotationSpeed = rotationSpeed.getValue();
        final float rotationSpeed = (float) MathUtils.getRandom(minRotationSpeed, maxRotationSpeed);
        if (minCPS.getValue() > maxCPS.getValue()) {
            minCPS.setValue(minCPS.getValue() - 1);
        }

        // Gets all entities in specified range, sorts them using your specified sort mode, and adds them to target list
        sortTargets();

        if (event.isPre()) {
            attacking = !targets.isEmpty() && (addons.getSetting("Allow Scaffold").isEnabled() || !Client.INSTANCE.isEnabled(Scaffold.class));
            blocking = autoblock.isEnabled() && attacking && InventoryUtils.isHoldingSword();
            if (attacking) {
                target = targets.get(0);

                if (rotations.isEnabled()) {
                    float[] rotations = new float[]{0, 0};
                    switch (rotationMode.getMode()) {
                        case "Vanilla":
                            rotations = RotationUtils.getRotationsNeeded(target);
                            break;
                        case "Smooth":
                            rotations = RotationUtils.getSmoothRotations(target);
                            break;
                        case "TestC":
                            if (target != null) {
                                Vector2f vector2f = dev.tenacity.utils.client.addons.rise.RotationUtils.getRotations(target);
                                rotations = new float[]{vector2f.x, vector2f.y};
                            }
                            break;
                        case "TestB":
                            if (target != null) {
                                double yDist = KillAura.target.posY - KillAura.mc.thePlayer.posY;
                                Vector3d targetPos = yDist >= 1.7 ? new Vector3d(KillAura.target.posX, KillAura.target.posY, KillAura.target.posZ) : (yDist <= -1.7 ? new Vector3d(KillAura.target.posX, KillAura.target.posY + (double) target.getEyeHeight(), KillAura.target.posZ) : new Vector3d(KillAura.target.posX, KillAura.target.posY + (double) (target.getEyeHeight() / 2.0f), KillAura.target.posZ));
                                Vector2f temp = dev.tenacity.utils.client.addons.rise.RotationUtils.getRotationFromEyeToPoint(targetPos);
                                rotations = new float[]{temp.getX(), temp.getY()};
                            }
                            break;
                        case "TestA":
                            if (target != null) {
                                Vector2f vector2f = dev.tenacity.utils.client.addons.rise.RotationUtils.calculate(target, true, reach.getValue().floatValue(), reach.getValue().floatValue(), true, true);
                                rotations = new float[]{vector2f.x, vector2f.y};
                            }
                            break;
                        case "HvH":
                            if (target != null) {
                                rotations = dev.tenacity.utils.client.addons.rise.RotationUtils.getHVHRotation(target);
                            }
                            break;
                        case "Nearest":
                            if (target != null) {
                                Pair<Float, Float> aimResult = AimSimulator.getLegitAim(target, mc.thePlayer, true, true, false, null, 0);
                                rotations = new float[]{
                                        aimResult.getFirst(),
                                        aimResult.getSecond()
                                };
                                break;

                            }
                            break;
                    }

                    RotationComponent.setRotations(new Vector2f(rotations[0], rotations[1]), rotationSpeed, MovementFix.values()[movementFix.modes.indexOf(movementFix.getMode())]);
                }

                if (!RotationComponent.isRotationg || mc.thePlayer.getDistanceToEntity(target) > reach.getValue() || (addons.isEnabled("Ray Cast") && !RotationUtils.isMouseOver(event.getYaw(), event.getPitch(), target, reach.getValue().floatValue())))
                    return;

                if (attackTimer.hasTimeElapsed(cps, true)) {
                    final int maxValue = (int) ((minCPS.getMaxValue() - maxCPS.getValue()) * 20);
                    final int minValue = (int) ((minCPS.getMaxValue() - minCPS.getValue()) * 20);
                    cps = MathUtils.getRandomInRange(minValue, maxValue);
                    if (mode.is("Multi")) {
                        for (EntityLivingBase entityLivingBase : targets) {
                            AttackEvent attackEvent = new AttackEvent(entityLivingBase);
                            Client.INSTANCE.getEventManager().register(attackEvent);

                            if (!attackEvent.isCancelled()) {
                                AttackOrder.sendFixedAttack(mc.thePlayer, entityLivingBase);
                            }
                        }
                    } else {
                        AttackEvent attackEvent = new AttackEvent(target);
                        Client.INSTANCE.getEventManager().register(attackEvent);

                        if (!attackEvent.isCancelled()) {
                            AttackOrder.sendFixedAttack(mc.thePlayer, target);
                        }
                    }
                }

            } else {
                target = null;
                switchTimer.reset();
            }
        }

        if (blocking) {
            switch (autoblockMode.getMode()) {
                case "Watchdog":
                    if (event.isPre() && wasBlocking && mc.thePlayer.ticksExisted % 4 == 0) {
                        PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        wasBlocking = false;
                    }

                    if (event.isPost() && !wasBlocking) {
                        PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.getHeldItem(), 255, 255, 255));
                        wasBlocking = true;
                    }
                    break;
                case "Verus":
                    if (event.isPre()) {
                        if (wasBlocking) {
                            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.
                                    Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }
                        PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        wasBlocking = true;
                    }
                    break;
                case "Fake":
                    break;
            }
        } else if (wasBlocking && autoblockMode.is("Watchdog") && event.isPre()) {
            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.
                    Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            wasBlocking = false;
        }
    }

    private void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase entityLivingBase) {
                if (mc.thePlayer.getDistanceToEntity(entity) <= reach.getValue() && isValid(entity) && mc.thePlayer != entityLivingBase && !FriendCommand.isFriend(entityLivingBase.getName())) {
                    targets.add(entityLivingBase);
                }
            }
        }
        switch (sortMode.getMode()) {
            case "Range":
                targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));
                break;
            case "Hurt Time":
                targets.sort(Comparator.comparingInt(EntityLivingBase::getHurtTime));
                break;
            case "Health":
                targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                break;
            case "Armor":
                targets.sort(Comparator.comparingInt(EntityLivingBase::getTotalArmorValue));
                break;
        }
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Players").isEnabled() && !entity.isInvisible() && mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Invisibles").isEnabled() && entity.isInvisible())
            return true;

        if (entity instanceof EntityPlayer && addons.getSetting("Through Walls").isEnabled() && !mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityAnimal && targetsSetting.getSetting("Animals").isEnabled())
            return true;

        if (entity instanceof EntityMob && targetsSetting.getSetting("Mobs").isEnabled())
            return true;

        return entity.isInvisible() && targetsSetting.getSetting("Invisibles").isEnabled();
    }

    @EventTarget
    public void onKeepSprintEvent(KeepSprintEvent event) {
        if (addons.getSetting("Keep Sprint").isEnabled()) {
            event.cancel();
        }
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent event) {
        auraESPAnim.setDirection(target != null ? Direction.FORWARDS : Direction.BACKWARDS);
        if (target != null) {
            auraESPTarget = target;
        }

        if (auraESPAnim.finished(Direction.BACKWARDS)) {
            auraESPTarget = null;
        }

        Color color = HUDMod.getClientColors().getFirst();

        if (auraESP.isEnabled("Custom Color")) {
            color = customColor.getColor();
        }


        if (auraESPTarget != null) {
            if (auraESP.getSetting("Box").isEnabled()) {
                RenderUtil.renderBoundingBox(auraESPTarget, color, auraESPAnim.getOutput().floatValue());
            }
            if (auraESP.getSetting("Circle").isEnabled()) {
                RenderUtil.drawCircle(auraESPTarget, event.getTicks(), .75f, color.getRGB(), auraESPAnim.getOutput().floatValue());
            }

            if (auraESP.getSetting("Tracer").isEnabled()) {
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, auraESPAnim.getOutput().floatValue());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, color, auraESPAnim.getOutput().floatValue());
            }
        }
    }
}
