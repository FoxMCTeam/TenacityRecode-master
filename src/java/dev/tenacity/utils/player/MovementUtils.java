package dev.tenacity.utils.player;

import dev.tenacity.event.impl.player.MoveEvent;
import dev.tenacity.event.impl.player.PlayerMoveUpdateEvent;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjglx.util.vector.Vector2f;

public class MovementUtils implements Utils {
    public static final double WALK_SPEED = 0.221;
    public static final double BUNNY_SLOPE = 0.66;
    public static final double MOD_SPRINTING = 1.3;
    public static final double MOD_SNEAK = 0.3;
    public static final double MOD_ICE = 2.5;
    public static final double MOD_WEB = 0.105 / WALK_SPEED;
    public static final double JUMP_HEIGHT = 0.42;
    public static final double BUNNY_FRICTION = 159.9;
    public static final double Y_ON_GROUND_MIN = 0.00001;
    public static final double Y_ON_GROUND_MAX = 0.0626;

    public static final double AIR_FRICTION = 0.9800000190734863;
    public static final double WATER_FRICTION = 0.800000011920929;
    public static final double LAVA_FRICTION = 0.5;
    public static final double MOD_SWIM = 0.115f / WALK_SPEED;
    public static final double[] MOD_DEPTH_STRIDER = new double[] {
            1.0,
            0.1645f / MOD_SWIM / WALK_SPEED,
            0.1995f / MOD_SWIM / WALK_SPEED,
            1.0f / MOD_SWIM
    };
    public static double speedValue(double v0, double v1, double v2) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            PotionEffect speedEffect = player.getActivePotionEffect(Potion.moveSpeed); // Speed potion ID is 1
            if (speedEffect != null) {
                int amplifier = speedEffect.getAmplifier();
                if (amplifier == 0) {
                    return v1;
                } else if (amplifier >= 1) {
                    return v2;
                }
            }
        }
        return v0;
    }
    public static double getDistanceToGround() {

        if (mc.thePlayer == null) {
            return -1;
        }


        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY;
        double playerZ = mc.thePlayer.posZ;


        if (mc.thePlayer.isOnGround()) {
            return 0;
        }


        for (int y = (int) Math.floor(playerY); y >= 0; y--) {
            BlockPos blockPos = new BlockPos(playerX, y, playerZ);
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);


            if (!(blockState.getBlock().equals(Blocks.air))) {
                return playerY - (y + 1);
            }
        }


        return playerY;
    }

    public static float getMotionDirection() {
        float motionDir = (float) Math.toDegrees(Math.atan2(-mc.thePlayer.motionX, mc.thePlayer.motionZ));
        return motionDir < 0 ? motionDir + 360 : motionDir;
    }

    public static double getAllowHorizontalDistance() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null) {
            return 0;
        }

        double horizontalDistance;
        boolean useBaseModifiers = false;

        if (player.isInWater()) {
            horizontalDistance = MOD_WEB * WALK_SPEED;
        } else if (player.isInWater() || player.isInLava()) {
            horizontalDistance = MOD_SWIM * WALK_SPEED;
        } else if (player.isSneaking()) {
            horizontalDistance = MOD_SNEAK * WALK_SPEED;
        } else {
            horizontalDistance = WALK_SPEED;
            useBaseModifiers = true;
        }

        if (useBaseModifiers) {
            // Apply sprinting modifier if moving and not sneaking
            if (!player.isSneaking() && (player.moveForward != 0 || player.moveStrafing != 0)) {
                horizontalDistance *= MOD_SPRINTING;
            }

            // Apply speed potion effect
            PotionEffect speedEffect = player.getActivePotionEffect(Potion.moveSpeed);
            if (speedEffect != null && speedEffect.getDuration() > 0) {
                horizontalDistance *= (1 + (speedEffect.getAmplifier() + 1) * 0.2);
            }

            // Apply slowness effect (overrides other modifiers)
            PotionEffect slownessEffect = player.getActivePotionEffect(Potion.moveSlowdown);
            if (slownessEffect != null) {
                horizontalDistance = 0.29 * WALK_SPEED; // Slowness reduces speed to about 29% of walking speed
            }
        }

        return horizontalDistance;
    }

    public static boolean isMoving() {
        if (mc.thePlayer == null) {
            return false;
        }
        return (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public static float getMoveYaw(float yaw) {
        Vector2f from = new Vector2f((float) mc.thePlayer.lastTickPosX, (float) mc.thePlayer.lastTickPosZ),
                to = new Vector2f((float) mc.thePlayer.posX, (float) mc.thePlayer.posZ),
                diff = new Vector2f(to.x - from.x, to.y - from.y);

        double x = diff.x, z = diff.y;
        if (x != 0 && z != 0) {
            yaw = (float) Math.toDegrees((Math.atan2(-x, z) + MathHelper.PI2) % MathHelper.PI2);
        }
        return yaw;
    }

    public static Block getBlockAt(double x, double y, double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public static void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }

    public static void setSpeedHypixel(PlayerMoveUpdateEvent event, float moveSpeed, float strafeMotion) {
        float remainder = 1F - strafeMotion;
        if (mc.thePlayer.onGround) {
            setSpeed(moveSpeed);
        } else {
            mc.thePlayer.motionX *= strafeMotion;
            mc.thePlayer.motionZ *= strafeMotion;
            event.setFriction(moveSpeed * remainder);
        }
    }

    public static void setSpeed(MoveEvent moveEvent, double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));
        moveEvent.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
        moveEvent.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);
    }

    public static void setSpeed(MoveEvent moveEvent, double moveSpeed) {
        setSpeed(moveEvent, moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = mc.thePlayer.capabilities.getWalkSpeed() * 2.873;
        if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
            baseSpeed /= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() + 1);
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static void sendFlyingCapabilities(final boolean isFlying, final boolean allowFlying) {
        final PlayerCapabilities playerCapabilities = new PlayerCapabilities();
        playerCapabilities.isFlying = isFlying;
        playerCapabilities.allowFlying = allowFlying;
        PacketUtils.sendPacketNoEvent(new C13PacketPlayerAbilities(playerCapabilities));
    }

    public static double getBaseMoveSpeed2() {
        double baseSpeed = mc.thePlayer.capabilities.getWalkSpeed() * (mc.thePlayer.isSprinting() ? 2.873 : 2.215);
        if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
            baseSpeed /= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() + 1);
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static double getBaseMoveSpeedStupid() {
        double sped = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            sped *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return sped;
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, -height, 0)).isEmpty();
    }

    public static float getSpeed() {
        if (mc.thePlayer == null || mc.theWorld == null) return 0;
        return (float) Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public static float getMaxFallDist() {
        return mc.thePlayer.getMaxFallHeight() + (mc.thePlayer.isPotionActive(Potion.jump) ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 0);
    }

}
