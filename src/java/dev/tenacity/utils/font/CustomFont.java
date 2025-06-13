package dev.tenacity.utils.font;

import dev.tenacity.Client;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.utils.render.GradientUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.tuples.mutable.MutablePair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author d3Ck
 * @since 06/08/2025
 */
public class CustomFont implements AbstractFontRenderer {
    private static final int[] colorCode = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            final int base = (i >> 3 & 0x1) * 85;
            int r = (i >> 2 & 0x1) * 170 + base;
            int g = (i >> 1 & 0x1) * 170 + base;
            int b = (i & 0x1) * 170 + base;
            if (i == 6) {
                r += 85;
            }
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            CustomFont.colorCode[i] = ((r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
        }
    }

    private final List<String> lines = new ArrayList<>();
    private final byte[][] charWidth;
    private final int[] textures;
    private final Font font;
    @Setter
    @Getter
    private CustomFont boldFont;
    @Setter
    @Getter
    private CustomFont thinFont;
    private float size;
    private int fontWidth;
    private int fontHeight;
    private int textureWidth;
    private int textureHeight;

    public CustomFont(final Font font) {
        this.charWidth = new byte[65536 / 256][]; // 支持 0-65535
        this.textures = new int[65536 / 256];
        FontRenderContext context = new FontRenderContext(new AffineTransform(), true, true);
        this.size = 0.0f;
        this.fontWidth = 0;
        this.fontHeight = 0;
        this.textureWidth = 0;
        this.textureHeight = 0;
        this.font = font;
        this.size = font.getSize2D();
        Arrays.fill(this.textures, -1);
        final Rectangle2D maxBounds = font.getMaxCharBounds(context);
        this.fontWidth = (int) Math.ceil(maxBounds.getWidth());
        this.fontHeight = (int) Math.ceil(maxBounds.getHeight());
        this.textureWidth = this.resizeToOpenGLSupportResolution(this.fontWidth * 16);
        this.textureHeight = this.resizeToOpenGLSupportResolution(this.fontHeight * 16);
    }

    private static ByteBuffer imageToBuffer(final BufferedImage img) {
        final int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        final ByteBuffer buf = ByteBuffer.allocateDirect(4 * arr.length);
        for (final int i : arr) {
            buf.putInt(i << 8 | (i >> 24 & 0xFF));
        }
        buf.flip();
        return buf;
    }

    public void drawSmoothStringWithShadow(String text, double x2, float y2, int color) {
        this.drawString(text, (float) (x2 + 0.5f), y2 + 0.5f, color, true);

        this.drawString(text, (float) x2, y2, color, false);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        this.drawStringWithShadow(text, x - (this.getStringWidth(text) / 2), y, color);
    }

    public void drawSmoothString(String text, double x2, float y2, int color) {
        this.drawString(text, (float) x2, y2, color, false);
    }

    public List<String> getWrappedLines(String text, float x, float width, float heightIncrement) {
        wrapTextToLines(text, x, width);
        return lines;
    }

    private void wrapTextToNewLine(String text) {
        lines.clear();
        lines.addAll(Arrays.asList(text.trim().split("\n")));
    }

    public MutablePair<Float, Float> drawNewLineText(String text, float x, float y, int color, float heightIncrement) {
        wrapTextToNewLine(text);

        String longest = "";
        float newY = y;
        for (String s : lines) {
            if (getStringWidth(s) > getStringWidth(longest)) {
                longest = s;
            }
            RenderUtil.resetColor();
            drawString(s, x, newY, color);
            newY += getHeight() + heightIncrement;
        }

        return MutablePair.of(getStringWidth(longest), newY - y);
    }

