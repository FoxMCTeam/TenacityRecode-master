package dev.tenacity.utils.render;

import dev.tenacity.event.EventState;
import dev.tenacity.event.annotations.EventTarget;

import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.event.impl.render.EventAddDynamicIsland;
import dev.tenacity.event.impl.render.Render2DEvent;
import dev.tenacity.Client;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.impl.display.NotificationsMod;
import dev.tenacity.module.impl.display.PostProcessing;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.animations.AnimationUtil;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.animations.impl.EaseOutExpo;
import dev.tenacity.utils.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jon_awa
 * @since 25/5/9
 */
public class DynamicIslandManager implements Utils {
    private final List<TimedContent> timedContents;
    private final List<Content> contents;
    private float renderX, width, height;
    private float[] containerPrevSlotColor;
    private float[] containerSlotColor;

    public DynamicIslandManager() {
        this.contents = new ArrayList<>();
        this.timedContents = new ArrayList<>();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (NotificationsMod.mode.is("Dynamic Island") && Client.INSTANCE.getModuleManager().getModule(NotificationsMod.class).isEnabled()) {
            final ScaledResolution sr = new ScaledResolution(mc);

            EventAddDynamicIsland dynamicIslandEvent = new EventAddDynamicIsland(EventState.PRE);
            Client.INSTANCE.getEventManager().call(dynamicIslandEvent);

            if (!this.timedContents.isEmpty()) {
                for (TimedContent content : new ArrayList<>(this.timedContents)) {
                    if (!content.timer.hasTimeElapsed(content.time)) {
                        this.contents.add(new Content(() -> {
                            switch (content.type) {
                                case MODULE -> {
                                    if (content.module != null) {
                                        if (content.module.isEnabled()) {
                                            content.animation.setDirection(Direction.FORWARDS);
                                        } else {
                                            content.animation.setDirection(Direction.BACKWARDS);
                                        }

                                        double animation = content.animation.getOutput();
                                        RoundedUtil.drawRound(6, 13, 26, 14, 6.5f, Color.DARK_GRAY);
                                        RoundedUtil.drawRound(7, 14, 24, 12, 6, new Color((int) (128 - 80 * animation), (int) (128 - 11 * animation), (int) (128 + 19 * animation), 255));
                                        RoundedUtil.drawRound((float) (8 + 12 * animation), 15, 10, 10, 4.5f, Color.DARK_GRAY);
                                    }
                                }

                                case SUCCESS -> {
                                    if (content.timer.hasTimeElapsed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRound(7, 8, 24, 24, 7, new Color(95, 143, 80, 200));

                                    StencilUtil.initStencilToWrite();
                                    RenderUtil.drawRectWH(7, 8, 24 * animation, 24, -1);
                                    StencilUtil.readStencilBuffer();
                                    iconfontFont.size(44).drawString("A", 10.5f, 13, -1);
                                    StencilUtil.endStencilBuffer();
                                }

                                case WARNING -> {
                                    if (content.timer.hasTimeElapsed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRound(7, 8, 24, 24, 7, new Color(143, 80, 80, 200));

                                    StencilUtil.initStencilToWrite();
                                    RenderUtil.drawRectWH(7, 8, 24, 24 * animation, -1);
                                    StencilUtil.readStencilBuffer();
                                    iconfontFont.size(44).drawString("C", 11.3f, 13, -1);
                                    StencilUtil.endStencilBuffer();
                                }

                                case INFO -> {
                                    if (content.timer.hasTimeElapsed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRound(7, 8, 24, 24, 7, new Color(48, 117, 147, 200));
                                    RoundedUtil.drawRound(18.2f, 11, 1.5f, (float) (18 * animation), 0.75f, Color.WHITE);
                                    RoundedUtil.drawRound(10, 19, (float) (18 * animation), 1.5f, 0.75f, Color.WHITE);
                                }
                                default -> RoundedUtil.drawRound(7, 8, 24, 24, 7, new Color(200, 200, 200, 200));
                            }

                            duckSansBoldFont20.drawString(content.title, 38, 9, -1);

                            if (content.type == ContentType.MODULE && content.module != null) {
                                content.description = content.module.getName() + (content.module.isEnabled() ? " has been §2Enabled" : " has been §4Disabled");
                                duckSansFont18.drawString(content.description, 38, 24, -1);
                            } else {
                                duckSansFont18.drawString(content.description, 38, 24, -1);
                            }
                        }, (50 + Math.max(duckSansBoldFont20.getStringWidth(content.title), duckSansFont18.getStringWidth(content.description))), 40, PriorityUtil.LOW));
                    } else {
                        this.timedContents.remove(content);
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerChest container) {
                this.contents.clear();

                if (containerPrevSlotColor == null || containerPrevSlotColor.length != container.inventorySlots.size() - 36) {
                    containerPrevSlotColor = new float[container.inventorySlots.size() - 36];
                }

                if (containerSlotColor == null || containerSlotColor.length != container.inventorySlots.size() - 36) {
                    containerSlotColor = new float[container.inventorySlots.size() - 36];
                }


                this.contents.add(new Content(() -> {
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.enableDepth();

                    boolean isEmpty = true;
                    for (int i = 0; i < container.inventorySlots.size() - 36; ++i) {
                        Slot slot = container.inventorySlots.get(i);

                        int x = slot.xDisplayPosition + 3;
                        int y = slot.yDisplayPosition - 10;

                        if (containerSlotColor[i] == 100) {
                            containerPrevSlotColor[i] = 0;
                        }

                        if (slot.getHasStack()) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), x - 2, y - 3);
                            mc.getRenderItem().renderItemOverlayIntoGUI(duckSansFont18, slot.getStack(), x - 2, y - 3, null);
                            isEmpty = false;
                        }

                        RoundedUtil.drawRound(x - 2, y - 3, 16, 16, 7, new Color(220, 220, 220, (int) (containerSlotColor[i] = AnimationUtil.smooth(containerSlotColor[i], containerPrevSlotColor[i], 0.3f))));
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.popMatrix();

                    if (isEmpty) {
                        duckSansFont20.drawCenteredString("Empty...", 90, (int) Math.ceil((float) container.getLowerChestInventory().getSizeInventory() / 9) * 10 - 3, -1);
                    }
                }, 180, (float) Math.ceil((float) container.getLowerChestInventory().getSizeInventory() / 9) * 21, PriorityUtil.TOP));
            } else if (mc.thePlayer.openContainer instanceof ContainerFurnace || mc.thePlayer.openContainer instanceof ContainerBrewingStand) {
                this.contents.clear();

                if (containerPrevSlotColor == null || containerPrevSlotColor.length != mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                    containerPrevSlotColor = new float[mc.thePlayer.openContainer.inventorySlots.size() - 36];
                }

                if (containerSlotColor == null || containerSlotColor.length != mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                    containerSlotColor = new float[mc.thePlayer.openContainer.inventorySlots.size() - 36];
                }

                this.contents.add(new Content(() -> {
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.enableDepth();

                    boolean isEmpty = true;
                    for (int i = 0; i < mc.thePlayer.openContainer.inventorySlots.size() - 36; ++i) {
                        Slot slot = mc.thePlayer.openContainer.inventorySlots.get(i);

                        if (containerSlotColor[i] == 100) {
                            containerPrevSlotColor[i] = 0;
                        }

                        if (slot != null && slot.getHasStack()) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), 5 + 20 * i, 5);
                            mc.getRenderItem().renderItemOverlayIntoGUI(duckSansFont18, slot.getStack(), 5 + 20 * i, 5, null);
                            isEmpty = false;
                        }

                        RoundedUtil.drawRound(3 + 20 * i, 3, 16, 16, 7, new Color(220, 220, 220, (int) (containerSlotColor[i] = AnimationUtil.smooth(containerSlotColor[i], containerPrevSlotColor[i], 0.3f))));
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.popMatrix();

                    if (isEmpty) {
                        duckSansFont20.drawCenteredString("Empty...", 90, 20, -1);
                    }
                }, 180, 50, PriorityUtil.TOP));
            }

            Client.INSTANCE.getEventManager().call(dynamicIslandEvent.setState(EventState.POST));

            int maxWeight = this.contents.stream().mapToInt(content -> (int) content.weight).max().orElse(-1);
            this.contents.removeIf(content -> content.weight < maxWeight);

            final NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            final String defaultText = " | " + mc.session.getUsername() + " | " + Minecraft.getDebugFPS() + "fps" + " | " + ((playerInfo == null || playerInfo.getResponseTime() == 0) ? "Pinging..." : playerInfo.getResponseTime() + " ping");

            this.renderX = AnimationUtil.smooth(this.renderX, (sr.getScaledWidth() - this.width) / 2f, 0.2f);
            this.width = AnimationUtil.smooth(this.width, (this.contents.isEmpty() && this.timedContents.isEmpty()) ? duckSansBoldFont20.getStringWidth(Client.NAME) + duckSansFont20.getStringWidth(defaultText) + 29 : this.getMaxWidth(this.contents), 0.2f);
            this.height = AnimationUtil.smooth(this.height, (this.contents.isEmpty() && this.timedContents.isEmpty()) ? duckSansFont20.getHeight() + 8 : this.getTotalHeight(this.contents), 0.2f);
            PostProcessing.runBloom(() -> RoundedUtil.drawRound(this.renderX, 15, this.width, this.height, 10.5f, Color.BLACK), true, true, 1);
            RoundedUtil.drawRound(this.renderX, 15, this.width, this.height, 10.5f, new Color(0, 0, 0, 130));
            RenderUtil.scissorStart((int) this.renderX, 15, (int) this.width, (int) this.height);

            if (this.contents.isEmpty() && this.timedContents.isEmpty()) {
                GradientUtil.applyGradientHorizontal(this.renderX, 14, 50, duckSansBoldFont20.getHeight() + 8, 1, HUDMod.color(1), HUDMod.color(4), () -> {
                    iconfontFont.size(60).drawString("x", this.renderX, 14, -1);
                    duckSansBoldFont20.drawString(Client.NAME, this.renderX + 22, 21.5f, -1);
                });
                duckSansFont20.drawString(defaultText, this.renderX + 39, 21.5f, -1);
            } else {
                float renderY = 15;
                for (Content content : this.contents) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(this.renderX, renderY, 0);
                    content.content.run();
                    GlStateManager.popMatrix();
                    renderY += content.height;
                }
                this.contents.clear();
            }

            RenderUtil.scissorEnd();
        }
    }

