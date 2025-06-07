package dev.tenacity.module.impl.combat;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.game.KeyPressEvent;
import com.cubk.event.impl.network.PacketReceiveEvent;
import com.cubk.event.impl.player.*;
import com.cubk.event.impl.render.Render3DEvent;
import com.google.common.base.Predicates;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import dev.tenacity.Client;
import dev.tenacity.commands.impl.FriendCommand;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.impl.movement.Scaffold;
import dev.tenacity.module.settings.impl.*;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.SmoothStepAnimation;
import dev.tenacity.utils.client.addons.rise.AimSimulator;
import dev.tenacity.utils.client.addons.rise.MovementFix;
import dev.tenacity.utils.client.addons.rise.RotationUtils;
import dev.tenacity.utils.client.addons.rise.component.RotationComponent;
import dev.tenacity.utils.client.addons.vector.Vector2f;
import dev.tenacity.utils.client.addons.viamcp.viamcp.fixes.AttackOrder;
import dev.tenacity.utils.misc.MathUtils;
import dev.tenacity.utils.player.InventoryUtils;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.server.PacketUtils;
import dev.tenacity.utils.time.TimerUtil;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjglx.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KillAura extends Module {


    public static final ModeSetting auraESP = new ModeSetting("Target ESP", "Capture", "Capture", "Round", "Circle", "Box", "Tracer", "None");
    public static final NumberSetting rotationReach = new NumberSetting("RotationReach", 5, 6, 3, 0.1);
    public static final NumberSetting reach = new NumberSetting("Reach", 3, 6, 3, 0.1);
    public static final ModeSetting movementFix = new ModeSetting("Movement fix", "Traditional", "Off", "Normal", "Traditional", "Backwards Sprint");
    public static final List<EntityLivingBase> targets = new ArrayList<>();
    private static final MultipleBoolSetting targetsSetting =
            new MultipleBoolSetting("Targets",
                    new BooleanSetting("Players", true),
                    new BooleanSetting("Animals", false),
                    new BooleanSetting("Mobs", false),
                    new BooleanSetting("Invisibles", false));
    private static final BooleanSetting ThroughWalls = new BooleanSetting("Through Walls", false);
    public static float yaw = 0.0F;
    public static boolean attacking;
    public static boolean blocking;
    public static boolean wasBlocking;
    public static EntityLivingBase target;
    public final NumberSetting switchDelay = new NumberSetting("SwitchDelay", 170, 1000.0, 0.0, 1.0);
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private final ModeSetting mode = new ModeSetting("Mode", "Single", "Single", "Switch");
    private final BooleanSetting rotations = new BooleanSetting("Rotations", true);
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation speed", 5, 10, 2, 0.1);
    private final ModeSetting rotationMode = new ModeSetting("Rotation Mode", "HvH", "HvH", "Vanilla", "Nearest", "TestA", "TestB", "TestC");
    private final BooleanSetting autoBlock = new BooleanSetting("AutoBlock", false);
    private final ModeSetting autoBlockMode = new ModeSetting("AutoBlock Mode", "Grim", "Fake", "Grim", "Watchdog", "Grimidk");
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", "Range", "Range", "Hurt Time", "Health", "Armor");
    private final NumberSetting minCPS = new NumberSetting("Min CPS", 10, 20, 1, 1);
    private final NumberSetting maxCPS = new NumberSetting("Max CPS", 20, 20, 1, 1);
    private final BooleanSetting KeepSprint = new BooleanSetting("Keep Sprint", false);
    private final BooleanSetting RayCast = new BooleanSetting("Ray Cast", false);
    private final Animation auraESPAnim = new SmoothStepAnimation(650, 1);
    private final String[] swapBlacklist = {"compass", "snowball", "spawn", "skull"};
    public BooleanSetting altSwitch = new BooleanSetting("LAlt Switch Strafe", false);
    private int cps;
    private int targetIndex = 0;
    private EntityLivingBase auraESPTarget;

    public KillAura() {
        super("module.combat.killAura", Category.COMBAT, "Automatically attacks players");
        addSettings(maxCPS, minCPS, movementFix, rotationReach, reach, targetsSetting, ThroughWalls, mode, switchDelay, rotations, rotationMode, rotationSpeed, autoBlock, autoBlockMode, sortMode, KeepSprint, RayCast, altSwitch, auraESP);
        autoBlockMode.addParent(autoBlock, a -> autoBlock.isEnabled());
        switchDelay.addParent(mode, m -> mode.is("Switch"));
        altSwitch.addParent(movementFix, d -> !movementFix.is("None"));
        rotationMode.addParent(rotations, BooleanSetting::isEnabled);
        rotationSpeed.addParent(rotations, BooleanSetting::isEnabled);
    }

    public static boolean isValid(Entity entity) {
        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Players").isEnabled() && !entity.isInvisible() && mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityPlayer && targetsSetting.getSetting("Invisibles").isEnabled() && entity.isInvisible())
            return true;

        if (entity instanceof EntityPlayer && ThroughWalls.isEnabled() && !mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityAnimal && targetsSetting.getSetting("Animals").isEnabled())
            return true;

        if (entity instanceof EntityMob && targetsSetting.getSetting("Mobs").isEnabled())
            return true;

        if (entity.isInvisible() && targetsSetting.getSetting("Invisibles").isEnabled())
            return true;

        return false;
    }

    public static MovingObjectPosition rayTraceCustom(double blockReachDistance, float yaw, float pitch) {
        // 获取玩家眼睛位置的起始向量
        Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0f);

        // 根据 yaw 和 pitch 获取方向向量
        Vec3 vec31 = mc.thePlayer.getVectorForRotation(yaw, pitch);

        // 计算终点向量 = 起点 + 方向向量 * 距离
        Vec3 vec32 = vec3.addVector(
                vec31.xCoord * blockReachDistance,
                vec31.yCoord * blockReachDistance,
                vec31.zCoord * blockReachDistance
        );

        // 进行方块射线追踪，参数为：起点、终点，不忽略实体、不检查液体、精确检测边缘
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    private void attack() {
        if (target != null) {
            attackEntity(target);
            if (mc.thePlayer.fallDistance > 0.0f && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null) {
                mc.thePlayer.onCriticalHit(target);
            }
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), target.getCreatureAttribute()) > 0.0f) {
                mc.thePlayer.onEnchantmentCritical(target);
                PacketUtils.sendPacket(new C0APacketAnimation());
            }
        }
    }

    private void attackEntity(final Entity target) {
        AttackOrder.sendFixedAttack(mc.thePlayer, target);
        attackTimer.reset();
    }

    @EventTarget
    public void onKeyPressEvent(KeyPressEvent event) {
        if (event.getKey() == Keyboard.KEY_LMENU && (altSwitch.isEnabled()) && !movementFix.is("None")) {
            if (movementFix.is("Normal")) {
                movementFix.setCurrentMode("Traditional");
            } else {
                movementFix.setCurrentMode("Normal");
            }
            NotificationManager.post(NotificationType.SUCCESS, "MovementCorrection", "Changed to " + (movementFix.is("Traditional") ? "Strict" : "Silent"));
        }
    }

    @Override
    public void onDisable() {
        target = null;
        targets.clear();
        attacking = false;
        blocking = false;
        if (wasBlocking) {
            if (autoBlockMode.is("Grim") || autoBlockMode.is("Grimidk")) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                mc.playerController.onStoppedUsingItem(KillAura.mc.thePlayer);
                KeyBinding.setKeyBindState(KillAura.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
        }
        wasBlocking = false;
        super.onDisable();
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        setSuffix(mode.getMode());

        if (minCPS.getValue() > maxCPS.getValue()) {
            minCPS.setValue(minCPS.getValue() - 1);
        }

        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) return;
        // Gets all entities in specified range, sorts them using your specified sort mode, and adds them to target list

        sortTargets();
        if (target == null) {
            yaw = mc.thePlayer.rotationYaw;
        }

        if (event.isPre()) {
            attacking = !targets.isEmpty();
            blocking = autoBlock.isEnabled() && attacking && InventoryUtils.isHoldingSword();
            if (attacking) {
                if (mode.is("Switch")) {
                    if (switchTimer.hasTimeElapsed(switchDelay.getValue().intValue(), true)) {
                        targetIndex = (targetIndex + 1) % targets.size();
                    }
                    if (targetIndex < targets.size()) {
                        target = targets.get(targetIndex);
                    } else {
                        target = null;
                    }
                } else {
                    if (!targets.isEmpty()) {
                        target = targets.get(0);
                    } else {
                        target = null;
                    }
                }

                if (rotations.isEnabled()) {
                    float[] rotations = new float[]{0, 0};
                    final double minRotationSpeed = rotationSpeed.getValue();
                    final double maxRotationSpeed = rotationSpeed.getValue();
                    final float rotationSpeed = (float) MathUtils.getRandom(minRotationSpeed, maxRotationSpeed);
                    switch (rotationMode.getMode()) {
                        case "TestC":
                            if (target != null) {
                                Vector2f vector2f = RotationUtils.getRotations(target);
                                rotations = new float[]{vector2f.x, vector2f.y};
                            }
                            break;
                        case "TestB":
                            if (target != null) {
                                double yDist = KillAura.target.posY - KillAura.mc.thePlayer.posY;
                                dev.tenacity.utils.client.addons.vector.Vector3d targetPos = yDist >= 1.7 ? new dev.tenacity.utils.client.addons.vector.Vector3d(KillAura.target.posX, KillAura.target.posY, KillAura.target.posZ) : (yDist <= -1.7 ? new dev.tenacity.utils.client.addons.vector.Vector3d(KillAura.target.posX, KillAura.target.posY + (double) target.getEyeHeight(), KillAura.target.posZ) : new dev.tenacity.utils.client.addons.vector.Vector3d(KillAura.target.posX, KillAura.target.posY + (double) (target.getEyeHeight() / 2.0f), KillAura.target.posZ));
                                Vector2f temp = RotationUtils.getRotationFromEyeToPoint(targetPos);
                                rotations = new float[]{temp.getX(), temp.getY()};
                            }
                            break;
                        case "TestA":
                            if (target != null) {
                                Vector2f vector2f = RotationUtils.calculate(target, true, reach.getValue().floatValue(), reach.getValue().floatValue(), true, true);
                                rotations = new float[]{vector2f.x, vector2f.y};
                            }
                            break;
                        case "HvH":
                            if (target != null) {
                                rotations = RotationUtils.getHVHRotation(target);
                            }
                            break;
                        case "Vanilla":
                            if (target != null) {
                                rotations = RotationUtils.getRotationsNeeded(KillAura.target);
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

                    }
                    RotationComponent.setRotations(new Vector2f(rotations[0], rotations[1]), rotationSpeed, MovementFix.values()[movementFix.modes.indexOf(movementFix.getMode())]);
                }

                if (!RotationComponent.isRotationg || mc.thePlayer.getDistanceToEntity(target) > reach.getValue() || (RayCast.isEnabled() && !RotationUtils.isMouseOver(event.getYaw(), event.getPitch(), target, reach.getValue().floatValue())))
                    return;

                if (attackTimer.hasTimeElapsed(cps, true)) {
                    final int maxValue = (int) ((minCPS.getMaxValue() - maxCPS.getValue()) * 5.0);
                    final int minValue = (int) ((minCPS.getMaxValue() - minCPS.getValue()) * 5.0);
                    cps = MathUtils.getRandomInRange(minValue, maxValue);
                    AttackEvent attackEvent = new AttackEvent(target);
                    Client.INSTANCE.getEventManager().call(attackEvent);
                    attack();
                    AttackEvent attackEvent2 = new AttackEvent(target);
                    Client.INSTANCE.getEventManager().call(attackEvent2);
                }

            } else {
                attackTimer.reset();
                target = null;
            }
        }
        if (!RotationComponent.isRotationg) return;

        if (blocking) {
            switch (autoBlockMode.getMode()) {
                case "Grimidk":
                    if (event.isPre()) {
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        }
                    }
                case "Grim":
                    if (event.isPost()) {
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());

                            PacketWrapper useItem_1_9 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem_1_9.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItem_1_9, Protocol1_8To1_9.class, true, true);

                            PacketWrapper useItem_1_9_2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem_1_9_2.write(Type.VAR_INT, 0);
                            PacketUtil.sendToServer(useItem_1_9_2, Protocol1_8To1_9.class, true, true);

                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                            wasBlocking = true;
                        }
                    }
                    break;
                case "Fake":
                    break;
                case "Watchdog":
                    if (event.isPre()) {
                        if (wasBlocking && mc.thePlayer.ticksExisted % 4 == 0) {
                            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            wasBlocking = false;
                        }

                        if (!wasBlocking) {
                            int bestSlot = getBestSwapSlot();
                            if (bestSlot != -1) {
                                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(bestSlot));
                                PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(bestSlot)));
                                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                wasBlocking = true;
                            }
                        }
                    }
            }
        } else if (wasBlocking && autoBlockMode.is("Grim")) {
            wasBlocking = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
            mc.playerController.onStoppedUsingItem(KillAura.mc.thePlayer);
            KeyBinding.setKeyBindState(KillAura.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        } else if (wasBlocking && autoBlockMode.is("Watchdog")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    private void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase entityLivingBase) {
                if (mc.thePlayer.getDistanceToEntity(entity) <= rotationReach.getValue() && isValid(entity) && mc.thePlayer != entityLivingBase && !FriendCommand.isFriend(entityLivingBase.getName())) {
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
    private int getBestSwapSlot() {
        int currentSlot = mc.thePlayer.inventory.currentItem;
        int bestSlot = -1;

        // First try to find a weapon
        for (int i = 0; i < 9; ++i) {
            if (i == currentSlot) continue;
            if (InventoryUtils.isWeapon(mc.thePlayer.inventory.getStackInSlot(i))) {
                bestSlot = i;
                break;
            }
        }

        // If no weapon found, find any non-blacklisted item
        if (bestSlot == -1) {
            for (int i = 0; i < 9; ++i) {
                if (i == currentSlot) continue;
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && !isBlacklisted(stack)) {
                    bestSlot = i;
                    break;
                }
            }
        }

        return bestSlot;
    }

    private boolean isBlacklisted(ItemStack stack) {
        String unlocalizedName = stack.getUnlocalizedName().toLowerCase();
        for (String blacklisted : swapBlacklist) {
            if (unlocalizedName.contains(blacklisted)) {
                return true;
            }
        }
        return false;
    }

    private MovingObjectPosition rayTrace(double range, float partialTicks, float[] rotations) {
        Entity targetEntity = null;
        MovingObjectPosition hitObject;
        double d0 = range;

        // 先进行一次自定义视线追踪（方块）
        hitObject = rayTraceCustom(d0, rotations[0], rotations[1]);
        double d1 = d0;

        // 获取玩家当前视角的眼睛位置
        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);

        // 如果玩家拥有 extended reach（远程攻击范围）
        if (mc.playerController.extendedReach()) {
            d0 = 6.0;
            d1 = 6.0;
        }

        // 如果方块追踪命中，修正最大实体检测距离
        if (hitObject != null) {
            d1 = hitObject.hitVec.distanceTo(vec3);
        }

        // 计算朝向向量（视角）
        Vec3 vec31 = RotationUtils.getVectorForRotation(rotations[0], rotations[1]);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);

        Vec3 vec33 = null;
        float f = 1.0F;

        // 获取追踪路径上的所有实体（排除玩家本身和旁观模式）
        List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(
                mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(
                        vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0
                ).expand(f, f, f),
                Predicates.and(EntitySelectors.NOT_SPECTATING)
        );

        double d2 = d1;

        for (Entity entity1 : list) {
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);

            MovingObjectPosition intercept = axisalignedbb.calculateIntercept(vec3, vec32);

            if (axisalignedbb.isVecInside(vec3)) {
                if (d2 >= 0.0) {
                    targetEntity = entity1;
                    vec33 = (intercept == null) ? vec3 : intercept.hitVec;
                    d2 = 0.0;
                }
            } else if (intercept != null) {
                double d3 = vec3.distanceTo(intercept.hitVec);
                if (d3 < d2 || d2 == 0.0) {
                    // 如果是骑乘实体并不能交互，跳过
                    if (entity1 == mc.thePlayer.ridingEntity) {
                        if (d2 == 0.0) {
                            targetEntity = entity1;
                            vec33 = intercept.hitVec;
                        }
                    } else {
                        targetEntity = entity1;
                        vec33 = intercept.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        // 如果实体比方块更接近，优先返回实体命中
        if (targetEntity != null && d2 < d1) {
            return new MovingObjectPosition(targetEntity, vec33);
        }

        return null;
    }

    @EventTarget
    public void onKeepSprintEvent(KeepSprintEvent event) {
        if (KeepSprint.isEnabled()) {
            event.cancel();
        }
    }

    @EventTarget
    public void onAttackEvent(AttackEvent event) {
        if (event.getTargetEntity() != null) {
            try {
                auraESPTarget = event.getTargetEntity();
            } catch (ClassCastException e) {
                if (auraESPAnim.finished(Direction.BACKWARDS)) {
                    auraESPTarget = null;
                }
            }
        }
    }

    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S2FPacketSetSlot s2f && autoBlockMode.is("Grimidk") && s2f.getItem().getItem() instanceof ItemSword) {
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) {
                event.setCancelled(true);
            }
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
        Color color2 = HUDMod.getClientColors().getSecond();
        float dst = mc.thePlayer.getSmoothDistanceToEntity(target);

        switch (auraESP.getMode()) {
            case "Box":
                if (target == null) {
                    return;
                }
                RenderUtil.renderBoundingBox(auraESPTarget, color, auraESPAnim.getOutput().floatValue());
                break;


            case "Circle":
                if (target == null) {
                    return;
                }
                RenderUtil.drawCircle(auraESPTarget, event.getTicks(), .75f, color.getRGB(), auraESPAnim.getOutput().floatValue());
                break;

            case "Tracer":
                if (target == null) {
                    return;
                }
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, auraESPAnim.getOutput().floatValue());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, color, auraESPAnim.getOutput().floatValue());
                break;

            case "Round":
            case "Capture":
                if (target == null) {
                    return;
                }
                javax.vecmath.Vector2f vector2f = RenderUtil.targetESPSPos(target);
                if (vector2f == null) break;
                RenderUtil.drawTargetESP2D(vector2f.x, vector2f.y, color, color2, 1.0f - MathHelper.clamp_float(Math.abs(dst - 6.0f) / 60.0f, 0.0f, 0.75f), 1, auraESPAnim.getOutput().floatValue());
                break;
        }
    }
}