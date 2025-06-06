package dev.tenacity.utils.client.addons.api.bindings;

import dev.tenacity.utils.tuples.Pair;
import dev.tenacity.Client;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.combat.KillAura;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.utils.player.ChatUtil;
import dev.tenacity.utils.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;

import java.awt.*;


public class ClientBinding {

    public String getClientVersion() {
        return Client.INSTANCE.getVersion();
    }


    public TimerUtil createTimer() {
        return new TimerUtil();
    }

    public void printClientMsg(String text) {
        ChatUtil.print(text);
    }

    public float fps() {
        return Minecraft.getDebugFPS();
    }

    public EntityLivingBase getAuraTarget() {
        KillAura killAura = Client.INSTANCE.getModuleCollection().getModule(KillAura.class);
        return KillAura.target;
    }

    public Pair<Color, Color> getClientColors() {
        return HUDMod.getClientColors();
    }

    public boolean leftMouseButtonDown() {
        return Mouse.isButtonDown(0);
    }

    public boolean rightMouseButtonDown() {
        return Mouse.isButtonDown(1);
    }

    public boolean isKeyDown(int key) {
        return Keyboard.isCreated() && Keyboard.isKeyDown(key);
    }


    public Module getModule(String moduleName) {
        Module module = Client.INSTANCE.getModuleCollection().getModuleByName(moduleName);
        if (module != null) {
            return module;
        } else {
            throw new NullPointerException("Module " + moduleName + " does not exist.");
        }
    }

}