    @EventTarget
    public void onPacketEvent(PacketEvent event) {
        if (event.getPacket() instanceof C0EPacketClickWindow c0e && (mc.thePlayer.openContainer instanceof ContainerChest || mc.thePlayer.openContainer instanceof ContainerFurnace || mc.thePlayer.openContainer instanceof ContainerBrewingStand)) {
            if (containerPrevSlotColor != null && containerPrevSlotColor.length == mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                if (c0e.getSlotId() < mc.thePlayer.openContainer.inventorySlots.size() - 36 && c0e.getSlotId() >= 0) {
                    containerPrevSlotColor[c0e.getSlotId()] = 100;
                }
            }
        }

        if (event.getPacket() instanceof C0DPacketCloseWindow) {
            containerSlotColor = null;
            containerPrevSlotColor = null;
        }
    }
    private float getMaxWidth(List<Content> contents) {
        float width = 0;

        for (Content content : contents) {
            if (width < content.width) {
                width = content.width;
            }
        }

        return width;
    }

    private float getTotalHeight(List<Content> contents) {
        float height = 0;

        for (Content content : contents) {
            height += content.height;
        }

        return height;
    }

    public void addContent(Runnable content, int width, int height, int weight) {
        this.contents.add(new Content(content, width, height, weight));
    }

