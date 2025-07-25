package net.minecraft.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLAllocation
{

    /**
     * Generates the specified number of display lists and returns the first index.
     */
    public static synchronized int generateDisplayLists(int range)
    {
        int i = GL11.glGenLists(range);

        if (i == 0)
        {
            int j = GL11.glGetError();
            String s = "No error code reported";

            if (j != 0)
            {
                s = GLU.gluErrorString(j);
            }

            throw new IllegalStateException("glGenLists returned an ID of 0 for a count of " + range + ", GL error (" + j + "): " + s);
        }
        else
        {
            return i;
        }
    }

    public static synchronized void deleteDisplayLists(int list, int range)
    {
        GL11.glDeleteLists(list, range);
    }

    public static synchronized void deleteDisplayLists(int list)
    {
        GL11.glDeleteLists(list, 1);
    }

    /**
     * Creates and returns a direct byte buffer with the specified capacity. Applies native ordering to speed up access.
     */
    public static synchronized ByteBuffer createDirectByteBuffer(int capacity)
    {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    /**
     * Creates and returns a direct int buffer with the specified capacity. Applies native ordering to speed up access.
     */
    public static IntBuffer createDirectIntBuffer(int capacity)
    {
        return createDirectByteBuffer(capacity << 2).asIntBuffer();
    }

    /**
     * Creates and returns a direct float buffer with the specified capacity. Applies native ordering to speed up
     * access.
     */
    public static FloatBuffer createDirectFloatBuffer(int capacity)
    {
        return createDirectByteBuffer(capacity << 2).asFloatBuffer();
    }
}
