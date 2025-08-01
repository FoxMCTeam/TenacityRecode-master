package dev.tenacity.module.impl.combat;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.AttackEvent;
import dev.tenacity.event.impl.player.KeepSprintEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.render.Render3DEvent;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import dev.tenacity.Client;
import dev.tenacity.commands.impl.FriendCommand;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.impl.display.SettingComponent;
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
import de.florianmichael.viamcp.fixes.AttackOrder;
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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class KillAura extends Module {

    public static final List<EntityLivingBase> targets = new ArrayList<>();
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
    private final BooleanSetting swapPreBlock = new BooleanSetting("Swap-PreBlock", false);
    private final BooleanSetting rotations = new BooleanSetting("Rotations", true);
    private final ModeSetting rotationMode = new ModeSetting("Rotation Mode", "Vanilla", "HvH", "Vanilla", "Nearest", "TestA", "TestB", "TestC", "Smooth");
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation speed", 5, 10, 2, 0.1);
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", "Range", "Range", "Hurt Time", "Health", "Armor");
    private final MultipleBoolSetting addons = new MultipleBoolSetting("Addons",
            new BooleanSetting("Keep Sprint", true),
            new BooleanSetting("Through Walls", true),
            new BooleanSetting("Allow Scaffold", false),
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
    private final List<Item> throwableList = Arrays.asList(
            Items.snowball,
            Items.egg,
            Items.ender_pearl,
            Items.ender_eye,
            Items.fire_charge,
            Items.water_bucket,
            Items.lava_bucket,
            Items.prismarine_shard,
            Items.fishing_rod,
            Items.gunpowder
    );

    public KillAura() {
        super("module.combat.killAura", Category.COMBAT, "Automatically attacks players");
        autoblockMode.addParent(autoblock, a -> autoblock.get());
        rotationMode.addParent(rotations, r -> rotations.get());
        rotationSpeed.addParent(rotations, r -> rotations.get());
        switchDelay.addParent(mode, m -> mode.is("Switch"));
        maxTargetAmount.addParent(mode, m -> mode.is("Multi"));
        customColor.addParent(auraESP, r -> r.get("Custom Color"));
        this.addSettings(targetsSetting, mode, maxTargetAmount, switchDelay, minCPS, maxCPS, reach, autoblock, autoblockMode, swapPreBlock,
                rotations, rotationMode, rotationSpeed, sortMode, addons, auraESP, customColor);
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
        this.setSuffix(mode.get());
        final double minRotationSpeed = rotationSpeed.get();
        final double maxRotationSpeed = rotationSpeed.get();
        final float rotationSpeed = (float) MathUtils.getRandom(minRotationSpeed, maxRotationSpeed);
        if (minCPS.get() > maxCPS.get()) {
            minCPS.set(minCPS.get() - 1);
        }

        // Gets all entities in specified range, sorts them using your specified sort mode, and adds them to target list
        sortTargets();

        if (event.isPre()) {
            attacking = !targets.isEmpty() && (addons.getSetting("Allow Scaffold").get() || !Client.INSTANCE.isEnabled(Scaffold.class));
            blocking = autoblock.get() && attacking && InventoryUtils.isHoldingSword();
            if (attacking) {
                target = targets.get(0);

                if (rotations.get()) {
                    float[] rotations = new float[]{0, 0};
                    switch (rotationMode.get()) {
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
                                Vector2f vector2f = dev.tenacity.utils.client.addons.rise.RotationUtils.calculate(target, true, reach.get().floatValue(), reach.get().floatValue(), true, true);
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

                    RotationComponent.setRotations(new Vector2f(rotations[0], rotations[1]), rotationSpeed, MovementFix.values()[SettingComponent.kAMovementFixMode.modes.indexOf(SettingComponent.kAMovementFixMode.get())]);
                }

                if (addons.getSetting("Ray Cast").get() && !RotationUtils.isMouseOver(event.getYaw(), event.getPitch(), target, reach.get().floatValue()))
                    return;

                if (attackTimer.hasTimeElapsed(cps, true)) {
                    final int maxValue = (int) ((minCPS.getMaxValue() - maxCPS.get()) * 20);
                    final int minValue = (int) ((minCPS.getMaxValue() - minCPS.get()) * 20);
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
            switch (autoblockMode.get()) {
                case "Watchdog":
                    for (int i = 0; i <= 8; i++) {
                        if (mc.thePlayer.inventory.getStackInSlot(i) != null) {
                            if (!throwableList.contains(mc.thePlayer.inventory.getStackInSlot(i).getItem()) && i != mc.thePlayer.inventory.currentItem) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(i));
                                break;
                            }
                        }
                    }
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                    if (addons.getSetting("Ray Cast").get() && !RotationUtils.isMouseOver(event.getYaw(), event.getPitch(), target, reach.get().floatValue()))
                        return;

                    if (attackTimer.hasTimeElapsed(cps, true)) {
                        final int maxValue = (int) ((minCPS.getMaxValue() - maxCPS.get()) * 20);
                        final int minValue = (int) ((minCPS.getMaxValue() - minCPS.get()) * 20);
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

                    sendInteractPacket(target);
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    if (!mc.isSingleplayer()) {
                        PacketWrapper blockPlace = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                        blockPlace.write(
                                Types.VAR_INT,
                                1
                        );
                        try {
                            blockPlace.sendToServer(Protocol1_8To1_9.class,true);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    wasBlocking = true;
                    break;
                case "Verus":
                    if (event.isPre()) {
                        PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        wasBlocking = true;
                    }
                    break;
                case "Fake":
                    break;
            }
        } else if (wasBlocking && autoblockMode.is("Watchdog") && event.isPre()) {
            wasBlocking = false;
        }
    }

    private void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase entityLivingBase) {
                if (mc.thePlayer.getDistanceToEntity(entity) <= reach.get() && isValid(entity) && mc.thePlayer != entityLivingBase && !FriendCommand.isFriend(entityLivingBase.getName())) {
                    targets.add(entityLivingBase);
                }
            }
        }
        switch (sortMode.get()) {
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
        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Players").get() && !entity.isInvisible() && mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Invisibles").get() && entity.isInvisible())
            return true;

        if (entity instanceof EntityPlayer && addons.getSetting("Through Walls").get() && !mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityAnimal && targetsSetting.getSetting("Animals").get())
            return true;

        if (entity instanceof EntityMob && targetsSetting.getSetting("Mobs").get())
            return true;

        return entity.isInvisible() && targetsSetting.getSetting("Invisibles").get();
    }

    @EventTarget
    public void onKeepSprintEvent(KeepSprintEvent event) {
        if (addons.getSetting("Keep Sprint").get()) {
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

        if (auraESP.get("Custom Color")) {
            color = customColor.get();
        }


        if (auraESPTarget != null) {
            if (auraESP.getSetting("Box").get()) {
                RenderUtil.renderBoundingBox(auraESPTarget, color, auraESPAnim.getOutput().floatValue());
            }
            if (auraESP.getSetting("Circle").get()) {
                RenderUtil.drawCircle(auraESPTarget, event.getTicks(), .75f, color.getRGB(), auraESPAnim.getOutput().floatValue());
            }

            if (auraESP.getSetting("Tracer").get()) {
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, auraESPAnim.getOutput().floatValue());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, color, auraESPAnim.getOutput().floatValue());
            }
        }
    }

    private void sendInteractPacket(Entity target) {
        if (KillAura.target != null) {
            // 计算射线追踪
            MovingObjectPosition hitObject = rayTrace(
                    this.reach.get().floatValue(),
                    mc.timer.renderPartialTicks,
                    new float[]{RotationComponent.rotations.getX(), RotationComponent.rotations.getY()}
            );
            // 如果命中目标实体
            if (hitObject != null &&
                    hitObject.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY &&
                    hitObject.entityHit == target) {
                // 计算命中位置相对于实体的坐标
                Vec3 hitVec = hitObject.hitVec;
                Vec3 relativeHitVec = new Vec3(
                        hitVec.xCoord - target.posX,
                        hitVec.yCoord - target.posY,
                        hitVec.zCoord - target.posZ
                );
                // 发送攻击数据包
                PacketUtils.sendPacketNoEvent(
                        new C02PacketUseEntity(target, relativeHitVec)
                );
            }
        }

    }

    // 辅助方法 - 射线追踪
    private MovingObjectPosition rayTrace(double range, float partialTicks, float[] rotations) {
        Vec3 startVec = new Vec3(
                mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ
        );

        float yaw = rotations[0];
        float pitch = rotations[1];

        float yawRad = (float)Math.toRadians(-yaw);
        float pitchRad = (float)Math.toRadians(-pitch);

        float cosYaw = (float)Math.cos(yawRad);
        float sinYaw = (float)Math.sin(yawRad);
        float cosPitch = (float)Math.cos(pitchRad);
        float sinPitch = (float)Math.sin(pitchRad);

        Vec3 lookVec = new Vec3(
                sinYaw * cosPitch,
                sinPitch,
                cosYaw * cosPitch
        );

        Vec3 endVec = startVec.addVector(
                lookVec.xCoord * range,
                lookVec.yCoord * range,
                lookVec.zCoord * range
        );

        return mc.theWorld.rayTraceBlocks(startVec, endVec, false, false, true);
    }
}
