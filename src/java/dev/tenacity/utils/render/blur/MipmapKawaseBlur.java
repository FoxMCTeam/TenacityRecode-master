package dev.tenacity.utils.render.blur;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.render.GLUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.render.ShaderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

public class MipmapKawaseBlur implements Utils {

    private static final List<Framebuffer> framebufferList = new ArrayList<>();
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private static int currentIterations;
    private static int mipmapLevel = 0;

    public static void setMipmapLevel(int level) {
        mipmapLevel = Math.max(0, Math.min(level, 4));
    }

    public static void setupUniforms(float offset) {
        kawaseDown.setUniformf("offset", offset, offset);
        kawaseUp.setUniformf("offset", offset, offset);
    }

    private static void initFramebuffers(int iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        int baseWidth = mc.displayWidth >> mipmapLevel;  baseWidth = Math.max(baseWidth, 16);
        int baseHeight = mc.displayHeight >> mipmapLevel;  baseHeight = Math.max(baseHeight, 16);

        framebufferList.add(framebuffer = new Framebuffer(baseWidth, baseHeight, false));

        for (int i = 1; i <= iterations; i++) {
            int width = baseWidth >> i;
            int height = baseHeight >> i;

            width = Math.max(width, 2);
            height = Math.max(height, 2);

            Framebuffer currentBuffer = new Framebuffer(width, height, false);
            currentBuffer.setFramebufferFilter(GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }

    public static void renderBlur(int stencilFrameBufferTexture, int iterations, int offset) {
        int baseWidth = mc.displayWidth >> mipmapLevel;
        int baseHeight = mc.displayHeight >> mipmapLevel;

        if (currentIterations != iterations ||
                framebuffer.framebufferWidth != baseWidth ||
                framebuffer.framebufferHeight != baseHeight) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, kawaseDown, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);

        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);

        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        RenderUtil.bindTexture(stencilFrameBufferTexture);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebufferList.get(1).framebufferTexture);

        ShaderUtil.drawQuads();
        kawaseUp.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        RenderUtil.bindTexture(lastBuffer.framebufferTexture);
        RenderUtil.setAlphaLimit(0);
        GLUtil.startBlend();

        ShaderUtil.drawQuads();
        GlStateManager.bindTexture(0);
        GLUtil.endBlend();
    }

    private static void renderFBO(Framebuffer outputFBO, int inputTexture, ShaderUtil shader, float offset) {
        outputFBO.framebufferClear();
        outputFBO.bindFramebuffer(false);

        shader.init();
        RenderUtil.bindTexture(inputTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / outputFBO.framebufferWidth, 1.0f / outputFBO.framebufferHeight);
        shader.setUniformf("iResolution", outputFBO.framebufferWidth, outputFBO.framebufferHeight);

        ShaderUtil.drawQuads();
        shader.unload();
    }
}