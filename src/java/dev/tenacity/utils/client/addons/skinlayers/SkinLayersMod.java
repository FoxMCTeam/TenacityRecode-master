package dev.tenacity.utils.client.addons.skinlayers;

import net.minecraft.client.Minecraft;

public class SkinLayersMod extends SkinLayersModBase {

    //Forge only
    private boolean onServer = false;
    
    public SkinLayersMod() {
        try {
            Class<Minecraft> clientClass = net.minecraft.client.Minecraft.class;
        }catch(Throwable ex) {
            System.out.println("EntityCulling Mod installed on a Server. Going to sleep.");
            onServer = true;
            return;
        }
        onInitialize();
    }
}
