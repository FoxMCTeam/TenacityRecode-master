package dev.tenacity.module;

import dev.tenacity.module.impl.combat.*;
import dev.tenacity.module.impl.display.*;
import dev.tenacity.module.impl.exploit.*;
import dev.tenacity.module.impl.misc.*;
import dev.tenacity.module.impl.movement.*;
import dev.tenacity.module.impl.player.*;
import dev.tenacity.module.impl.render.*;
import dev.tenacity.module.impl.render.killeffects.KillEffects;
import dev.tenacity.module.impl.render.wings.DragonWings;
import dev.tenacity.utils.render.EntityCulling;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public static boolean reloadModules;
    @Getter
    private final List<Class<? extends Module>> hiddenModules = new ArrayList<>(Arrays.asList(ArrayListMod.class, NotificationsMod.class));
    @Setter
    private HashMap<Object, Module> modules = new HashMap<>();

    public void init() {
        // Combat
        modules.put(KillAura.class, new KillAura());
        modules.put(Velocity.class, new Velocity());
        modules.put(Criticals.class, new Criticals());
        modules.put(AutoHead.class, new AutoHead());
        modules.put(AutoPot.class, new AutoPot());
        modules.put(FastBow.class, new FastBow());
        modules.put(KeepSprint.class, new KeepSprint());
        modules.put(SuperKnockback.class, new SuperKnockback());

        // Exploit
        modules.put(Disabler.class, new Disabler());
        modules.put(AntiInvis.class, new AntiInvis());
        modules.put(Regen.class, new Regen());
        modules.put(TPAKiller.class, new TPAKiller());
        modules.put(AntiAura.class, new AntiAura());
        modules.put(ResetVL.class, new ResetVL());
        modules.put(Crasher.class, new Crasher());

        // Misc
        modules.put(AntiDesync.class, new AntiDesync());
        modules.put(AntiTabComplete.class, new AntiTabComplete());
        modules.put(Spammer.class, new Spammer());
        modules.put(AntiFreeze.class, new AntiFreeze());
        modules.put(LightningTracker.class, new LightningTracker());
        modules.put(HackerDetector.class, new HackerDetector());
        modules.put(AutoHypixel.class, new AutoHypixel());
        modules.put(NoRotate.class, new NoRotate());
        modules.put(AutoRespawn.class, new AutoRespawn());
        modules.put(MCF.class, new MCF());
        modules.put(AutoAuthenticate.class, new AutoAuthenticate());
        modules.put(Killsults.class, new Killsults());
        modules.put(Sniper.class, new Sniper());

        // Movement
        modules.put(Sprint.class, new Sprint());
        modules.put(Scaffold.class, new Scaffold());
        modules.put(Speed.class, new Speed());
        modules.put(Flight.class, new Flight());
        modules.put(LongJump.class, new LongJump());
        modules.put(Step.class, new Step());
        modules.put(TargetStrafe.class, new TargetStrafe());
        modules.put(FastLadder.class, new FastLadder());
        modules.put(InventoryMove.class, new InventoryMove());
        modules.put(Jesus.class, new Jesus());
        modules.put(Spider.class, new Spider());
        modules.put(AutoHeadHitter.class, new AutoHeadHitter());

        // Player
        modules.put(ChestStealer.class, new ChestStealer());
        modules.put(InvManager.class, new InvManager());
        modules.put(AutoArmor.class, new AutoArmor());
        modules.put(SpeedMine.class, new SpeedMine());
        modules.put(Blink.class, new Blink());
        modules.put(NoFall.class, new NoFall());
        modules.put(Timer.class, new Timer());
        modules.put(Freecam.class, new Freecam());
        modules.put(FastPlace.class, new FastPlace());
        modules.put(SafeWalk.class, new SafeWalk());
        modules.put(NoSlow.class, new NoSlow());
        modules.put(AutoTool.class, new AutoTool());
        modules.put(AntiVoid.class, new AntiVoid());
        modules.put(KillEffects.class, new KillEffects());

        // Render
        modules.put(ArrayListMod.class, new ArrayListMod());
        modules.put(NotificationsMod.class, new NotificationsMod());
        modules.put(ScoreboardMod.class, new ScoreboardMod());
        modules.put(HUDMod.class, new HUDMod());
        modules.put(ClickGUIMod.class, new ClickGUIMod());
        modules.put(Radar.class, new Radar());
        modules.put(Animations.class, new Animations());
        modules.put(Ambience.class, new Ambience());
        modules.put(ChinaHat.class, new ChinaHat());
        modules.put(GlowESP.class, new GlowESP());
        modules.put(Brightness.class, new Brightness());
        modules.put(ESP2D.class, new ESP2D());
        modules.put(PostProcessing.class, new PostProcessing());
        modules.put(Statistics.class, new Statistics());
        modules.put(TargetHUDMod.class, new TargetHUDMod());
        modules.put(Glint.class, new Glint());
        modules.put(Breadcrumbs.class, new Breadcrumbs());
        modules.put(Streamer.class, new Streamer());
        modules.put(Hitmarkers.class, new Hitmarkers());
        modules.put(NoHurtCam.class, new NoHurtCam());
        modules.put(Keystrokes.class, new Keystrokes());
        modules.put(ItemPhysics.class, new ItemPhysics());
        modules.put(XRay.class, new XRay());
        modules.put(EntityCulling.class, new EntityCulling());
        modules.put(DragonWings.class, new DragonWings());
        modules.put(PlayerList.class, new PlayerList());
        modules.put(JumpCircle.class, new JumpCircle());
        modules.put(CustomModel.class, new CustomModel());
        modules.put(EntityEffects.class, new EntityEffects());
        modules.put(Chams.class, new Chams());
        modules.put(BrightPlayers.class, new BrightPlayers());
    }

    public List<Module> getModules() {
        return new ArrayList<>(this.modules.values());
    }

    public HashMap<Object, Module> getModuleMap() {
        return modules;
    }

    public List<Module> getModulesInCategory(Category c) {
        return this.modules.values().stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

    public Module get(Class<? extends Module> mod) {
        return this.modules.get(mod);
    }

    public <T extends Module> T getModule(Class<T> mod) {
        return (T) this.modules.get(mod);
    }

    public List<Module> getModulesThatContainText(String text) {
        return this.getModules().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return this.modules.values().stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Module> getModulesContains(String text) {
        return this.modules.values().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public final List<Module> getToggledModules() {
        return this.modules.values().stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    public final List<Module> getArraylistModules(ArrayListMod arraylistMod, List<Module> modules) {
        return modules.stream().filter(module -> module.isEnabled() &&
                !((module.getCategory() == Category.RENDER && arraylistMod.hideModules.isEnabled("Render")) ||
                 (module.getCategory() == Category.DISPLAY && arraylistMod.hideModules.isEnabled("Display")) ||
                 (module.getCategory() == Category.MISC && arraylistMod.hideModules.isEnabled("Misc")) ||
                 (module.getCategory() == Category.COMBAT && arraylistMod.hideModules.isEnabled("Combat")) ||
                 (module.getCategory() == Category.PLAYER && arraylistMod.hideModules.isEnabled("Player")) ||
                 (module.getCategory() == Category.MOVEMENT && arraylistMod.hideModules.isEnabled("Movement"))
                 )).collect(Collectors.toList());
    }

}
