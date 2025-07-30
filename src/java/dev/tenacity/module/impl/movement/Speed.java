package dev.tenacity.module.impl.movement;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.event.impl.player.MoveEvent;
import dev.tenacity.event.impl.player.PlayerMoveUpdateEvent;
import dev.tenacity.Client;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.combat.TargetStrafe;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.player.MovementUtils;
import dev.tenacity.utils.server.PacketUtils;
import dev.tenacity.utils.time.TimerUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.floor;

@SuppressWarnings("unused")
public final class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog",
            "Watchdog", "Strafe", "Matrix", "HurtTime", "Vanilla", "BHop", "Verus", "Viper", "Vulcan", "Zonecraft", "Heatseeker", "Mineland");
    private final ModeSetting watchdogMode = new ModeSetting("Watchdog Mode", "Hop", "Hop", "Dev", "Low Hop", "Ground");
    private final ModeSetting verusMode = new ModeSetting("Verus Mode", "Normal", "Low", "Normal");
    private final ModeSetting viperMode = new ModeSetting("Viper Mode", "Normal", "High", "Normal");
    private final BooleanSetting autoDisable = new BooleanSetting("Auto Disable", false);
    private final NumberSetting groundSpeed = new NumberSetting("Ground Speed", 2, 5, 1, 0.1);
    private final NumberSetting timer = new NumberSetting("Timer", 1, 5, 1, 0.1);
    private final NumberSetting vanillaSpeed = new NumberSetting("Speed", 1, 10, 1, 0.1);

    private final TimerUtil timerUtil = new TimerUtil();
    private final float r = ThreadLocalRandom.current().nextFloat();
    private double speed, lastDist;
    private float speedChangingDirection;
    private int stage;
    private boolean strafe, wasOnGround;
    private final boolean setTimer = true;
    private double moveSpeed;
    private int inAirTicks;
    //Watchdog Lowhop
    private int fallTicks;
    private double lowHopTicks = 0;
    private boolean canLowHop = false;
    private boolean lagged = false;
    private boolean floated = false;

    private boolean doextra = false;
    private boolean speedJump = false;
    private int airTicks = 0;
    private boolean doSlowHop = false;
    private float yaw = 0;
    public Speed() {
        super("module.movement.Speed", Category.MOVEMENT, "Makes you go faster");
        watchdogMode.addParent(mode, modeSetting -> modeSetting.is("Watchdog"));
        verusMode.addParent(mode, modeSetting -> modeSetting.is("Verus"));
        viperMode.addParent(mode, modeSetting -> modeSetting.is("Viper"));
        groundSpeed.addParent(watchdogMode, modeSetting -> modeSetting.is("Ground") && mode.is("Watchdog"));
        vanillaSpeed.addParent(mode, modeSetting -> modeSetting.is("Vanilla") || modeSetting.is("BHop"));
        this.addSettings(mode, vanillaSpeed, watchdogMode, verusMode, viperMode, autoDisable, groundSpeed, timer);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent e) {
        this.setSuffix(mode.get());
        if (setTimer) {
            mc.timer.timerSpeed = timer.get().floatValue();
        }

        double distX = e.getX() - mc.thePlayer.prevPosX, distZ = e.getZ() - mc.thePlayer.prevPosZ;
        lastDist = Math.hypot(distX, distZ);

        switch (mode.get()) {
            case "Watchdog":
                switch (watchdogMode.get()) {
                    case "Hop":
                    case "Dev":
                        if (e.isPre()) {
                            if (MovementUtils.isMoving() && mc.thePlayer.fallDistance < 1) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                }
                            }
                        }
                        break;
                    case "Low Hop":
                        PotionEffect speedEffect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
                        double simpleY = floor((mc.thePlayer.getPosY() % 1) * 10000 + 0.5);
                        double hypotenuse = Math.sqrt(mc.thePlayer.getMotionX() * mc.thePlayer.getMotionX() + mc.thePlayer.getMotionZ() * mc.thePlayer.getMotionZ());
                        if (mc.thePlayer != null && mc.theWorld != null && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava() && !mc.thePlayer.isSpectator() && !e.isPre()) {
                            yaw = mc.thePlayer.rotationYaw;
                            //if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).get() || KillAura.target!=null){
                            //    yaw = RotationComponent.rotations.getY();
                            //}
                            if (mc.thePlayer.onGround) {
                                doSlowHop = false;
                                if (airTicks > 0) lowHopTicks = airTicks;
                                airTicks = 0;
                            } else {
                                airTicks++;
                            }
                            if (mc.thePlayer.onGround) {
                                boolean allowLow = Math.abs(mc.thePlayer.getPosY() - Math.round(mc.thePlayer.getPosY())) <= 0.03;

                                canLowHop = false;
                                if (MovementUtils.isMoving()) {
                                    if (!canLowHop) {
                                        lagged = false;
                                    }

                                    speedJump = true;
                                    mc.thePlayer.setSprinting(true);
                                    mc.thePlayer.jump();

                                    TargetStrafe.strafe(MovementUtils.getSpeed(), yaw);
                                    speedJump = false;

                                    if (floated) floated = false;
                                    canLowHop = !lagged && lowHopTicks >= 3 && allowLow /*&& !Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).get()*/;
                                }
                            }
                            if (doSlowHop){
                                System.out.println(airTicks + " " + simpleY);
                                if (simpleY == 2492) {
                                    mc.thePlayer.addVelocity(0, -0.18, 0);
                                }
                                return;
                            }
                            if (canLowHop) {
                                //System.out.println(simpleY);
                                if (simpleY == 4200) {
                                    mc.thePlayer.setVelocity(mc.thePlayer.getMotionX(), 0.39, mc.thePlayer.getMotionZ());
                                } else if (simpleY == 1138) {
                                    mc.thePlayer.addVelocity(0, -0.1309, 0);
                                } else if (simpleY == 2022) {
                                    mc.thePlayer.addVelocity(0, -0.2, 0);
                                }
                                if (simpleY == 7442)
                                    if (MovementUtils.getDistanceToGround() <= 0.75) {
                                        doextra = true;
                                        mc.thePlayer.addVelocity(0, +0.075, 0);
                                        if (hypotenuse < MovementUtils.getAllowHorizontalDistance() && mc.thePlayer.getMotionX() == 0 && mc.thePlayer.getMotionZ() == 0) {
                                            if (speedEffect != null) {
                                                TargetStrafe.strafe(MovementUtils.getAllowHorizontalDistance() - 0.01);
                                            } else {
                                                TargetStrafe.strafe(MovementUtils.getAllowHorizontalDistance() - 0.05);
                                            }
                                        }
                                    }
                                if (doextra) {
                                    if (airTicks == 7) {
                                        if (MovementUtils.getDistanceToGround() <= 0.75) {
                                            TargetStrafe.strafe(getPlayerSpeed());
                                        }
                                    }
                                    if (airTicks == 8) {
                                        if (MovementUtils.getDistanceToGround() <= 0.75) {
                                            TargetStrafe.strafe(MovementUtils.getAllowHorizontalDistance());
                                        }
                                    }
                                } else {
                                    if (airTicks == 2) {
                                        EntityPlayer player = mc.thePlayer;
                                        if (player != null) {
                                            player.motionX=(player.motionX * 1 + player.motionX * 2)/3;
                                            player.motionZ=(player.motionZ * 1 + player.motionZ * 2)/3;
                                        }
                                    }

                                    if (airTicks == 9 && (MovementUtils.getDistanceToGround() <= 0.80)) {

                                        EntityPlayer player = mc.thePlayer;
                                        if (player != null) {
                                            player.motionY=player.motionY +0.075;
                                        }
                                        TargetStrafe.strafe(getPlayerSpeed());
                                    }

                                    if (airTicks == 10  && (MovementUtils.getDistanceToGround() <= 0.80)) {

                                        double hypotenuse2 = Math.hypot(mc.thePlayer.getMotionX(), mc.thePlayer.getMotionZ());
                                        if (hypotenuse2 < MovementUtils.getAllowHorizontalDistance() || mc.thePlayer.getMotionX() == 0 || mc.thePlayer.getMotionZ() == 0) {
                                            TargetStrafe.strafe(MovementUtils.getAllowHorizontalDistance());
                                        }
                                        TargetStrafe.strafe(getPlayerSpeed());
                                    }

                                    if (getBlockRelativeToPlayer(0, mc.thePlayer.getMotionY(), 0).equals(Blocks.air)) {
                                        if (airTicks == 11) {
                                            TargetStrafe.strafe(MovementUtils.speedValue(0.31, 0.375, 0.45));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }
                break;
            case "Heatseeker":
                if (e.isPre()) {
                    if (mc.thePlayer.onGround) {
                        if (timerUtil.hasTimeElapsed(300, true)) {
                            strafe = !strafe;
                        }
                        if (strafe) {
                            MovementUtils.setSpeed(1.5);
                        }
                    }
                }
                break;
            case "Mineland":
                if (e.isPre()) {
                    stage++;
                    if (stage == 1)
                        mc.thePlayer.motionY = 0.2;

                    if (mc.thePlayer.onGround && stage > 1)
                        MovementUtils.setSpeed(0.5);

                    if (stage % 14 == 0)
                        stage = 0;
                }
                break;
            case "Vulcan":
                if (e.isPre()) {
                    if (mc.thePlayer.onGround) {
                        if (MovementUtils.isMoving()) {
                            mc.thePlayer.jump();
                            MovementUtils.setSpeed(MovementUtils.getBaseMoveSpeed() * 1.6);
                            inAirTicks = 0;
                        }
                    } else {
                        inAirTicks++;
                        if (inAirTicks == 1)
                            MovementUtils.setSpeed(MovementUtils.getBaseMoveSpeed() * 1.16);
                    }
                }
                break;
            case "Zonecraft":
                if (e.isPre()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                        MovementUtils.setSpeed(MovementUtils.getBaseMoveSpeed() * 1.8);
                        stage = 0;
                    } else {
                        if (stage == 0 && !mc.thePlayer.isCollidedHorizontally)
                            mc.thePlayer.motionY = -0.4;
                        stage++;
                    }
                }
                break;
            case "Matrix":
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround && mc.thePlayer.motionY < 0.003) {
                        mc.thePlayer.jump();
                        mc.timer.timerSpeed = 1.0f;
                    }
                    if (mc.thePlayer.motionY > 0.003) {
                        mc.thePlayer.motionX *= speed;
                        mc.thePlayer.motionZ *= speed;
                        mc.timer.timerSpeed = 1.05f;
                    }
                    speed = 1.0012f;
                }
                break;
            case "HurtTime":
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.hurtTime <= 0) {
                        mc.thePlayer.motionX *= 1.001f;
                        mc.thePlayer.motionZ *= 1.001f;
                    } else {
                        mc.thePlayer.motionX *= 1.0294f;
                        mc.thePlayer.motionZ *= 1.0294f;
                    }
                    if (mc.thePlayer.onGround && mc.thePlayer.motionY < 0.003) {
                        mc.thePlayer.jump();
                    }
                }
                break;
            case "Vanilla":
                if (MovementUtils.isMoving()) {
                    MovementUtils.setSpeed(vanillaSpeed.get() / 4);
                }
                break;
            case "BHop":
                if (MovementUtils.isMoving()) {
                    MovementUtils.setSpeed(vanillaSpeed.get() / 4);
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
                break;
            case "Verus":
                switch (verusMode.get()) {
                    case "Low":
                        if (e.isPre()) {
                            if (MovementUtils.isMoving()) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                    wasOnGround = true;
                                } else if (wasOnGround) {
                                    if (!mc.thePlayer.isCollidedHorizontally) {
                                        mc.thePlayer.motionY = -0.0784000015258789;
                                    }
                                    wasOnGround = false;
                                }
                                MovementUtils.setSpeed(0.33);
                            } else {
                                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                            }
                        }
                        break;
                    case "Normal":
                        if (e.isPre()) {
                            if (MovementUtils.isMoving()) {
                                if (mc.thePlayer.onGround) {
                                    mc.thePlayer.jump();
                                    MovementUtils.setSpeed(0.48);
                                } else {
                                    MovementUtils.setSpeed(MovementUtils.getSpeed());
                                }
                            } else {
                                MovementUtils.setSpeed(0);
                            }
                        }
                        break;
                }
                break;
            case "Viper":
                switch (viperMode.get()) {
                    case "High":
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.motionY = 0.7;
                        }
                        break;
                    case "Normal":
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.motionY = 0.42;
                        }
                        break;
                }
                MovementUtils.setSpeed(MovementUtils.getBaseMoveSpeed() * 1.2);
                break;
            case "Strafe":
                if (e.isPre() && MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    } else {
                        MovementUtils.setSpeed(MovementUtils.getSpeed());
                    }
                }
                break;
        }

    }

    @EventTarget
    public void onMoveEvent(MoveEvent e) {
        if (mode.is("Watchdog")) {
            switch (watchdogMode.get()) {
                case "Ground":
                    strafe = !strafe;
                    if (mc.thePlayer.onGround && MovementUtils.isMoving() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + e.getX(), mc.thePlayer.posY, mc.thePlayer.posZ + e.getZ())).getBlock() == Blocks.air && !mc.thePlayer.isCollidedHorizontally && !Step.isStepping) {
                        if (strafe || groundSpeed.get() >= 1.6)
                            PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + e.getX(), mc.thePlayer.posY, mc.thePlayer.posZ + e.getZ(), true));
                        e.setSpeed(MovementUtils.getBaseMoveSpeed() * groundSpeed.get());
                        break;
                    }
                    break;
            }
        }
        TargetStrafe.strafe(e);
    }

    @EventTarget
    public void onPlayerMoveUpdateEvent(PlayerMoveUpdateEvent e) {
        if (mode.is("Watchdog") && (watchdogMode.is("Hop") || watchdogMode.is("Dev")) && mc.thePlayer.fallDistance < 1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            if (MovementUtils.isMoving()) {
                switch (watchdogMode.get()) {
                    case "Hop":
                        if (mc.thePlayer.onGround)
                            speed = 1.5f;
                        speed -= 0.025;
                        e.applyMotion(MovementUtils.getBaseMoveSpeed() * speed, 0.55f);
                        break;
                    case "Dev":
                        if (mc.thePlayer.onGround) {
                            moveSpeed = MovementUtils.getBaseMoveSpeed() * 2.1475 * 0.76;
                            wasOnGround = true;
                        } else if (wasOnGround) {
                            moveSpeed = lastDist - 0.81999 * (lastDist - MovementUtils.getBaseMoveSpeed());
                            moveSpeed *= 1 / 0.91;
                            wasOnGround = false;
                        } else {
                            moveSpeed -= TargetStrafe.canStrafe() ? lastDist / 100.0 : lastDist / 150.0;
                        }
                        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
                            speed = MovementUtils.getBaseMoveSpeed() * 0.25;
                        } else {
                            speed = Math.max(moveSpeed, MovementUtils.getBaseMoveSpeed());
                        }
                        e.applyMotion(speed, 0.6f);
                        break;
                }
            } else {
                e.applyMotion(0, 0);
            }
        }
    }

    @EventTarget
    public void onPacketEvent(PacketEvent e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook && autoDisable.get()) {
            NotificationManager.post(NotificationType.WARNING, "Flag Detector",
                    "Speed disabled due to " +
                            (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5
                                    ? "world change"
                                    : "lagback"), 1.5F);
            this.toggleSilent();
        }
    }

    public boolean shouldPreventJumping() {
        return Client.INSTANCE.isEnabled(Speed.class) && MovementUtils.isMoving() && !(mode.is("Watchdog") && watchdogMode.is("Ground"));
    }

    @Override
    public void onEnable() {
        speed = 1.5f;
        timerUtil.reset();
        if (mc.thePlayer != null) {
            wasOnGround = mc.thePlayer.onGround;
        }
        inAirTicks = 0;
        moveSpeed = 0;
        stage = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
        super.onDisable();
    }

    public double getPlayerSpeed() {
        return Math.hypot(mc.thePlayer.getMotionX(), mc.thePlayer.getMotionZ());
    }
    public static IBlockState getBlockRelativeToPlayer(double x, double y, double z) {
        if (mc.thePlayer == null || mc.thePlayer == null) {
            return null;
        }

        BlockPos playerPos = mc.thePlayer.getPosition();
        BlockPos targetPos = playerPos.add(x, y, z);

        return mc.theWorld.getBlockState(targetPos);
    }
}