    private void wrapTextToLines(String text, float x, float width) {
        lines.clear();
        String[] words = text.trim().split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            float totalWidth = getStringWidth(line + " " + word);

            if (x + totalWidth >= x + width) {
                lines.add(line.toString());
                line = new StringBuilder(word).append(" ");
                continue;
            }

            line.append(word).append(" ");
        }
        lines.add(line.toString());
    }

    public float drawWrappedText(String text, float x, float y, int color, float width, float heightIncrement) {
        wrapTextToLines(text, x, width);

        float newY = y;
        for (String s : lines) {
            RenderUtil.resetColor();
            drawString(s, x, newY, color);
            newY += getHeight() + heightIncrement;
        }
        return newY - y;
    }

    @Override
    public final int drawCenteredString(final String text, final float x, final float y, final int color) {
        this.drawString(text, x - this.getStringWidth(text) / 2, y, color);
        return (int) getStringWidth(text) + 1;
    }

    @Override
    public void drawCenteredString(String name, float x, float y, Color color) {
        drawCenteredString(name, x, y, color.getRGB());
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(final String p_trimStringToWidth_1_, final int p_trimStringToWidth_2_, final boolean p_trimStringToWidth_3_) {
        final StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        final int j = p_trimStringToWidth_3_ ? (p_trimStringToWidth_1_.length() - 1) : 0;
        final int k = p_trimStringToWidth_3_ ? -1 : 1;
        boolean flag = false;
        boolean flag2 = false;
        for (int l = j; l >= 0 && l < p_trimStringToWidth_1_.length() && i < p_trimStringToWidth_2_; l += k) {
            final char c0 = p_trimStringToWidth_1_.charAt(l);
            final int i2 = (int) this.getStringWidth(String.valueOf(c0));
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag2 = false;
                    }
                } else {
                    flag2 = true;
                }
            } else if (i2 < 0) {
                flag = true;
            } else {
                i += i2;
                if (flag2) {
                    ++i;
                }
            }
            if (i > p_trimStringToWidth_2_) {
                break;
            }
            if (p_trimStringToWidth_3_) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }

    @Override
    public int getHeight() {
        return (this.fontHeight - 8) / 2;
    }

    protected final int drawChar(final char chr, final float x, final float y) {
        final int region = chr >> 8;
        int id = chr & 0xFF;
        final int xTexCoord = (id & 0xF) * this.fontWidth;
        final int yTexCoord = (id >> 4) * (this.fontHeight);
        final int width = this.getOrGenerateCharWidthMap(region)[id];
        GlStateManager.bindTexture(this.getOrGenerateCharTexture(region));
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glBegin(7);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord, this.textureWidth), this.wrapTextureCoord(yTexCoord, this.textureHeight));
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord, this.textureWidth), this.wrapTextureCoord(yTexCoord + this.fontHeight, this.textureHeight));
        GL11.glVertex2f(x, y + this.fontHeight);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord + width, this.textureWidth), this.wrapTextureCoord(yTexCoord + this.fontHeight, this.textureHeight));
        GL11.glVertex2f(x + width, y + this.fontHeight);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord + width, this.textureWidth), this.wrapTextureCoord(yTexCoord, this.textureHeight));
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        return width;
    }

    @Override
    public int drawString(final String str, final float x, final float y, final int color) {
        return this.drawString(str, x, y, color, false);
    }


    public final int drawString(String str, float x, float y, int color, final boolean darken) {
        if (str == null || str.isEmpty()) {
            return 0;
        }

        str = str.replace("▬", "="); // Character replacement (compatibility handling)
        y -= 2.0f;
        x *= 2.0f;
        y *= 2.0f;

        int offset = 0;

        if (darken) {
            color = ((color & 0xFCFCFC) >> 2) | (color & 0xFF000000); // Darken color
        }
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        int currentColor = color;
        RenderUtil.resetColor();
        RenderUtil.color(currentColor);
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char chr = chars[i];
            if (chr == '§' && i + 1 < chars.length) {
                char codeChar = Character.toLowerCase(chars[i + 1]);
                int codeIndex = "0123456789abcdefklmnor".indexOf(codeChar);
                if (codeIndex >= 0) {
                    if (codeIndex < 16) {
                        int newColor = colorCode[codeIndex];
                        float alpha = (currentColor >> 24 & 0xFF) / 255.0f;
                        if (alpha == 0.0f) alpha = 1.0f;
                        currentColor = (newColor & 0x00FFFFFF) | ((int)(alpha * 255) << 24);
                        RenderUtil.resetColor();
                        RenderUtil.color(currentColor);
                    } else if (codeChar == 'r') {
                        currentColor = color;
                        RenderUtil.resetColor();
                        RenderUtil.color(currentColor);
                    }
                    i++;
                    continue;
                }
            }

            offset += drawChar(chr, x + offset, y);
        }

        GL11.glPopMatrix();
        RenderUtil.resetColor();
        return offset / 2;
    }


    public void drawStringDynamic(String text, double x2, double y2, int tick1, int tick2) {
        GradientUtil.applyGradientHorizontal((float) x2, (float) y2, this.getStringWidth(text), this.fontHeight, 1.0f, HUDMod.color(tick1), HUDMod.color(tick2), () -> {
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.0f);
            this.drawString(text, (float) x2, (float) y2, -1);
        });
    }

    @Override
    public void drawString(String name, float x, float y, Color color) {
        drawString(name, x, y, color.getRGB(), false);
    }

    public float getMiddleOfBox(final float height) {
        return height / 2.0f - this.getHeight() / 2.0f;
    }

    @Override
    public final float getStringWidth(final String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        final char[] currentData = text.toCharArray();
        for (int size = text.length(), i = 0; i < size; ++i) {
            final char chr = currentData[i];
            final char character = text.charAt(i);
            if (character == '§') {
                ++i;
            } else {
                width += this.getOrGenerateCharWidthMap(chr >> 8)[chr & 'ÿ'];
            }
        }
        return (float) width / 2;
    }

    public final float getSize() {
        return this.size;
    }

    private int generateCharTexture(final int id) {
        final int textureId = GL11.glGenTextures();
        final int offset = id << 8;

        final BufferedImage img = new BufferedImage(this.textureWidth, this.textureHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(this.font);
        g.setColor(Color.WHITE);

        FontMetrics fontMetrics = g.getFontMetrics();
        for (int i = 0; i < 256; i++) {
            char c = (char) (offset + i);
            int x = (i & 0xF) * fontWidth;
            int y = (i >> 4) * fontHeight;
            g.drawString(String.valueOf(c), x, y + fontMetrics.getAscent());

            this.charWidth[id][i] = (byte) fontMetrics.charWidth(c);
        }

        g.dispose();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        ByteBuffer buffer = imageToBuffer(img);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        return textureId;
    }

    private byte[] getOrGenerateCharWidthMap(int id) {
        if (this.charWidth[id] == null) {
            this.charWidth[id] = new byte[256];
            this.textures[id] = generateCharTexture(id);
        }
        return this.charWidth[id];
    }

    private int getOrGenerateCharTexture(int id) {
        if (this.textures[id] == -1) {
            this.textures[id] = generateCharTexture(id);
        }
        return this.textures[id];
    }

    private float wrapTextureCoord(int value, int max) {
        return (float) value / (float) max;
    }

    private int resizeToOpenGLSupportResolution(int size) {
        int result = 1;
        while (result < size) {
            result *= 2;
        }
        return result;
    }

    public void drawStringWithOutline(String text, float x, float y, int color, int outlineColor) {
        drawString(text, x-1, y, outlineColor); // 左
        drawString(text, x+1, y, outlineColor); // 右
        drawString(text, x, y-1, outlineColor); // 上
        drawString(text, x, y+1, outlineColor); // 下
        drawString(text, x, y, color); // 中心
    }

    public void destroy() {
        for (int texture : textures) {
            if (texture != -1) GL11.glDeleteTextures(texture);
        }
    }

    @Override
    public final int drawStringWithShadow(final String name, final float i, final float i1, final int rgb) {
        int shadowColor = (rgb & 0xFCFCFC) >> 2 | (rgb & 0xFF000000); // Darken for shadow
        this.drawString(name, i + 0.5f, i1 + 0.5f, shadowColor, false);
        return drawString(name, i, i1, rgb, false);
    }

    @Override
    public void drawStringWithShadow(String name, float x, float y, Color color) {
        int rgb = color.getRGB();
        int shadowColor = (rgb & 0xFCFCFC) >> 2 | (rgb & 0xFF000000); // Darken for shadow
        this.drawString(name, x + 0.5f, y + 0.5f, shadowColor, false);
        drawString(name, x, y, rgb, false);
    }

    public void drawStringWithShadow(final String z, final double x, final double positionY, final int mainTextColor) {
        this.drawStringWithShadow(z, (float) x, (float) positionY, mainTextColor);
    }

    public float drawStringWithShadow(final String text, final double x, final double y, final double sWidth, final int color) {
        final float shadowWidth = (float) this.drawString(text, (float) (x + sWidth), (float) (y + sWidth), color, true);
        return Math.max(shadowWidth, (float) this.drawString(text, (float) x, (float) y, color, false));
    }
}
