package dev.tenacity.utils.client.addons.viamcp.viamcp;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.addons.viamcp.vialoadingbase.ViaLoadingBase;

public class ProtocolFixer implements Utils {
    //ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)
    public static boolean newerThanOrEqualsTo1_8() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_8) || mc.isIntegratedServerRunning();
    }

    public static boolean newerThan1_8() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThan(ProtocolVersion.v1_8) && !mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_9() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_9) && !mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_13() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_13) && !mc.isIntegratedServerRunning();
    }

    public static boolean olderThanOrEqualsTo1_13_2() {
        return ViaLoadingBase.getInstance().getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_13_2) && !mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_14() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_14) && !mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_16() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_14) && !mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_17() {
        return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17) && !mc.isIntegratedServerRunning();
    }
}
