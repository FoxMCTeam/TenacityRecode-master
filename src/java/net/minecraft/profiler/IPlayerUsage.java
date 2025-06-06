package net.minecraft.profiler;

public interface IPlayerUsage
{
    void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper);

    /**
     * Returns whether snooping is enabled or not.
     */
    boolean isSnooperEnabled();
}