    public void addContent(ContentType type, String title, String text, long time) {
        this.timedContents.add(new TimedContent(type, title, text, time));
    }

    public void addContent(Module module) {
        boolean canAdd = true;
        for (TimedContent content : this.timedContents) {
            if (content.module == null) continue;
            if (content.module == module) {
                content.time = content.timer.getTime() + 2000;
                canAdd = false;
                break;
            }
        }

        if (canAdd) {
            this.timedContents.add(new TimedContent(module));
        }
    }

    public void addContent(Runnable icon, String title, String description, float present, Color color) {
        this.contents.add(new Content( () -> {
            RoundedUtil.drawRound(7, 8, 24, 24, 7, ColorUtil.applyOpacity(color, 255));

            if (icon != null) {
                icon.run();
            }

            duckSansBoldFont20.drawString(title, 38, 9, -1);
            duckSansFont18.drawString(description, 38, 24, -1);
            RoundedUtil.drawRound(7, 38, this.width - 14, 5, 2, ColorUtil.applyOpacity(color.darker(), 150));
            RoundedUtil.drawRound(7, 38, Math.min(present * this.width - 14, this.width - 14), 5, 2, ColorUtil.applyOpacity(color, 255));
        }, (50 + Math.max(duckSansBoldFont20.getStringWidth(title), duckSansFont18.getStringWidth(description))), 50, PriorityUtil.MEDIUM));
    }

    public enum ContentType {
        MODULE,
        SUCCESS,
        WARNING,
        INFO
    }

    public static class Content {
        private final Runnable content;
        private final float width;
        private final float height;
        private final float weight;

        private Content(Runnable content, float width, float height, float weight) {
            this.content = content;
            this.width = width;
            this.height = height;
            this.weight = weight;
        }
    }

    private static class TimedContent {
        private final ContentType type;
        private final Module module;
        private final String title;
        private final EaseOutExpo animation;
        private final TimerUtil timer;
        private String description;
        private long time;

        TimedContent(ContentType contentType, String title, String description, long time) {
            this.type = contentType;
            this.module = null;
            this.title = title;
            this.description = description;
            this.animation = new EaseOutExpo(800, 1);
            this.timer = new TimerUtil();
            this.time = time;
        }

        TimedContent(Module module) {
            this.type = ContentType.MODULE;
            this.module = module;
            this.title = "Module Toggled";
            this.description = module.getName() + (module.isEnabled() ? " has been §2Enabled" : " has been §4Disabled");
            this.animation = new EaseOutExpo(300, 1);
            this.timer = new TimerUtil();
            this.time = 2000;
        }
    }
}
