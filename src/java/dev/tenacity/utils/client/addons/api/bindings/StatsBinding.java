package dev.tenacity.utils.client.addons.api.bindings;

import dev.tenacity.module.impl.display.Statistics;
import dev.tenacity.utils.misc.MathUtils;


public class StatsBinding {

    public int getKills() {
        return Statistics.killCount;
    }

    public int getDeaths() {
        return Statistics.deathCount;
    }

    public double getKD() {
        return getDeaths() == 0 ? getKills() : MathUtils.round((double) getKills() / getDeaths(), 2);
    }

    public int getGamesPlayed() {
        return Statistics.gamesPlayed;
    }

    public int[] getPlayTime() {
        return Statistics.getPlayTime();
    }

}
