package dev.tenacity.utils.animations;

import lombok.Setter;
import net.minecraft.client.Minecraft;

public class AnimationUtil {
    @Setter
    private static long delta;

    public static float animation(float animation, float target, float speedTarget) {
        float dif = (target - animation) / Math.max((float) Minecraft.getDebugFPS(), 5) * 15;

        if (dif > 0) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }

    public static float animateIDK(double target, double current, double speed) {
        boolean larger = (target > current);
        if (speed < 0.0F) speed = 0.0F;
        else if (speed > 1.0F) speed = 1.0F;
        double dif = Math.abs(current - target);
        double factor = dif * speed;
//        if (factor < 0.1f) factor = 0.1F;
        if (larger) current += factor;
        else current -= factor;
        return (float) current;
    }

    public static double animation(double animation, double target, double speedTarget) {
        double dif = (target - animation) / Math.max(Minecraft.getDebugFPS(), 5) * speedTarget;
        if (dif > 0.0D) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0.0D) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }

    public static float moveUDSmooth(final float current, final float end) {
        return moveUD(current, end);
    }

    public static float lstransition(float now, float desired, double speed) {
        double dif = Math.abs(desired - now);
        float a = (float) Math.abs((double) (desired - (desired - Math.abs(desired - now))) / (100.0 - speed * 10.0));
        float x = now;
        if (dif != 0.0 && dif < (double) a) {
            a = (float) dif;
        }

        if (dif > 0.0) {
            if (now < desired) {
                x = (float) (now + a * delta);
            } else if (now > desired) {
                x = (float) (now - a * delta);
            }
        } else {
            x = desired;
        }

        if ((double) Math.abs(desired - x) < 0.05 && x != desired) {
            x = desired;
        }

        return x;
    }


    public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 0) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        return current + movement;
    }

    public static float smoothAnimation(float ani, float finalState, float speed, float scale) {
        return getAnimationStateFlux(ani, finalState, Math.max(10, (Math.abs(ani - finalState)) * speed) * scale);
    }

    public static float smoothAnimation(float ani, float finalState, float speed) {
        return getAnimationStateFlux(ani, finalState, Math.max(10, (Math.abs(ani - finalState)) * speed) * 1);
    }

    public static float smoothAnimation(float ani, float finalState) {
        return getAnimationStateFlux(ani, finalState, Math.max(10f, (Math.abs(ani - finalState)) * 50f) * 0.3f);
    }

    public static float moveUD(float current, final float end) {
        return lstransition(current, end, 2f);
    }

    public static double getAnimationStateEasing(double animation, double finalState, double speed) {
        if (animation == finalState) return finalState;

        final double add = delta * Math.max(Math.abs(finalState - animation) * speed, 0.01);
        animation = animation < finalState ? (Math.min(animation + add, finalState)) : (Math.max(animation - add, finalState));
        return animation;
    }


    public static double deltaTime() {
        return Minecraft.getDebugFPS() > 0 ? 1.0 / Minecraft.getDebugFPS() : 1.0;
    }

    public static float getAnimationStateFlux(float animation, final float finalState, final float speed) {
        final float add = (float) (delta * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }

    public static float calculateCompensation(final float target, float current, long delta, final int speed) {
        final float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        double v = (speed * delta / 16L < 0.25) ? 0.5 : (speed * delta / 16L);
        if (diff > speed) {
            current -= (float) v;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            current += (float) v;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }


    public static float getAnimationState(float animation, final float finalState, final float speed) {
        final float add = (float) (0.01 * speed);
        animation = animation < finalState ? (Math.min(animation + add, finalState)) : (Math.max(animation - add, finalState));
        return animation;
    }

    public static float animate(final float target, float current, float speed) {
        final boolean larger = (target > current);
        if (speed < 0f) {
            speed = 0f;
        } else if (speed > 1f) {
            speed = 1f;
        }
        if (target == current) {
            return target;
        }
        final float dif = Math.max(target, current) - Math.min(target, current);
        float factor = Math.max(dif * speed, 1.0f);
        if (factor < 0.1f) {
            factor = 0.1f;
        }
        if (larger) {
            if (current + factor > target) {
                current = target;
            } else {
                current += factor;
            }
        } else if (current - factor < target) {
            current = target;
        } else {
            current -= factor;
        }
        return current;
    }


    public static float clamp(final float number, final float min, final float max) {
        return number < min ? min : Math.min(number, max);
    }

    public static float animateSmooth(float current, final float target, float speed) {
        if (current == target) {
            return current;
        }
        final boolean larger = target > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }
        final double dif = Math.max(target, (double) current) - Math.min(target, (double) current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        if (larger) {
            current += (float) factor;
            if (current >= target) {
                current = target;
            }
        } else if (target < current) {
            current -= (float) factor;
            if (current <= target) {
                current = target;
            }
        }
        return current;
    }

    /**
     * @param current The raw number
     * @param target The target number
     * @param speed The animation speed (The speed increases as this value increases)
     * @return The animated number
     */
    public static float smooth(float current, float target, float speed) {
        long deltaTime = delta;

        speed = Math.abs(target - current) * speed;

        if (deltaTime < 1L) {
            deltaTime = 1L;
        }

        final float difference = current - target;
        final float smoothing = Math.max(speed * (deltaTime / 16F), .15F);

        if (difference > speed) {
            current = Math.max(current - smoothing, target);
        } else if (difference < -speed) {
            current = Math.min(current + smoothing, target);
        } else {
            current = target;
        }

        return current;
    }

    public static double smooth(double current, double target, double speed) {
        long deltaTime = delta;

        speed = Math.abs(target - current) * speed;

        if (deltaTime < 1L) {
            deltaTime = 1L;
        }

        final double difference = current - target;
        final double smoothing = Math.max(speed * (deltaTime / 16F), .15F);

        if (difference > speed) {
            current = Math.max(current - smoothing, target);
        } else if (difference < -speed) {
            current = Math.min(current + smoothing, target);
        } else {
            current = target;
        }

        return current;
    }
}
