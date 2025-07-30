package dev.tenacity.module.impl.display;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.TickEvent;
import dev.tenacity.event.impl.render.Render2DEvent;
import dev.tenacity.event.impl.render.ShaderEvent;
import dev.tenacity.Client;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.utils.client.addons.vector.Vector2d;
import dev.tenacity.utils.font.AbstractFontRenderer;
import dev.tenacity.utils.objects.Dragging;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RoundedUtil;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ScoreboardMod extends Module {
    public final Dragging position = Client.INSTANCE.createDrag(this, "ScoreboardMod", 20f, 60.0f);
    private final BooleanSetting customFont = new BooleanSetting("Custom Font", true);
    private Collection<Score> collection;
    private ScoreObjective scoreObjective;
    private int maxWidth = 0;

    public ScoreboardMod() {
        super("module.display.scoreboard", Category.DISPLAY, "Scoreboard preferences");
        this.addSettings(customFont);
        this.setToggled(true);
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        HUDMod.colorWheel.setColors();

        float alpha = .17f;

        Vector2d position = new Vector2d(this.position.getX(), this.position.getY());

        if (this.scoreObjective != null) {
            this.renderScoreboard(
                    (int) position.x,
                    (int) position.y,
                    ColorUtil.applyOpacity(HUDMod.colorWheel.getColor1(), alpha),
                    ColorUtil.applyOpacity(HUDMod.colorWheel.getColor4(), alpha),
                    ColorUtil.applyOpacity(HUDMod.colorWheel.getColor2(), alpha),
                    ColorUtil.applyOpacity(HUDMod.colorWheel.getColor3(), alpha), new Color(0, 0, 0, 60), false);
            this.renderScoreboard(
                    (int) position.x,
                    (int) position.y
            );
        }
    }
    @EventTarget
    public void onShaderEvent(ShaderEvent event) {
        float alpha = 1f;
        HUDMod.colorWheel.setColors();
        if (this.scoreObjective == null) return;
        Vector2d position = new Vector2d(
                this.position.getX(),
                this.position.getY()
        );

        this.renderScoreboard(
                (int) position.x,
                (int) position.y,
                ColorUtil.applyOpacity(HUDMod.colorWheel.getColor1(), alpha),
                ColorUtil.applyOpacity(HUDMod.colorWheel.getColor4(), alpha),
                ColorUtil.applyOpacity(HUDMod.colorWheel.getColor2(), alpha),
                ColorUtil.applyOpacity(HUDMod.colorWheel.getColor3(), alpha), new Color(0, 0, 0), true);
    }

    /**
     * Updates the scoreboard each tick.
     */
    @EventTarget
    public void onTickEvent(TickEvent event) {
        this.scoreObjective = this.getScoreObjective();

        if (this.scoreObjective == null || event == null || mc.thePlayer == null) return;

        Collection<Score> collection = this.scoreObjective.getScoreboard().getSortedScores(this.scoreObjective);

        List<Score> list = collection.stream()
                .filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            this.collection = new ArrayList<>(list.subList(list.size() - 15, list.size()));
        } else {
            this.collection = list;
        }

        this.maxWidth = (int) mc.fontRendererObj.getStringWidth(scoreObjective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scorePlayerTeam =
                    this.scoreObjective.getScoreboard().getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());
            this.maxWidth = (int) Math.max(this.maxWidth, mc.fontRendererObj.getStringWidth(s));
        }

        this.maxWidth += 2;
    }

    private ScoreObjective getScoreObjective() {
        if (mc.theWorld == null || mc.thePlayer == null) return null;

        net.minecraft.scoreboard.Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective scoreObjective = null;
        ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(mc.thePlayer.getCommandSenderName());

        if (scorePlayerTeam != null) {
            int colorIndex = scorePlayerTeam.getChatFormat().getColorIndex();

            if (colorIndex > -1) {
                scoreObjective = scoreboard.getObjectiveInDisplaySlot(3 + colorIndex);
            }
        }

        return scoreObjective != null ? scoreObjective : scoreboard.getObjectiveInDisplaySlot(1);
    }

    private void renderScoreboard(int x, int y, Color backgroundColor, Color backgroundColor2, Color backgroundColor3, Color backgroundColor4, Color backgroundColor5, boolean shadow) {
        AbstractFontRenderer fontRenderer = customFont.isEnabled() ? duckSansFont16 : fr;
        float fontHeight = fontRenderer.getHeight() * 1.45F;
        float size = collection.size();
        float height = size * fontHeight;
        float width = maxWidth * 0.8F;
        RoundedUtil.drawThemeColorRound(
                x,
                y,
                width,
                (height + fontHeight),
                HUDMod.radius.getValue().floatValue(), backgroundColor,
                backgroundColor2, backgroundColor3, backgroundColor4, backgroundColor5, shadow
        );
        position.setX(x);
        position.setY(y);
        position.setWidth(width);
        position.setHeight(height + fontHeight);
    }


    public void renderScoreboard(int x, int y) {
        int x2 = x;
        int y2;
        String objective = scoreObjective.getDisplayName();
        AbstractFontRenderer fontRenderer = customFont.isEnabled() ? duckSansFont16 : fr;

        x2 += 6;
        y2 = (int) (y + 1.5);

        fontRenderer.drawString(objective, x2, y2, Color.white.getRGB());

        List<Score> reversedList = new ArrayList<>(collection);
        Collections.reverse(reversedList);

        // 绘制字符串
        for (Score score : reversedList) {
            y2 += 10;

            ScorePlayerTeam scorePlayerTeam = scoreObjective.getScoreboard().getPlayersTeam(score.getPlayerName());
            String s1 = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());

            fontRenderer.drawString(s1, x2, y2, Color.white.getRGB());
        }
    }
}
