/**
 * @author Administrator$
 * @date 2024/11/15$
 * @version 1.0
 */


package dev.tenacity.utils.objects;

import lombok.Getter;
import lombok.Setter;
import org.lwjglx.input.Mouse;

@Getter
@Setter
public class ScrollUtils {
    public static float yOffset;
    public static float speed = 120.0F;

    public static void run() {

        Mouse.addWheelEvent(MathUtils.scrollSpeed(yOffset, speed));

    }
}
